package com.trollLab.services.serviceImpl;

import com.trollLab.services.TikTokService;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.models.badges.PictureBadge;
import io.github.jwdeveloper.tiktok.data.models.users.UserAttribute;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.data.models.*;
import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class TikTokServiceImpl implements TikTokService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, LiveClient> activeClients = new ConcurrentHashMap<>();
    private final Map<String, String> registeredUserId = new ConcurrentHashMap<>();

    @Autowired
    public TikTokServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void startMonitoring(String userId, String tiktokUser) {

        if (activeClients.containsKey(userId)) {
            LiveClient existingClient = activeClients.get(userId);
            String existingTikTokUser = registeredUserId.get(userId);

            if (existingTikTokUser.equals(tiktokUser)) {
                System.out.println("User " + userId + " is already monitoring TikTok user " + tiktokUser);
                return;
            }

            System.out.println("User " + userId + " is switching from " + existingTikTokUser + " to " + tiktokUser);
            existingClient.disconnect();
            activeClients.remove(userId);
        }

        try {
            LiveClient liveClient = TikTokLive.newClient(tiktokUser)
                    .onGift((client, event) -> {
                        sendMessageToWebSocket("/topic/gifts", Map.of(
                                "giftMessage", event.getUser().getProfileName() + " Send-> " + event.getGift().getName(),
                                "user", event.getUser().getProfileName()
                        ));
                    })
                    .onRoomInfo((client, event) -> {
                        LiveRoomInfo roomInfo = event.getRoomInfo();
                        sendMessageToWebSocket("/topic/roomInfo", Map.of(
                                "roomId", roomInfo.getHost() != null ? roomInfo.getHost().getProfileName() : "Unknown",
                                "likes", roomInfo.getLikesCount(),
                                "viewers", roomInfo.getViewersCount(),
                                "startTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                "totalViewers", roomInfo.getTotalViewersCount(),
                                "ranking", roomInfo.getUsersRanking().stream()
                                        .map(r -> "Rank " + r.getRank() + ": " + r.getUser().getProfileName() + " (Score: " + r.getScore() + ")")
                                        .collect(Collectors.joining("\n")),
                                "title", roomInfo.getTitle(),
                                "picture", roomInfo.getHost() != null && roomInfo.getHost().getPicture() != null
                                        ? roomInfo.getHost().getPicture().getLink()
                                        : "default-image-url.jpg"
                        ));
                    })
                    .onJoin((client, event) -> sendMessageToWebSocket("/topic/join", Map.of("username", event.getUser().getProfileName())))
                    .onConnected((client, event) -> sendMessageToWebSocket("/topic/status", Map.of("statusMessage", "Connected to Live Stream!")))
                    .onError((client, event) -> sendMessageToWebSocket("/topic/error", Map.of("errorMessage", "Error! " + event.getException().getMessage())))
                    .onComment((client, event) -> sendMessageToWebSocket("/topic/comments", Map.of(
                            "commenterName", event.getUser().getProfileName(),
                            "commentMessage", event.getText(),
                            "profileImageUrl", Optional.ofNullable(event.getUser().getPicture()).map(Picture::getLink).orElse("default-image-url.jpg"),
                            "badges", event.getUser().getBadges().stream()
                                    .filter(b -> b instanceof PictureBadge)
                                    .map(b -> ((PictureBadge) b).getPicture().getLink())
                                    .toList(),
                            "attributes", event.getUser().getAttributes().stream().map(UserAttribute::name).toList()
                    )))
                    .configure(settings -> {
                        settings.setClientLanguage("bg");
                        settings.setLogLevel(Level.ALL);
                        settings.setPrintToConsole(true);
                        settings.setRetryOnConnectionFailure(true);
                        settings.setRetryConnectionTimeout(Duration.ofSeconds(1));
                    })
                    .buildAndConnect();

            activeClients.put(userId, liveClient);
            registeredUserId.put(userId, tiktokUser);
            System.out.println("Started monitoring TikTok user " + tiktokUser + " for session " + userId);
        } catch (Exception e) {
            stopMonitoring(userId);
            System.out.println("Failed to start monitoring TikTok user " + tiktokUser + ": " + e.getMessage());
        }
    }

    @Override
    public void stopMonitoring(String userId) {
        LiveClient liveClient = activeClients.get(userId);
        if (liveClient != null) {
            liveClient.disconnect();
            registeredUserId.put(userId,"");
            System.out.println("Stopped monitoring TikTok user for session " + userId);
        } else {
            System.out.println("No active monitoring found for user: " + userId);
        }
    }

    private void sendMessageToWebSocket(String topic, Map<String, ?> messageData) {
        try {
            messagingTemplate.convertAndSend(topic, messageData);
        } catch (Exception e) {
            System.out.println("Failed to send message to WebSocket: " + e.getMessage());
        }
    }

    public void registerNewUserId(String userId) {
        registeredUserId.putIfAbsent(userId, "");
        System.out.println("Session registered: " + userId);
    }
}
