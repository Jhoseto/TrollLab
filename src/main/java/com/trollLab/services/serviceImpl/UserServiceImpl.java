package com.trollLab.services.serviceImpl;

import com.trollLab.services.UserService;
import com.trollLab.views.YouTubeCommentViewModel;
import com.trollLab.views.UserDetailsViewModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final String COMMENT_THREADS_URL = "https://www.googleapis.com/youtube/v3/commentThreads";

    @Override
    public UserDetailsViewModel getUserProfile(String videoUrl, String userId) {
        List<YouTubeCommentViewModel> allComments = new ArrayList<>();
        Map<String, YouTubeCommentViewModel> topLevelComments = new HashMap<>();

        String videoId = extractVideoId(videoUrl);
        if (videoId == null) {
            throw new IllegalArgumentException("Invalid YouTube video URL: " + videoUrl);
        }

        RestTemplate restTemplate = new RestTemplate();
        String nextPageToken = null;

        do {
            String url = COMMENT_THREADS_URL +
                    "?part=snippet,replies" +
                    "&videoId=" + videoId +
                    "&key=" + apiKey +
                    "&maxResults=100" +
                    (nextPageToken != null ? "&pageToken=" + nextPageToken : "");

            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                throw new RuntimeException("Failed to retrieve comments from YouTube API");
            }

            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray items = jsonResponse.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject snippet = item.getJSONObject("snippet").getJSONObject("topLevelComment").getJSONObject("snippet");

                    YouTubeCommentViewModel comment = new YouTubeCommentViewModel(
                            snippet.getString("textDisplay"),
                            snippet.getString("authorDisplayName"),
                            formatDate(snippet.getString("publishedAt")),
                            snippet.getInt("likeCount"),
                            0
                    );

                    String authorID = snippet.getString("authorDisplayName");

                    comment.setAuthorDisplayName(authorID);
                    comment.setAuthorProfileImageUrl(snippet.optString("authorProfileImageUrl", null));
                    comment.setVideoId(videoId);
                    comment.setIsTopLevelComment(true);
                    comment.setId(item.getJSONObject("snippet").getJSONObject("topLevelComment").getString("id"));
                    comment.setParentId(null);
                    comment.setParentCommentText(null); // Няма parentCommentText за топ коментари
                    comment.setAuthorProfileUrl("https://www.youtube.com/" + authorID);

                    allComments.add(comment);
                    topLevelComments.put(comment.getId(), comment); // Ensure we store top-level comments

                    JSONArray repliesArray = item.optJSONObject("replies") != null ? item.optJSONObject("replies").optJSONArray("comments") : null;
                    if (repliesArray != null) {
                        for (int j = 0; j < repliesArray.length(); j++) {
                            JSONObject replyObject = repliesArray.getJSONObject(j);
                            JSONObject replySnippet = replyObject.getJSONObject("snippet");

                            String replyAuthorId = replySnippet.getString("authorDisplayName");

                            YouTubeCommentViewModel reply = new YouTubeCommentViewModel(
                                    replySnippet.getString("textDisplay"),
                                    replySnippet.getString("authorDisplayName"),
                                    formatDate(replySnippet.getString("publishedAt")),
                                    replySnippet.getInt("likeCount"),
                                    0
                            );

                            reply.setAuthorDisplayName(replyAuthorId);
                            reply.setAuthorProfileImageUrl(replySnippet.optString("authorProfileImageUrl", null));
                            reply.setVideoId(videoId);
                            reply.setIsTopLevelComment(false);
                            reply.setId(replyObject.getString("id"));
                            reply.setParentId(replySnippet.getString("parentId"));

                            // Намерете родителския коментар и задайте текста му на отговора
                            YouTubeCommentViewModel parentComment = topLevelComments.get(reply.getParentId());
                            if (parentComment != null) {
                                reply.setParentCommentText(parentComment.getText());
                                if (parentComment.getReplies() == null) {
                                    parentComment.setReplies(new ArrayList<>());
                                }
                                parentComment.getReplies().add(reply);
                            } else {
                                // В случай че родителският коментар не е в topLevelComments
                                // Може да добавите логика тук ако е необходимо
                            }

                            allComments.add(reply);
                        }
                    }
                }

                nextPageToken = jsonResponse.optString("nextPageToken", null);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to parse YouTube API response");
            }

        } while (nextPageToken != null);

        UserDetailsViewModel userDetails = new UserDetailsViewModel();
        List<YouTubeCommentViewModel> userComments = new ArrayList<>();
        List<YouTubeCommentViewModel> userReplies = new ArrayList<>();

        for (YouTubeCommentViewModel comment : allComments) {
            if (comment.getAuthorDisplayName().equals(userId)) {
                if (comment.isTopLevelComment()) {
                    userComments.add(comment);
                } else {
                    userReplies.add(comment);
                }
            }
        }

        if (!userComments.isEmpty()) {
            YouTubeCommentViewModel firstComment = userComments.get(0);
            userDetails.setProfileImageUrl(firstComment.getAuthorProfileImageUrl());
            userDetails.setName(firstComment.getAuthorDisplayName());
            userDetails.setProfileUrl(firstComment.getAuthorProfileUrl());
        }

        userDetails.setTotalComments(userComments.size() + userReplies.size());
        userDetails.setComments(userComments);
        userDetails.setReplies(userReplies);

        return userDetails;
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


    public String formatDate(String date) {
        LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
        ZoneId zoneId = ZoneId.of("Europe/Sofia");
        ZonedDateTime zonedDateTime = dateTime.atZone(zoneId);
        ZonedDateTime adjustedTime = zonedDateTime.plusHours(3);
        return adjustedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
