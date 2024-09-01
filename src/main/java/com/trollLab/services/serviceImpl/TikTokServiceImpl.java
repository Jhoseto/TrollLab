package com.trollLab.services.serviceImpl;

import com.trollLab.services.TikTokService;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.models.RankingUser;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.badges.PictureBadge;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.data.models.users.UserAttribute;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class TikTokServiceImpl implements TikTokService {

    private static final Logger logger = LoggerFactory.getLogger(TikTokServiceImpl.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, LiveClient> activeClients = new ConcurrentHashMap<>();

    @Autowired
    public TikTokServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void startMonitoring(String tiktokUser) {
        logger.debug("Starting monitoring for user: {}", tiktokUser);

        LiveClient liveClient = TikTokLive.newClient(tiktokUser)
                .onGift((client, event) -> {
                    Gift gift = event.getGift();
                    User user = event.getUser();
                    String message = user.getProfileName() + " Send-> " + gift.getName();
                    logger.debug("Received gift: {} from {}", gift.getName(), user.getProfileName());
                    sendMessageToWebSocket("/topic/gifts", Map.of("giftMessage", message, "user", user.getProfileName()));
                })
                .onRoomInfo((client, event) -> {
                    LiveRoomInfo roomInfo = event.getRoomInfo();
                    logger.debug("Room info: {}", roomInfo);

                    // Format the ranking information
                    List<RankingUser> rankings = roomInfo.getUsersRanking();
                    String rankingFormatted = rankings.stream()
                            .map(rankingUser -> "Rank " + rankingUser.getRank() + ": " +
                                    rankingUser.getUser().getProfileName() + " (Score: " +
                                    rankingUser.getScore() + ")")
                            .collect(Collectors.joining("\n"));

                    // Get current date and time
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String formattedNow = now.format(formatter);

                    // Send message to WebSocket
                    sendMessageToWebSocket("/topic/roomInfo", Map.of(
                            "roomId", roomInfo.getHostUser().getProfileName(),
                            "likes", roomInfo.getLikesCount(),
                            "viewers", roomInfo.getViewersCount(),
                            "startTime", formattedNow,
                            "totalViewers", roomInfo.getTotalViewersCount(),
                            "ranking", rankingFormatted,
                            "title", roomInfo.getTitle(),
                            "picture", roomInfo.getHostUser().getPicture().getLink()
                    ));
                })
                .onJoin((client, event) -> {
                    User user = event.getUser();
                    logger.debug("User joined: {}", user.getProfileName());
                    sendMessageToWebSocket("/topic/join", Map.of("username", user.getProfileName()));
                })
                .onConnected((client, event) -> {
                    logger.debug("Connected to the live stream!");
                    sendMessageToWebSocket("/topic/status", Map.of("statusMessage", "Connected to Live Stream!"));
                })
                .onError((client, event) -> {
                    logger.error("Error during the stream: ", event.getException());
                    sendMessageToWebSocket("/topic/error", Map.of("errorMessage", "Error! " + event.getException().getMessage()));
                })
                .onComment((client, event) -> {
                    TikTokCommentEvent commentEvent = event;
                    User user = commentEvent.getUser();

                    String commentMessage = user.getProfileName() + " -> " + commentEvent.getText();

                    Picture profilePicture = user.getPicture();
                    String profileImageUrl = (profilePicture != null && profilePicture.getLink() != null)
                            ? profilePicture.getLink()
                            : "default-image-url.jpg";

                    List<String> badges = user.getBadges().stream()
                            .filter(badge -> badge instanceof PictureBadge)
                            .map(badge -> ((PictureBadge) badge).getPicture().getLink())
                            .filter(url -> url != null && !url.isEmpty())
                            .toList();

                    List<String> attributes = user.getAttributes().stream()
                            .map(UserAttribute::name)
                            .toList();

                    sendMessageToWebSocket("/topic/comments", Map.of(
                            "commenterName", user.getProfileName(),
                            "commentMessage", commentEvent.getText(),
                            "profileImageUrl", profileImageUrl,
                            "badges", badges,
                            "attributes", attributes
                    ));
                })
                .configure(settings -> {
                    settings.setClientLanguage("bg");
                    settings.setLogLevel(Level.ALL);
                    settings.setPrintToConsole(true);
                    settings.setRetryOnConnectionFailure(true);
                    settings.setRetryConnectionTimeout(Duration.ofSeconds(1));
                })
                .buildAndConnect();

        activeClients.put(tiktokUser, liveClient);
        logger.debug("Stream settings configured and connected for user: {}", tiktokUser);
    }

    @Override
    public void stopMonitoring(String tiktokUser) {
        LiveClient liveClient = activeClients.remove(tiktokUser);
        if (liveClient != null) {
            liveClient.disconnect();
            logger.debug("Stopped monitoring for user: {}", tiktokUser);
        } else {
            logger.warn("No active monitoring found for user: {}", tiktokUser);
        }
    }

    private void sendMessageToWebSocket(String topic, Map<String, ?> messageData) {
        try {
            messagingTemplate.convertAndSend(topic, messageData);
        } catch (Exception e) {
            logger.error("Failed to send message to WebSocket: ", e);
        }
    }
}
