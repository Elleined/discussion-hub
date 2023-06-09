package com.forum.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReplyDTO {
    private int id;
    private String body;
    private String replierName;
    private LocalDateTime dateCreated;
    private String formattedDate;
    private String formattedTime;
    private int commentId;
    private int replierId;
    private String replierPicture;
    private String status;
    private int postId;
}
