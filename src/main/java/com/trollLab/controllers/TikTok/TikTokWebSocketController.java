package com.trollLab.controllers.TikTok;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.UUID;

@Controller
public class TikTokWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentMap<String, String> userSessions = new ConcurrentHashMap<>();

    public TikTokWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/register")
    public String registerUser() {
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId;
    }

    @MessageMapping("/subscribe")
    public void subscribeToUser(String request) {
        String[] parts = request.split(":");
        if (parts.length == 2) {
            String uniqueId = parts[0];
            String tiktokUser = parts[1];
            userSessions.put(uniqueId, tiktokUser);
        }
    }

    public void sendLiveData(String tiktokUser, Object liveData) {
        userSessions.forEach((sessionId, user) -> {
            if (user.equals(tiktokUser)) {
                messagingTemplate.convertAndSend("/topic/live/" + sessionId, liveData);
            }
        });
    }
}
