package com.trollLab.services.serviceImpl;

import com.trollLab.services.YouTubeService;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class YouTubeServiceImpl implements YouTubeService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final String COMMENT_THREADS_URL = "https://www.googleapis.com/youtube/v3/commentThreads";

    public List<CommentViewModel> getComments(String videoUrl, String pageToken, String sort) {
        String videoId = extractVideoId(videoUrl);
        if (videoId == null) {
            throw new IllegalArgumentException("Invalid YouTube video URL: " + videoUrl);
        }

        RestTemplate restTemplate = new RestTemplate();
        List<CommentViewModel> allComments = new ArrayList<>();
        Map<String, CommentViewModel> commentMap = new HashMap<>();
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
                            0 // Инициализиране на общия брой коментари
                    );

                    // Добавяне на допълнителна информация от отговора
                    comment.setAuthorProfileUrl(snippet.getString("authorChannelUrl"));
                    comment.setAuthorProfileImageUrl(snippet.getString("authorProfileImageUrl"));

                    String commentId = item.getJSONObject("snippet").getJSONObject("topLevelComment").getString("id");
                    comment.setIsTopLevelComment(true);
                    commentMap.put(commentId, comment);

                    // Добавяне на отговори, ако има такива
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
                                    0 // Инициализиране на общия брой коментари за отговорите
                            );

                            reply.setIsTopLevelComment(false);
                            reply.setAuthorProfileUrl(replySnippet.getString("authorChannelUrl"));
                            reply.setAuthorProfileImageUrl(replySnippet.getString("authorProfileImageUrl"));
                            replies.add(reply);

                            // Запазване на отговорите в commentMap
                            String replyId = replyObject.getString("id");
                            commentMap.put(replyId, reply);
                        }
                        comment.setReplies(replies);
                    }
                }

                nextPageToken = jsonResponse.optString("nextPageToken", null);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to parse YouTube API response");
            }

        } while (nextPageToken != null);

        // Събиране на общия брой коментари (включително отговори) за всеки потребител
        Map<String, Integer> userCommentCount = new HashMap<>();

        // Броене на всички главни коментари и отговори
        for (CommentViewModel comment : commentMap.values()) {
            String author = comment.getAuthorDisplayName();
            userCommentCount.put(author, userCommentCount.getOrDefault(author, 0) + 1);

            if (comment.getReplies() != null) {
                for (CommentViewModel reply : comment.getReplies()) {
                    userCommentCount.put(reply.getAuthorDisplayName(), userCommentCount.getOrDefault(reply.getAuthorDisplayName(), 0) + 1);
                }
            }
        }

        // Задаване на общия брой коментари, включително отговори за всеки коментар
        commentMap.values().forEach(comment -> {
            int totalComments = userCommentCount.getOrDefault(comment.getAuthorDisplayName(), 0);
            comment.setTotalComments(totalComments);
        });

        // Приложете сортиране според избраната опция
        List<CommentViewModel> commentList = new ArrayList<>(commentMap.values());

        switch (sort) {
            case "oldest":
                commentList.sort(Comparator.comparing(CommentViewModel::getPublishedAt));
                break;
            case "most-liked":
                commentList.sort(Comparator.comparingInt(CommentViewModel::getLikeCount).reversed());
                break;
            case "most-comments":
                // Сортиране по потребители с най-много коментари и показване на отговорите на техните коментари
                commentList.sort((c1, c2) -> {
                    int count1 = userCommentCount.getOrDefault(c1.getAuthorDisplayName(), 0);
                    int count2 = userCommentCount.getOrDefault(c2.getAuthorDisplayName(), 0);
                    return Integer.compare(count2, count1); // Сортиране по низходящ ред
                });

                List<CommentViewModel> sortedCommentsWithReplies = new ArrayList<>();
                for (CommentViewModel comment : commentList) {
                    sortedCommentsWithReplies.add(comment);
                    if (comment.getReplies() != null) {
                        sortedCommentsWithReplies.addAll(comment.getReplies());
                    }
                }
                commentList = sortedCommentsWithReplies;
                break;
            case "only-replied":
                commentList = commentList.stream()
                        .filter(comment -> comment.getReplies() != null && !comment.getReplies().isEmpty())
                        .collect(Collectors.toList());
                commentList.sort(Comparator.comparing(CommentViewModel::getPublishedAt).reversed());
                break;
            case "newest":
            default:
                commentList.sort(Comparator.comparing(CommentViewModel::getPublishedAt).reversed());
                break;
        }

        return commentList;
    }


    private static String extractVideoId(String videoUrl) {
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
