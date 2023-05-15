package com.forum.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDTO {
    private int id;
    private String body;
    private LocalDateTime dateCreated;
    private int postId;
    private int commenterId;
}
