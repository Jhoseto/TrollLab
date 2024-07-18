package com.trollLab.controllers;

import com.trollLab.views.CommentViewModel;
import com.trollLab.services.serviceImpl.YouTubeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CommentController {

    @Autowired
    private YouTubeServiceImpl youTubeService;

    @GetMapping("/comments")
    public String getComments(@RequestParam("videoUrl") String videoUrl,
                              @RequestParam(value = "pageToken", required = false) String pageToken,
                              Model model) {
        List<CommentViewModel> comments = youTubeService.getComments(videoUrl, pageToken);
        String nextPageToken = null;
        if (!comments.isEmpty() && comments.get(comments.size() - 1).getAuthorDisplayName().equals("nextPageToken")) {
            nextPageToken = comments.remove(comments.size() - 1).getText();
        }
        model.addAttribute("comments", comments);
        model.addAttribute("videoUrl", videoUrl);
        model.addAttribute("nextPageToken", nextPageToken);
        return "comments";
    }
}

