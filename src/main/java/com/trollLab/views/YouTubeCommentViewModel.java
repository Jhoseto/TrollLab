package com.trollLab.views;

import java.util.List;

public class YouTubeCommentViewModel {
    private String id;
    private String text;
    private String authorDisplayName;
    private String authorProfileImageUrl;
    private String publishedAt;
    private int likeCount;
    private String channelId;
    private String videoId;
    private boolean canReply;
    private int totalReplyCount;
    private boolean isPublic;
    private List<YouTubeCommentViewModel> replies;
    private int totalComments;
    private String authorProfileUrl;
    private boolean isTopLevelComment;
    private String parentId;
    private String parentCommentText;

    public YouTubeCommentViewModel(String text, String authorDisplayName, String publishedAt, int likeCount, int totalComments) {
        this.text = text;
        this.authorDisplayName = authorDisplayName;
        this.publishedAt = publishedAt;
        this.likeCount = likeCount;
        this.totalComments = totalComments;
    }

    // Гетъри и сетъри

    public String getId() {
        return id;
    }

    public YouTubeCommentViewModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getText() {
        return text;
    }

    public YouTubeCommentViewModel setText(String text) {
        this.text = text;
        return this;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public YouTubeCommentViewModel setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
        return this;
    }

    public String getAuthorProfileImageUrl() {
        return authorProfileImageUrl;
    }

    public YouTubeCommentViewModel setAuthorProfileImageUrl(String authorProfileImageUrl) {
        this.authorProfileImageUrl = authorProfileImageUrl;
        return this;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public YouTubeCommentViewModel setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
        return this;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public YouTubeCommentViewModel setLikeCount(int likeCount) {
        this.likeCount = likeCount;
        return this;
    }

    public String getChannelId() {
        return channelId;
    }

    public YouTubeCommentViewModel setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getVideoId() {
        return videoId;
    }

    public YouTubeCommentViewModel setVideoId(String videoId) {
        this.videoId = videoId;
        return this;
    }

    public boolean isCanReply() {
        return canReply;
    }

    public YouTubeCommentViewModel setCanReply(boolean canReply) {
        this.canReply = canReply;
        return this;
    }

    public int getTotalReplyCount() {
        return totalReplyCount;
    }

    public YouTubeCommentViewModel setTotalReplyCount(int totalReplyCount) {
        this.totalReplyCount = totalReplyCount;
        return this;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public YouTubeCommentViewModel setPublic(boolean aPublic) {
        isPublic = aPublic;
        return this;
    }

    public List<YouTubeCommentViewModel> getReplies() {
        return replies;
    }

    public YouTubeCommentViewModel setReplies(List<YouTubeCommentViewModel> replies) {
        this.replies = replies;
        return this;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public YouTubeCommentViewModel setTotalComments(int totalComments) {
        this.totalComments = totalComments;
        return this;
    }

    public String getAuthorProfileUrl() {
        return authorProfileUrl;
    }

    public YouTubeCommentViewModel setAuthorProfileUrl(String authorProfileUrl) {
        this.authorProfileUrl = authorProfileUrl;
        return this;
    }

    public boolean isTopLevelComment() {
        return isTopLevelComment;
    }

    public YouTubeCommentViewModel setIsTopLevelComment(boolean isTopLevelComment) {
        this.isTopLevelComment = isTopLevelComment;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public YouTubeCommentViewModel setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getParentCommentText() {
        return parentCommentText;
    }

    public void setParentCommentText(String parentCommentText) {
        this.parentCommentText = parentCommentText;
    }
}
