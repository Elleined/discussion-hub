package com.forum.application.mapper;

import com.forum.application.dto.MentionResponse;
import com.forum.application.model.Mention;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
abstract class MentionMapper {
    @Mappings({
            @Mapping(target = "mentioningUserId", source = "mention.mentioningUser.id"),
            @Mapping(target = "mentionedUserId", source = "mention.mentionedUser.id"),
            @Mapping(target = "type", source = "mention.type"),
            @Mapping(target = "notificationStatus", source = "mention.notificationStatus"),
            @Mapping(target = "message", ignore = true)
    })
    public abstract MentionResponse toDTO(Mention mention);
}
