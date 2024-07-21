package com.trollLab.views;

import java.util.List;

public class CommentViewModel {
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
    private List<CommentViewModel> replies;
    private int totalComments;
    private String authorProfileUrl;
    private boolean isTopLevelComment;
    private String parentId; // Добавено поле за ID на родителския коментар
    private String parentCommentText;

    public CommentViewModel(String text, String authorDisplayName, String publishedAt, int likeCount, int totalComments) {
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

    public String getAuthorProfileImageUrl() {
        return authorProfileImageUrl;
    }

    public CommentViewModel setAuthorProfileImageUrl(String authorProfileImageUrl) {
        this.authorProfileImageUrl = authorProfileImageUrl;
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

    public CommentViewModel setIsTopLevelComment(boolean isTopLevelComment) {
        this.isTopLevelComment = isTopLevelComment;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public CommentViewModel setParentId(String parentId) {
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
