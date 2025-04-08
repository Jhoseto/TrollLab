package com.trollLab.controllers.TikTok;

import com.trollLab.services.TikTokDataService;
import com.trollLab.services.TikTokService;
import io.github.jwdeveloper.tiktok.TikTokLive;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TikTokController {

    private final TikTokService tikTokService;
    private final TikTokDataService dataService;
    private final SimpMessagingTemplate messagingTemplate;

    public TikTokController(TikTokService tikTokService,
                            TikTokDataService dataService,
                            SimpMessagingTemplate messagingTemplate) {
        this.tikTokService = tikTokService;
        this.dataService = dataService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/tiktokAnalyze")
    public String analyzeTikTokUser(@CookieValue(value = "uniqueUserId", defaultValue = "") String userId,
                                    @RequestParam("tiktokUser") String tiktokUser,
                                    RedirectAttributes redirectAttributes) {
        if (userId.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User ID not found in cookies.");
            return "redirect:/tiktok-live-monitor";
        }

        if (tiktokUser == null || tiktokUser.trim().isEmpty()) {
            tikTokService.stopMonitoring(userId);
            redirectAttributes.addFlashAttribute("error", "Invalid TikTok username.");
            return "redirect:/tiktok-live-monitor";
        }

        if (!TikTokLive.isLiveOnline(tiktokUser)) {
            tikTokService.stopMonitoring(userId);
            redirectAttributes.addFlashAttribute("error", "TikTok username is Offline.");
            System.out.printf("TikTok User %s is Offline.%n", tiktokUser);
            return "redirect:/tiktok-live-monitor";
        }

        try {
            tikTokService.startMonitoring(userId, tiktokUser);
            redirectAttributes.addFlashAttribute("message", "Monitoring started for user: " + tiktokUser);

            // Изпращаме съобщение само към канала на потребителя
            sendMessageToUser(userId, tiktokUser, "Monitoring started for: " + tiktokUser);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to start monitoring: " + e.getMessage());
            tikTokService.stopMonitoring(userId);
            return "redirect:/tiktok-live-monitor";
        }

        return "redirect:/tiktok-live-monitor";
    }

    @GetMapping("/api/clear-data")
    public String clearData(@CookieValue(value = "uniqueUserId", defaultValue = "") String userId) {
        if (userId != null) {
            tikTokService.stopMonitoring(userId);
            dataService.clearAllData();
        }
        return "redirect:/tiktok-live-monitor";
    }

    @GetMapping("/tiktok-live-monitor")
    public String tiktokMonitor() {
        return "tiktok-live-monitor";
    }

    // Метод за изпращане на съобщения към уникален канал за конкретния потребител
    private void sendMessageToUser(String uniqueUserId, String tiktokUser, String message) {
        String channel = "/user/" + uniqueUserId + "/topic/" + tiktokUser;
        messagingTemplate.convertAndSend(channel, message);
    }
}
