package com.forum.application.mapper;

import com.forum.application.dto.UserDTO;
import com.forum.application.model.User;
import com.forum.application.model.like.Like;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);

    default UserDTO mapToLikeToUser(Like like) {
        return toDTO(like.getRespondent());
    }
}
