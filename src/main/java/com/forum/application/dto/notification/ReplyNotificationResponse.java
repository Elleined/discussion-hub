package com.forum.application.dto.notification;

public class ReplyNotificationResponse extends NotificationResponse {
    public ReplyNotificationResponse(String message, String respondentPicture, int respondentId, String uri) {
        super(message, respondentPicture, respondentId, uri, Type.REPLY);
    }
}
