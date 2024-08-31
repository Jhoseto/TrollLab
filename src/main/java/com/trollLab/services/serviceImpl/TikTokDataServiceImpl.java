package com.trollLab.services.serviceImpl;

import com.trollLab.services.TikTokDataService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TikTokDataServiceImpl implements TikTokDataService {

    private final ConcurrentHashMap<String, Object> storedData = new ConcurrentHashMap<>();

    @Override
    public void clearAllData() {
        storedData.clear();
    }

    @Override
    public void addData(String key, Object value) {
        storedData.put(key, value);
    }

    @Override
    public Object getData(String key) {
        return storedData.get(key);
    }

    @Override
    public void removeData(String key) {
        storedData.remove(key);
    }
}
