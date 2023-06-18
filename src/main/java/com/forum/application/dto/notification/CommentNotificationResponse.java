package com.forum.application.dto.notification;

public class CommentNotificationResponse extends NotificationResponse {
    public CommentNotificationResponse(String message, String respondentPicture, int respondentId, String uri) {
        super(message, respondentPicture, respondentId, uri, Type.COMMENT);
    }
}
