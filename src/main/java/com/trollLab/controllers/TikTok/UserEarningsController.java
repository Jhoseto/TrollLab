package com.trollLab.controllers.TikTok;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserEarningsController {

    @GetMapping("/earnings")
    public String showEarningsPage() {
        return "earnings";
    }
}
