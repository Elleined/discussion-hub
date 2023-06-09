package com.forum.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostDTO {
    private int id;
    private String body;
    private LocalDateTime dateCreated;
    private String formattedDateCreated;
    private String formattedTimeCreated;
    private int authorId;
    private String authorName;
    private String authorPicture;
    private int totalCommentAndReplies;
    private String status;
}
