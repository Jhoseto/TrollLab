package com.trollLab.services;

import com.trollLab.views.FacebookCommentViewModel;
import java.util.List;

public interface FacebookService {
    List<FacebookCommentViewModel> getCommentsFromPost(String postId);
}
