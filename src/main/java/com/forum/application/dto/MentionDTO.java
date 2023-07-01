package com.forum.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class MentionDTO {
    int mentioningUserId;
    int mentionedUserId;
    String type;
    int typeId;
    LocalDateTime createdAt;
    String notificationStatus;
    String message;
}