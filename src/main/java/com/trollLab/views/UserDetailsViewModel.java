package com.trollLab.views;

import java.util.List;

public class UserDetailsViewModel {
    private String profileImageUrl;
    private String name;
    private String profileUrl;
    private int totalComments;
    private List<CommentViewModel> comments;
    private List<CommentViewModel> replies;

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

    public List<CommentViewModel> getComments() {
        return comments;
    }

    public UserDetailsViewModel setComments(List<CommentViewModel> comments) {
        this.comments = comments;
        return this;
    }

    public List<CommentViewModel> getReplies() {
        return replies;
    }

    public UserDetailsViewModel setReplies(List<CommentViewModel> replies) {
        this.replies = replies;
        return this;
    }
}
