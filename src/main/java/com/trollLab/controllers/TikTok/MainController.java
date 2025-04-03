package com.trollLab.controllers.TikTok;

import com.trollLab.services.TikTokService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class MainController {
    private final TikTokService tikTokService;

    @Autowired
    public MainController(TikTokService tikTokService) {
        this.tikTokService = tikTokService;
    }

    @GetMapping("/")
    public String setUniqueUserId(HttpServletRequest request, HttpServletResponse response) {
        // Проверка дали бисквитката вече съществува
        String uniqueUserId = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("uniqueUserId")) {
                    uniqueUserId = cookie.getValue();
                    break;
                }
            }
        }
        if (uniqueUserId == null) {
            // Ако бисквитката не съществува, генерирай ново уникално ID
            uniqueUserId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie("uniqueUserId", uniqueUserId);
            cookie.setMaxAge(60 * 60 * 24 * 365); // Бисквитката да бъде валидна 1 година
            response.addCookie(cookie);
            System.out.println("New UserId is registered "+uniqueUserId);
        }
        return "index";
    }
}
