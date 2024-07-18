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


    private final YouTubeServiceImpl youTubeService;

    @Autowired
    public CommentController(YouTubeServiceImpl youTubeService) {
        this.youTubeService = youTubeService;
    }

    @GetMapping("/comments")
    public String getComments(@RequestParam("videoUrl") String videoUrl,
                              @RequestParam(value = "pageToken", required = false) String pageToken,
                              @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                              Model model) {
        List<CommentViewModel> allComments = youTubeService.getComments(videoUrl, null);
        int pageSize = 100;
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allComments.size());

        List<CommentViewModel> paginatedComments = allComments.subList(startIndex, endIndex);
        boolean hasPrevPage = page > 1;
        boolean hasNextPage = endIndex < allComments.size();

        model.addAttribute("comments", paginatedComments);
        model.addAttribute("videoUrl", videoUrl);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasPrevPage", hasPrevPage);
        model.addAttribute("hasNextPage", hasNextPage);

        return "index";
    }
}
