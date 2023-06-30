package com.forum.application.dto;

import lombok.Builder;

@Builder
public record MentionDTO(int mentioningUserId, int mentionedUserId, String type, int typeId) {
}
