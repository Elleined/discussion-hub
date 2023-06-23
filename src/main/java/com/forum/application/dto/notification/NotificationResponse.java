package com.forum.application.dto.notification;

import com.forum.application.model.Type;
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
}
