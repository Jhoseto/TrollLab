package com.trollLab.services.serviceImpl;

import com.trollLab.views.CommentViewModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class YouTubeServiceImpl {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final String COMMENT_THREADS_URL = "https://www.googleapis.com/youtube/v3/commentThreads";

    public List<CommentViewModel> getComments(String videoUrl, String pageToken, String sort) {
        String videoId = extractVideoId(videoUrl);
        if (videoId == null) {
            throw new IllegalArgumentException("Invalid YouTube video URL: " + videoUrl);
        }

        RestTemplate restTemplate = new RestTemplate();
        List<CommentViewModel> comments = new ArrayList<>();
        String nextPageToken = pageToken;

        do {
            String url = COMMENT_THREADS_URL +
                    "?part=snippet" +
                    "&videoId=" + videoId +
                    "&key=" + apiKey +
                    "&maxResults=100" +
                    (nextPageToken != null && !nextPageToken.isEmpty() ? "&pageToken=" + nextPageToken : "");

            String response = restTemplate.getForObject(url, String.class);

            try {
                assert response != null;
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray items = jsonResponse.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject snippet = item.getJSONObject("snippet").getJSONObject("topLevelComment").getJSONObject("snippet");

                    CommentViewModel comment = new CommentViewModel(
                            snippet.getString("textDisplay"),
                            snippet.getString("authorDisplayName"),
                            formatDate(snippet.getString("publishedAt")),
                            snippet.getInt("likeCount")
                    );
                    comments.add(comment);
                }

                nextPageToken = jsonResponse.optString("nextPageToken", null);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to parse YouTube API response");
            }

        } while (nextPageToken != null);

        // Apply sorting based on the selected option
        switch (sort) {
            case "newest":
                Collections.sort(comments, Comparator.comparing(CommentViewModel::getPublishedAt).reversed());
                break;
            case "oldest":
                Collections.sort(comments, Comparator.comparing(CommentViewModel::getPublishedAt));
                break;
            case "most-liked":
                Collections.sort(comments, Comparator.comparingInt(CommentViewModel::getLikeCount).reversed());
                break;
            default:
                // Default sorting by newest
                Collections.sort(comments, Comparator.comparing(CommentViewModel::getPublishedAt).reversed());
                break;
        }

        return comments;
    }

    private String extractVideoId(String videoUrl) {
        String videoId = null;
        if (videoUrl.contains("youtube.com")) {
            videoId = videoUrl.split("v=")[1];
            int ampersandPosition = videoId.indexOf('&');
            if (ampersandPosition != -1) {
                videoId = videoId.substring(0, ampersandPosition);
            }
        } else if (videoUrl.contains("youtu.be")) {
            videoId = videoUrl.substring(videoUrl.lastIndexOf('/') + 1);
        }
        return videoId;
    }

    private String formatDate(String date) {
        LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
        ZoneId zoneId = ZoneId.of("Europe/Sofia");
        ZonedDateTime zonedDateTime = dateTime.atZone(zoneId);
        ZonedDateTime adjustedTime = zonedDateTime.plusHours(3);
        return adjustedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
