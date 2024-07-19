package com.trollLab.views;

import java.util.List;

public class CommentViewModel {
    private String id;
    private String text;
    private String authorDisplayName;
    private String publishedAt;
    private int likeCount;
    private String channelId;
    private String videoId;
    private boolean canReply;
    private int totalReplyCount;
    private boolean isPublic;
    private List<CommentViewModel> replies;
    private int totalComments;
    private String authorProfileUrl;
    private boolean isTopLevelComment;

    public CommentViewModel(String text, String authorDisplayName, String publishedAt, int likeCount, int totalComments) {
        this.text = text;
        this.authorDisplayName = authorDisplayName;
        this.publishedAt = publishedAt;
        this.likeCount = likeCount;
        this.totalComments = totalComments;
    }

    public String getId() {
        return id;
    }

    public CommentViewModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getText() {
        return text;
    }

    public CommentViewModel setText(String text) {
        this.text = text;
        return this;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public CommentViewModel setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
        return this;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public CommentViewModel setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
        return this;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public CommentViewModel setLikeCount(int likeCount) {
        this.likeCount = likeCount;
        return this;
    }

    public String getChannelId() {
        return channelId;
    }

    public CommentViewModel setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getVideoId() {
        return videoId;
    }

    public CommentViewModel setVideoId(String videoId) {
        this.videoId = videoId;
        return this;
    }

    public boolean isCanReply() {
        return canReply;
    }

    public CommentViewModel setCanReply(boolean canReply) {
        this.canReply = canReply;
        return this;
    }

    public int getTotalReplyCount() {
        return totalReplyCount;
    }

    public CommentViewModel setTotalReplyCount(int totalReplyCount) {
        this.totalReplyCount = totalReplyCount;
        return this;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public CommentViewModel setPublic(boolean aPublic) {
        isPublic = aPublic;
        return this;
    }

    public List<CommentViewModel> getReplies() {
        return replies;
    }

    public CommentViewModel setReplies(List<CommentViewModel> replies) {
        this.replies = replies;
        return this;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public CommentViewModel setTotalComments(int totalComments) {
        this.totalComments = totalComments;
        return this;
    }

    public String getAuthorProfileUrl() {
        return authorProfileUrl;
    }

    public CommentViewModel setAuthorProfileUrl(String authorProfileUrl) {
        this.authorProfileUrl = authorProfileUrl;
        return this;
    }

    public boolean isTopLevelComment() {
        return isTopLevelComment;
    }

    public void setIsTopLevelComment(boolean isTopLevelComment) {
        this.isTopLevelComment = isTopLevelComment;
    }
}
