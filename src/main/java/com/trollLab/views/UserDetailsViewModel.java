package com.trollLab.views;

import java.util.List;

public class UserDetailsViewModel {
    private String profileImageUrl;
    private String name;
    private String profileUrl;
    private int totalComments;
    private List<YouTubeCommentViewModel> comments;
    private List<YouTubeCommentViewModel> replies;

    public UserDetailsViewModel() {

    }

    public UserDetailsViewModel(String name, String profileUrl, String profileImageUrl, int totalComments) {
        this.name = name;
        this.profileUrl = profileUrl;
        this.profileImageUrl = profileImageUrl;
        this.totalComments = totalComments;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public UserDetailsViewModel setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserDetailsViewModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public UserDetailsViewModel setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
        return this;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public UserDetailsViewModel setTotalComments(int totalComments) {
        this.totalComments = totalComments;
        return this;
    }

    public List<YouTubeCommentViewModel> getComments() {
        return comments;
    }

    public UserDetailsViewModel setComments(List<YouTubeCommentViewModel> comments) {
        this.comments = comments;
        return this;
    }

    public List<YouTubeCommentViewModel> getReplies() {
        return replies;
    }

    public UserDetailsViewModel setReplies(List<YouTubeCommentViewModel> replies) {
        this.replies = replies;
        return this;
    }
}
