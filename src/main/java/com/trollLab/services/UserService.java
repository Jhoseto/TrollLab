package com.trollLab.services;

import com.trollLab.views.UserDetailsViewModel;

import java.util.List;

public interface UserService {

    UserDetailsViewModel getUserProfile(String videoUrl, String userId);

    String formatDate(String date);
}
