package com.trollLab.services;

import com.trollLab.views.CommentViewModel;

import java.util.List;

public interface YouTubeService {

    List<CommentViewModel> getComments(String videoUrl, String pageToken, String sort);
}
