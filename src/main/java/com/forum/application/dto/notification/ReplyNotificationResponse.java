package com.forum.application.dto.notification;

import com.forum.application.model.Type;
import lombok.Builder;
import lombok.Getter;

public class ReplyNotificationResponse extends NotificationResponse {
    @Getter private String commentURI;
    @Builder(builderMethodName = "replyNotificationBuilder")

    public ReplyNotificationResponse(int id, String message, String respondentPicture, int respondentId, String uri, Type type, boolean isModalOpen, int count, String formattedDate, String formattedTime, String commentURI) {
        super(id, message, respondentPicture, respondentId, uri, type, isModalOpen, count, formattedDate, formattedTime);
        this.commentURI = commentURI;
    }
}
