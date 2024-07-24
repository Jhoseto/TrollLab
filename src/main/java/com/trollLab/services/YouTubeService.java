package com.trollLab.services;

import com.trollLab.views.CommentViewModel;

import java.util.List;

public interface YouTubeService {

    List<CommentViewModel> getComments(String videoUrl, String pageToken, String sort);

    String extractVideoId(String videoUrl);

    List<CommentViewModel>getCommentsBySearchingWords(String videoUrl, String pageToken, String sort, String words);
}
