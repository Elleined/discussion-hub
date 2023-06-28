package com.forum.application.dto.notification;

import com.forum.application.model.Type;

public class CommentNotificationResponse extends NotificationResponse {
    public CommentNotificationResponse(int id, String message, String respondentPicture, int respondentId, String uri, Type type, boolean isModalOpen, int count) {
        super(id, message, respondentPicture, respondentId, uri, type, isModalOpen, count);
    }
}
