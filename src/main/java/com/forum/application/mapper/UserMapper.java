package com.forum.application.mapper;

import com.forum.application.dto.UserDTO;
import com.forum.application.model.User;
import com.forum.application.model.mention.Mention;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);

    default Set<UserDTO> mapLikers(Set<User> likers) {
        return likers.stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }

    default UserDTO mapMentionToUser(Mention mention) {
        return toDTO(mention.getMentionedUser());
    }
}
