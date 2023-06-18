package com.forum.application.dto.notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private String message;
    private String respondentPicture;
    private int respondentId;
    private String uri;
    private Type type;

    public enum Type {
        COMMENT, REPLY
    }
}
