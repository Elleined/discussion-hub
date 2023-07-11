package com.forum.application.dto;

import com.forum.application.model.Type;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private int id;
    private String message;
    private String respondentPicture;
    private int respondentId;
    private String uri;
    private Type type;
    private boolean isModalOpen;
    private int count;
    private String formattedDate;
    private String formattedTime;
}
