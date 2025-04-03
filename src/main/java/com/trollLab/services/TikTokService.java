package com.trollLab.services;

public interface TikTokService {

    void startMonitoring(String userId, String tiktokUser) throws Exception;

    void stopMonitoring(String userId);

    void registerNewUserId(String userId);
}
