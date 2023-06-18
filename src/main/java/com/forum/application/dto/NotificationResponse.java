package com.forum.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private String message;
    private String commenterPicture;
    private int postId;
}
