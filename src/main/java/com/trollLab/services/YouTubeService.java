package com.trollLab.services;

import com.trollLab.views.YouTubeCommentViewModel;

import java.util.List;

public interface YouTubeService {

    List<YouTubeCommentViewModel> getComments(String videoUrl, String pageToken, String sort);

    String extractVideoId(String videoUrl);

    List<YouTubeCommentViewModel>getCommentsBySearchingWords(String videoUrl, String pageToken, String sort, String words);

}
