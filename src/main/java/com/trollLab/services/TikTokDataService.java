package com.trollLab.services;

public interface TikTokDataService {
    void clearAllData();

    void addData(String key, Object value);

    Object getData(String key);

    void removeData(String key);
}
