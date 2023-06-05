package com.forum.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDTO {
    private int id;
    private String body;
    private String commenterName;
    private LocalDateTime dateCreated;
    private String formattedDate;
    private String formattedTime;
    private int postId;
    private int commenterId;
    private String commenterPicture;
}
