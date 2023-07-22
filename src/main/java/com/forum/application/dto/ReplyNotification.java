package com.forum.application.dto;

import com.forum.application.model.Type;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


public class ReplyNotification extends NotificationResponse {

    @Getter @Setter
    private int postId;

    @Builder(builderMethodName = "replyNotificationBuilder")
    public ReplyNotification(int id, String message, String respondentPicture, int respondentId, Type type, String notificationStatus, int count, String formattedDate, String formattedTime, int postId) {
        super(id, message, respondentPicture, respondentId, type, notificationStatus, count, formattedDate, formattedTime);
        this.postId = postId;
    }
}
