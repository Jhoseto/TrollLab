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
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class YouTubeServiceImpl {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final String COMMENT_THREADS_URL = "https://www.googleapis.com/youtube/v3/commentThreads";
    private static final String COMMENTS_URL = "https://www.googleapis.com/youtube/v3/comments";

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
                    "?part=snippet,replies" +
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
                            snippet.getInt("likeCount"),
                            0 // Инициализация на броя на коментарите
                    );

                    // Добавяне на отговорите, ако има такива
                    JSONArray repliesArray = item.optJSONObject("replies") != null ? item.optJSONObject("replies").optJSONArray("comments") : null;
                    if (repliesArray != null) {
                        List<CommentViewModel> replies = new ArrayList<>();
                        for (int j = 0; j < repliesArray.length(); j++) {
                            JSONObject replyObject = repliesArray.getJSONObject(j);
                            JSONObject replySnippet = replyObject.getJSONObject("snippet");

                            CommentViewModel reply = new CommentViewModel(
                                    replySnippet.getString("textDisplay"),
                                    replySnippet.getString("authorDisplayName"),
                                    formatDate(replySnippet.getString("publishedAt")),
                                    replySnippet.getInt("likeCount"),
                                    0 // Инициализация на броя на коментарите
                            );
                            replies.add(reply);
                        }
                        comment.setReplies(replies);
                    }

                    comments.add(comment);
                }

                nextPageToken = jsonResponse.optString("nextPageToken", null);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to parse YouTube API response");
            }

        } while (nextPageToken != null);

        // Събиране на броя на коментарите на потребителите
        Map<String, Long> userCommentCount = comments.stream()
                .collect(Collectors.groupingBy(CommentViewModel::getAuthorDisplayName, Collectors.counting()));

        comments.forEach(comment -> {
            int totalComments = userCommentCount.getOrDefault(comment.getAuthorDisplayName(), 0L).intValue();
            comment.setTotalComments(totalComments);
        });

        comments.forEach(comment -> {
            // URL на профила на потребителя в YouTube
            String profileUrl = "https://www.youtube.com/" + comment.getAuthorDisplayName();
            comment.setAuthorProfileUrl(profileUrl);
        });

        // Apply sorting based on the selected option
        switch (sort) {
            case "newest":
                comments.sort(Comparator.comparing(CommentViewModel::getPublishedAt).reversed());
                break;
            case "oldest":
                Collections.sort(comments, Comparator.comparing(CommentViewModel::getPublishedAt));
                break;
            case "most-liked":
                Collections.sort(comments, Comparator.comparingInt(CommentViewModel::getLikeCount).reversed());
                break;
            case "most-comments":
                // Sort users by comment count descending
                comments.sort((c1, c2) -> {
                    long count1 = userCommentCount.getOrDefault(c1.getAuthorDisplayName(), 0L);
                    long count2 = userCommentCount.getOrDefault(c2.getAuthorDisplayName(), 0L);
                    return Long.compare(count2, count1); // Descending order
                });
                break;
            default:
                // Default sorting by newest
                comments.sort(Comparator.comparing(CommentViewModel::getPublishedAt).reversed());
                break;
        }

        return comments;
    }


    private String extractVideoId(String videoUrl) {
        String videoId = null;
        if (videoUrl.contains("youtube.com")) {

            String[] urlParts = videoUrl.split("v=");
            if (urlParts.length > 1) {
                videoId = urlParts[1].split("&")[0];
            }
        } else if (videoUrl.contains("youtu.be")) {

            int indexOfEqualSign = videoUrl.indexOf('=');
            if (indexOfEqualSign != -1) {
                videoId = videoUrl.substring(indexOfEqualSign + 1);
            }
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
