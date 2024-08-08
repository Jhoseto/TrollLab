package com.trollLab.services.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trollLab.services.TikTokService;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
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
                    String message = getGiftMessage(gift);
                    logger.debug("Received gift: {} from {}", message, event.getUser().getProfileName());
                    sendMessageToWebSocket("/topic/gifts", Map.of("giftMessage", message, "user", event.getUser().getProfileName()));
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
                    String joinMessage = user.getProfileName();
                    logger.debug("User joined: {}", user.getProfileName());
                    sendMessageToWebSocket("/topic/join", Map.of("username", event.getUser().getProfileName()));
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
                    String commentMessage = commentEvent.getUser().getProfileName() + " -> " + commentEvent.getText();
                    sendMessageToWebSocket("/topic/comments", Map.of("commenterName", commentEvent.getUser().getProfileName(),
                            "commentMessage", commentEvent.getText()));
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

    private String getGiftMessage(Gift gift) {
        if (gift == null) {
            return "Неизвестен подарък";
        }
        switch (gift.getName()) {
            case "ROSE":
                return "РОЗА!";
            case "GG":
                return "ДОБРА ИГРА";
            case "TIKTOK":
                return "ЙЕ";
            case "CORGI":
                return "Хубав подарък";
            default:
                return "Благодаря за " + gift.getName();
        }
    }
}
