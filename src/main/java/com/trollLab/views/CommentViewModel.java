package com.trollLab.views;

public class CommentViewModel {
    private String text;
    private String authorDisplayName;
    private String publishedAt;
    private int likeCount;


    public CommentViewModel(String text, String authorDisplayName, String publishedAt, int likeCount) {
        this.text = text;
        this.authorDisplayName = authorDisplayName;
        this.publishedAt = publishedAt;
        this.likeCount = likeCount;
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
}
