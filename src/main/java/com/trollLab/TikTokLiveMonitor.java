package com.trollLab;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.data.models.users.User;

import java.time.Duration;
import java.util.logging.Level;

public class TikTokLiveMonitor {

    public static void main(String[] args) {
        // TikTok живо ID
        String liveId = "_natali_4312";

        TikTokLive.newClient(liveId)
                .onGift((liveClient, event) -> {
                    Gift gift = event.getGift();
                    String message = getGiftMessage(gift);
                    System.out.println(event.getUser().getProfileName() + " изпраща " + message);
                })
                .onRoomInfo((liveClient, event) -> {
                    TikTokRoomInfo roomInfo = (TikTokRoomInfo) event.getRoomInfo();
                    System.out.println("Room Id: " + roomInfo.getRoomId());
                    System.out.println("Likes: " + roomInfo.getLikesCount());
                    System.out.println("Viewers: " + roomInfo.getViewersCount());
                })
                .onJoin((liveClient, event) -> {
                    User user = event.getUser();
                    System.out.println(user.getProfileName() + " се присъедини към стрийма!");
                })
                .onConnected((liveClient, event) -> {
                    System.out.println("Свързан към живото предаване!");
                })
                .onError((liveClient, event) -> {
                    System.out.println("Грешка! " + event.getException().getMessage());
                })
                .onComment((liveClient, event) -> {
                    TikTokCommentEvent commentEvent = event;
                    System.out.println(commentEvent.getUser().getProfileName() + " каза: " + commentEvent.getText());
                })
                .configure(settings -> {
                    settings.setHostName(liveId);
                    settings.setClientLanguage("bg"); // Настройка на езика
                    settings.setLogLevel(Level.ALL); // Ниво на логовете
                    settings.setPrintToConsole(true); // Печати всички логове на конзолата
                    settings.setRetryOnConnectionFailure(true); // Опитва се отново при грешка в свързването
                    settings.setRetryConnectionTimeout(Duration.ofSeconds(1));// Време преди следващото свързване

                    // Опционално: Може да зададете sessionId ако имате проблеми
                    settings.setSessionId("YOUR_SESSION_ID");

                    // Опционално: Може да зададете RoomId ако имате проблеми с HostId
                    settings.setRoomId("YOUR_ROOM_ID");
                })
                .buildAndConnect();
    }

    private static String getGiftMessage(Gift gift) {
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
