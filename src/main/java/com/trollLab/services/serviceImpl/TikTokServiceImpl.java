package com.trollLab.services.serviceImpl;

import com.trollLab.services.TikTokService;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.RankingUser;
import io.github.jwdeveloper.tiktok.data.models.badges.PictureBadge;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.models.users.UserAttribute;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                        Gift gift = event.getGift();
                        User user = event.getUser();
                        String message = user.getProfileName() + " sent -> " + gift.getName();
                        sendMessageToUser(userId, "/topic/gifts", Map.of(
                                "giftMessage", message,
                                "user", user.getProfileName()
                        ));
                    })
                    .onRoomInfo((client, event) -> {
                        LiveRoomInfo roomInfo = event.getRoomInfo();

                        List<RankingUser> rankings = roomInfo.getUsersRanking();
                        String rankingFormatted = rankings.stream()
                                .map(r -> "Rank " + r.getRank() + ": " +
                                        r.getUser().getProfileName() + " (Score: " +
                                        r.getScore() + ")")
                                .collect(Collectors.joining("\n"));

                        String formattedNow = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                        sendMessageToUser(userId, "/topic/roomInfo", Map.of(
                                "roomId", roomInfo.getHostName(),
                                "likes", roomInfo.getLikesCount(),
                                "viewers", roomInfo.getViewersCount(),
                                "startTime", formattedNow,
                                "totalViewers", roomInfo.getTotalViewersCount(),
                                "ranking", rankingFormatted,
                                "title", roomInfo.getTitle(),
                                "picture", roomInfo.getHost().getPicture().getLink()
                        ));
                    })
                    .onJoin((client, event) -> {
                        User user = event.getUser();
                        sendMessageToUser(userId, "/topic/join", Map.of("username", user.getProfileName()));
                    })
                    .onConnected((client, event) -> {
                        sendMessageToUser(userId, "/topic/status", Map.of("statusMessage", "Connected to Live Stream!"));
                    })
                    .onError((client, event) -> {
                        sendMessageToUser(userId, "/topic/error", Map.of("errorMessage", "Error! " + event.getException().getMessage()));
                    })
                    .onComment((client, event) -> {
                        TikTokCommentEvent commentEvent = event;
                        User user = commentEvent.getUser();

                        String profileImageUrl = Optional.ofNullable(user.getPicture())
                                .map(Picture::getLink)
                                .orElse("default-image-url.jpg");

                        List<String> badges = user.getBadges().stream()
                                .filter(b -> b instanceof PictureBadge)
                                .map(b -> ((PictureBadge) b).getPicture().getLink())
                                .filter(url -> url != null && !url.isEmpty())
                                .toList();

                        List<String> attributes = user.getAttributes().stream()
                                .map(UserAttribute::name)
                                .toList();

                        sendMessageToUser(userId, "/topic/comments", Map.of(
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
            activeClients.remove(userId);
            registeredUserId.put(userId, "");
            System.out.println("Stopped monitoring TikTok user for session " + userId);
        } else {
            System.out.println("No active monitoring found for user: " + userId);
        }
    }

    /**
     * Изпраща насочено съобщение към конкретен потребител с помощта на Spring WebSocket STOMP `/user/{userId}/topic/...`
     */
    private void sendMessageToUser(String userId, String topic, Map<String, ?> messageData) {
        try {
            messagingTemplate.convertAndSendToUser(userId, topic, messageData);
        } catch (Exception e) {
            System.out.println("Error sending message to user " + userId + " at " + topic + ": " + e.getMessage());
        }
    }

    @Override
    public void registerNewUserId(String userId) {
        registeredUserId.putIfAbsent(userId, "");
        System.out.println("Session registered: " + userId);
    }
}
