package com.trollLab.services.serviceImpl;

import com.trollLab.services.TikTokService;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.badges.Badge;
import io.github.jwdeveloper.tiktok.data.models.badges.PictureBadge;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.data.models.users.UserAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Service
public class TikTokServiceImpl implements TikTokService {

    private static final Logger logger = LoggerFactory.getLogger(TikTokServiceImpl.class);
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TikTokServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void startMonitoring(String tiktokUser) {
        logger.debug("Starting monitoring for user: {}", tiktokUser);
        TikTokLive.newClient(tiktokUser)
                .onGift((liveClient, event) -> {
                    Gift gift = event.getGift();
                    User user = event.getUser();
                    String message = user.getProfileName() + " Send-> " + gift.getName();
                    logger.debug("Received gift: {} from {}", gift.getName(), user.getProfileName());
                    sendMessageToWebSocket("/topic/gifts", Map.of("giftMessage", message, "user", user.getProfileName()));
                })
                .onRoomInfo((liveClient, event) -> {
                    TikTokRoomInfo roomInfo = (TikTokRoomInfo) event.getRoomInfo();
                    logger.debug("Room info: {}", roomInfo);
                    sendMessageToWebSocket("/topic/roomInfo", Map.of(
                            "roomId", roomInfo.getHostName(),
                            "likes", roomInfo.getLikesCount(),
                            "viewers", roomInfo.getViewersCount()
                    ));
                })
                .onJoin((liveClient, event) -> {
                    User user = event.getUser();
                    logger.debug("User joined: {}", user.getProfileName());
                    sendMessageToWebSocket("/topic/join", Map.of("username", user.getProfileName()));
                })
                .onConnected((liveClient, event) -> {
                    logger.debug("Connected to the live stream!");
                    sendMessageToWebSocket("/topic/status", Map.of("statusMessage", "Connected to Live Stream!"));
                })
                .onError((liveClient, event) -> {
                    logger.error("Error during the stream: ", event.getException());
                    sendMessageToWebSocket("/topic/error", Map.of("errorMessage", "Error! " + event.getException().getMessage()));
                })
                .onComment((liveClient, event) -> {
                    TikTokCommentEvent commentEvent = (TikTokCommentEvent) event;
                    User user = commentEvent.getUser();

                    // Подготовка на съобщението за коментара
                    String commentMessage = user.getProfileName() + " -> " + commentEvent.getText();

                    // Извличане на URL на профилната снимка
                    Picture profilePicture = user.getPicture();
                    String profileImageUrl = profilePicture != null ? profilePicture.getLink() : "default-image-url.jpg";

                    // Извличане на URL-ите на баджовете
                    List<Object> badges = user.getBadges().stream()
                            .map(badge -> {
                                // Проверяваме дали баджът е от тип PictureBadge, който има изображение
                                if (badge instanceof PictureBadge) {
                                    return ((PictureBadge) badge).getPicture().downloadImage();
                                }
                                // Ако баджът няма изображение, връщаме празен стринг или заместител
                                return "";
                            })
                            .filter(url -> url != null) // Филтрираме празните и null URL-и
                            .toList();

                    // Получаване на атрибутите на потребителя
                    List<String> attributes = user.getAttributes().stream()
                            .map(UserAttribute::name)  // Конвертиране на атрибутите в низове (имена)
                            .toList();

                    // Изпращане на съобщението чрез WebSocket
                    sendMessageToWebSocket("/topic/comments", Map.of(
                            "commenterName", user.getProfileName(),
                            "commentMessage", commentEvent.getText(),
                            "profileImageUrl", profileImageUrl,
                            "badges", badges,
                            "attributes", attributes
                    ));
                })
                .configure(settings -> {
                    settings.setClientLanguage("bg"); // Настройка на езика
                    settings.setLogLevel(Level.ALL); // Ниво на логовете
                    settings.setPrintToConsole(true); // Печати всички логове на конзолата
                    settings.setRetryOnConnectionFailure(true); // Опитва се отново при грешка в свързването
                    settings.setRetryConnectionTimeout(Duration.ofSeconds(1)); // Време преди следващото свързване
                })
                .buildAndConnect();

        logger.debug("Stream settings configured and attempting to connect.");
    }

    private void sendMessageToWebSocket(String topic, Map<String, ?> messageData) {
        messagingTemplate.convertAndSend(topic, messageData);
    }
}
