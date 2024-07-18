package com.trollLab.services.serviceImpl;

import com.trollLab.views.CommentViewModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeServiceImpl {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final String COMMENT_THREADS_URL = "https://www.googleapis.com/youtube/v3/commentThreads";

    public List<CommentViewModel> getComments(String videoUrl, String pageToken) {
        String videoId = extractVideoId(videoUrl);
        if (videoId == null) {
            throw new IllegalArgumentException("Invalid YouTube video URL: " + videoUrl);
        }

        RestTemplate restTemplate = new RestTemplate();
        List<CommentViewModel> comments = new ArrayList<>();
        String url = COMMENT_THREADS_URL +
                "?part=snippet" +
                "&videoId=" + videoId +
                "&key=" + apiKey +
                "&maxResults=100" +
                (pageToken != null && !pageToken.isEmpty() ? "&pageToken=" + pageToken : "");

        String response = restTemplate.getForObject(url, String.class);

        try {
            assert response != null;
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray items = jsonResponse.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject snippet = item.getJSONObject("snippet").getJSONObject("topLevelComment").getJSONObject("snippet");
                String text = snippet.getString("textDisplay");
                String authorDisplayName = snippet.getString("authorDisplayName");
                String publishedAt = snippet.getString("publishedAt");
                int likeCount = snippet.getInt("likeCount");

                // Преобразуване на publishedAt в желания формат
                LocalDateTime publishedDateTime = LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME);
                String formattedPublishedAt = publishedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                CommentViewModel comment = new CommentViewModel(text, authorDisplayName, formattedPublishedAt, likeCount);
                comments.add(comment);
            }

            String nextPageToken = jsonResponse.optString("nextPageToken", null);
            if (nextPageToken != null) {
                comments.add(new CommentViewModel("nextPageToken", nextPageToken, "", 0)); // Добавяне на nextPageToken като коментар
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return comments;
    }

    private String extractVideoId(String videoUrl) {
        String videoId = null;
        try {
            String[] query = videoUrl.split("\\?");
            if (query.length > 1) {
                String[] queryParams = query[1].split("&");
                for (String param : queryParams) {
                    String[] pair = param.split("=");
                    if (pair.length > 1 && pair[0].equals("v")) {
                        videoId = pair[1];
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoId;
    }
}
