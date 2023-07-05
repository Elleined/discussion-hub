package com.forum.application.mapper;

import com.forum.application.dto.notification.NotificationResponse;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class NotificationMapper {

    public abstract NotificationResponse toDTO(int typeId, int respondentId);

 
}
