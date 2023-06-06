package com.forum.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReplyDTO {
    private int id;
    private String body;
    private LocalDateTime dateCreated;
    private String formattedDate;
    private String formattedTime;
    private int commentId;
    private int replierId;
    private String replierPicture;
}
