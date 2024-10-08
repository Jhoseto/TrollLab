package com.trollLab.controllers;

import com.trollLab.services.TikTokDataService;
import com.trollLab.services.TikTokService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TikTokController {

    private final TikTokService tikTokService;
    private final TikTokDataService dataService;


    public TikTokController(TikTokService tikTokService,
                            TikTokDataService dataService) {
        this.tikTokService = tikTokService;
        this.dataService = dataService;
    }

    @GetMapping("/tiktokAnalyze")
    public String analyzeTikTokUser(@RequestParam("tiktokUser") String tiktokUser,
                                    RedirectAttributes redirectAttributes) {
        if (tiktokUser == null || tiktokUser.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid TikTok username.");
            return "tiktok-live-monitor";
        }
        try {
            tikTokService.startMonitoring(tiktokUser);
            redirectAttributes.addFlashAttribute("message", "Monitoring started for user: " + tiktokUser);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to start monitoring: " + e.getMessage());
        }
        return "tiktok-live-monitor";
    }

    @GetMapping("/api/clear-data")
    public String clearData() {
        dataService.clearAllData();
        return "index";
    }

}
