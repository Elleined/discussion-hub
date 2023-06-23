package com.forum.application.dto.notification;

import com.forum.application.model.Type;
import lombok.Builder;
import lombok.Getter;

public class ReplyNotificationResponse extends NotificationResponse {
    @Getter private String commentURI;
    @Builder(builderMethodName = "replyNotificationBuilder")
    public ReplyNotificationResponse(String message, String respondentPicture, int respondentId, String uri, Type type, String commentURI) {
        super(message, respondentPicture, respondentId, uri, type);
        this.commentURI = commentURI;
    }
}
