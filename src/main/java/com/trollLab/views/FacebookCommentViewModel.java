package com.trollLab.views;

import com.restfb.types.User;
import java.util.Date;
import java.util.List;

public class FacebookCommentViewModel {
    private String id;
    private String message;
    private Date createdTime;
    private User from;
    private Long likeCount;
    private Long commentCount;
    private boolean canComment;
    private boolean canRemove;
    private boolean canHide;
    private boolean canLike;
    private boolean canReplyPrivately;
    private boolean userLikes;
    private List<FacebookCommentViewModel> replies;


    public String getId() {
        return id;
    }

    public FacebookCommentViewModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public FacebookCommentViewModel setMessage(String message) {
        this.message = message;
        return this;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public FacebookCommentViewModel setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isCanComment() {
        return canComment;
    }

    public void setCanComment(boolean canComment) {
        this.canComment = canComment;
    }

    public boolean isCanRemove() {
        return canRemove;
    }

    public void setCanRemove(boolean canRemove) {
        this.canRemove = canRemove;
    }

    public boolean isCanHide() {
        return canHide;
    }

    public void setCanHide(boolean canHide) {
        this.canHide = canHide;
    }

    public boolean isCanLike() {
        return canLike;
    }

    public void setCanLike(boolean canLike) {
        this.canLike = canLike;
    }

    public boolean isCanReplyPrivately() {
        return canReplyPrivately;
    }

    public void setCanReplyPrivately(boolean canReplyPrivately) {
        this.canReplyPrivately = canReplyPrivately;
    }

    public boolean isUserLikes() {
        return userLikes;
    }

    public void setUserLikes(boolean userLikes) {
        this.userLikes = userLikes;
    }

    public List<FacebookCommentViewModel> getReplies() {
        return replies;
    }

    public FacebookCommentViewModel setReplies(List<FacebookCommentViewModel> replies) {
        this.replies = replies;
        return this;
    }
}
