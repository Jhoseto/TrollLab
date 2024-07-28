package com.trollLab.services.serviceImpl;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Comment;
import com.restfb.types.User;
import com.trollLab.services.FacebookService;
import com.trollLab.views.FacebookCommentViewModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FacebookServiceImpl implements FacebookService {
    @Value("${facebook.access.token}")
    private String accessToken;

    @Override
    public List<FacebookCommentViewModel> getCommentsFromPost(String postId) {
        FacebookClient facebookClient = new DefaultFacebookClient(accessToken, com.restfb.Version.LATEST);
        List<FacebookCommentViewModel> allComments = new ArrayList<>();

        Connection<Comment> commentsConnection = facebookClient.fetchConnection(
                postId + "/comments", Comment.class, Parameter.with("limit", 100));

        for (List<Comment> comments : commentsConnection) {
            for (Comment comment : comments) {
                allComments.add(convertToFacebookComment(comment, facebookClient));
            }
        }
        return allComments;
    }

    private FacebookCommentViewModel convertToFacebookComment(Comment comment, FacebookClient facebookClient) {
        FacebookCommentViewModel fbComment = new FacebookCommentViewModel();
        fbComment.setId(comment.getId());
        fbComment.setMessage(comment.getMessage());
        fbComment.setCreatedTime(comment.getCreatedTime());

        // Конвертиране на `From` в `User`
        User user = new User();
        user.setId(comment.getFrom().getId());
        user.setName(comment.getFrom().getName());

        fbComment.setFrom(user);
        fbComment.setLikeCount(comment.getLikeCount());
        fbComment.setCommentCount(comment.getCommentCount());
        fbComment.setCanComment(comment.getCanComment());
        fbComment.setCanRemove(comment.getCanRemove());
        fbComment.setCanHide(comment.getCanHide());
        fbComment.setCanLike(comment.getCanLike());
        fbComment.setCanReplyPrivately(comment.getCanReplyPrivately());
        fbComment.setUserLikes(comment.getUserLikes());

        // Извличане и задаване на отговори
        List<FacebookCommentViewModel> replies = new ArrayList<>();
        if (comment.getCommentCount() > 0) {
            Connection<Comment> repliesConnection = facebookClient.fetchConnection(
                    comment.getId() + "/comments", Comment.class, Parameter.with("limit", 100));
            for (List<Comment> replyList : repliesConnection) {
                for (Comment reply : replyList) {
                    replies.add(convertToFacebookComment(reply, facebookClient));
                }
            }
        }
        fbComment.setReplies(replies);

        return fbComment;
    }
}
