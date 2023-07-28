
package com.forum.application.mapper;

import com.forum.application.dto.ReplyDTO;
import com.forum.application.model.Reply;
import com.forum.application.service.Formatter;
import com.forum.application.service.LikeService;
import com.forum.application.service.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Mapper(componentModel = "spring", imports = Formatter.class, uses = UserMapper.class)
public abstract class ReplyMapper {

    @Autowired @Lazy
    protected UserService userService;

    @Autowired @Lazy
    protected LikeService likeService;
    @Mappings({
            @Mapping(target = "replierName", source = "reply.replier.name"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDateWithoutYear(reply.getDateCreated()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(reply.getDateCreated()))"),
            @Mapping(target = "commentId", source = "reply.comment.id"),
            @Mapping(target = "replierId", source = "replier.id"),
            @Mapping(target = "replierPicture", source = "replier.picture"),
            @Mapping(target = "status", source = "reply.status"),
            @Mapping(target = "postId", source = "reply.comment.post.id"),
            @Mapping(target = "notificationStatus", source = "reply.notificationStatus"),
            @Mapping(target = "likers", source = "reply.likes"),
            @Mapping(target = "mentionedUsers", source = "reply.mentions"),
            @Mapping(target = "totalLikes", expression = "java(reply.getLikes().size())"),
            @Mapping(target = "isCurrentUserLikedReply", expression = "java(likeService.isUserAlreadyLiked(userService.getCurrentUser().getId(), reply))")
    })
    public abstract ReplyDTO toDTO(Reply reply);
}
