package com.trollLab.services.serviceImpl;

import com.trollLab.services.YouTubeService;
import com.trollLab.views.CommentViewModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
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

    private final RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(YouTubeServiceImpl.class);

    @Autowired
    public YouTubeServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Override
    public List<CommentViewModel> getComments(String videoUrl, String pageToken, String sort) {
        String videoId = extractVideoId(videoUrl);
        if (videoId == null) {
            throw new IllegalArgumentException("Invalid YouTube video URL: " + videoUrl);
        }

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
                            0 // Initializing total comments count
                    );

                    comment.setAuthorProfileUrl(snippet.getString("authorChannelUrl"));
                    comment.setAuthorProfileImageUrl(snippet.getString("authorProfileImageUrl"));

                    String commentId = item.getJSONObject("snippet").getJSONObject("topLevelComment").getString("id");
                    comment.setIsTopLevelComment(true);
                    commentMap.put(commentId, comment);

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
                                    0 // Initializing total comments count for replies
                            );

                            reply.setIsTopLevelComment(false);
                            reply.setAuthorProfileUrl(replySnippet.getString("authorChannelUrl"));
                            reply.setAuthorProfileImageUrl(replySnippet.getString("authorProfileImageUrl"));
                            replies.add(reply);

                            String replyId = replyObject.getString("id");
                            commentMap.put(replyId, reply);
                        }
                        comment.setReplies(replies);
                    }
                }

                nextPageToken = jsonResponse.optString("nextPageToken", null);

            } catch (Exception e) {
                logger.error("Failed to parse YouTube API response", e);
                throw new RuntimeException("Failed to parse YouTube API response", e);
            }

        } while (nextPageToken != null);

        Map<String, Integer> userCommentCount = new HashMap<>();

        for (CommentViewModel comment : commentMap.values()) {
            String author = comment.getAuthorDisplayName();
            userCommentCount.put(author, userCommentCount.getOrDefault(author, 0) + 1);

            if (comment.getReplies() != null) {
                for (CommentViewModel reply : comment.getReplies()) {
                    userCommentCount.put(reply.getAuthorDisplayName(), userCommentCount.getOrDefault(reply.getAuthorDisplayName(), 0) + 1);
                }
            }
        }

        commentMap.values().forEach(comment -> {
            int totalComments = userCommentCount.getOrDefault(comment.getAuthorDisplayName(), 0);
            comment.setTotalComments(totalComments);
        });

        List<CommentViewModel> commentList = new ArrayList<>(commentMap.values());

        switch (sort) {
            case "oldest":
                commentList.sort(Comparator.comparing(CommentViewModel::getPublishedAt));
                break;
            case "most-liked":
                commentList.sort(Comparator.comparingInt(CommentViewModel::getLikeCount).reversed());
                break;
            case "most-comments":
                commentList.sort((c1, c2) -> {
                    int count1 = userCommentCount.getOrDefault(c1.getAuthorDisplayName(), 0);
                    int count2 = userCommentCount.getOrDefault(c2.getAuthorDisplayName(), 0);
                    return Integer.compare(count2, count1);
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

    @Override
    public List<CommentViewModel> getCommentsBySearchingWords(String videoUrl, String pageToken, String sort, String words) {
        List<CommentViewModel> allComments = getComments(videoUrl, pageToken, sort);
        String[] allWords = words.split("\\s+");

        List<CommentViewModel> filteredComments = allComments.stream()
                .filter(comment -> {
                    for (String word : allWords) {
                        if (comment.getText().contains(word)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());

        return filteredComments;
    }

//

    public String extractVideoId(String videoUrl) {
        String videoId = null;

        try {
            URL url = new URL(videoUrl);
            String host = url.getHost();
            String query = url.getQuery();

            // Check for standard YouTube URLs
            if (host.contains("youtube.com") && query != null) {
                String[] queryParams = query.split("&");
                for (String param : queryParams) {
                    if (param.startsWith("v=")) {
                        videoId = param.split("=")[1];
                        break;
                    }
                }
            }
            // Check for shortened YouTube URLs
            else if (host.contains("youtu.be")) {
                String[] pathSegments = url.getPath().split("/");
                if (pathSegments.length > 1) {
                    videoId = pathSegments[1];
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
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
