package com.forum.application.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MentionDTO(int mentioningUserId, int mentionedUserId, String type, int typeId, LocalDateTime createdAt, String notificationStatus) {
}