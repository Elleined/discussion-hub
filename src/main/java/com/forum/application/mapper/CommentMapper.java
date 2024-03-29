package com.forum.application.mapper;

import com.forum.application.dto.CommentDTO;
import com.forum.application.model.Comment;
import com.forum.application.service.CommentService;
import com.forum.application.service.Formatter;
import com.forum.application.service.LikeService;
import com.forum.application.service.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
@Mapper(componentModel = "spring", imports = Formatter.class, uses = UserMapper.class)
public abstract class CommentMapper {

    @Autowired @Lazy
    protected CommentService commentService;

    @Autowired @Lazy
    protected LikeService likeService;

    @Autowired @Lazy
    protected UserService userService;

    @Mappings({
            @Mapping(target = "commenterName", source = "comment.commenter.name"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDateWithoutYear(comment.getDateCreated()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(comment.getDateCreated()))"),
            @Mapping(target = "postId", source = "comment.post.id"),
            @Mapping(target = "commenterId", source = "comment.commenter.id"),
            @Mapping(target = "commenterPicture", source = "comment.commenter.picture"),
            @Mapping(target = "authorName", source = "comment.post.author.name"),
            @Mapping(target = "status", source = "comment.status"),
            @Mapping(target = "totalReplies", expression = "java(commentService.getTotalReplies(comment))"),
            @Mapping(target = "notificationStatus", source = "comment.notificationStatus"),
            @Mapping(target = "postBody", source = "comment.post.body"),
            @Mapping(target = "likers", source = "comment.likes"),
            @Mapping(target = "mentionedUsers", source = "comment.mentions"),
            @Mapping(target = "totalLikes", expression = "java(comment.getLikes().size())"),
            @Mapping(target = "isCurrentUserLikedComment", expression = "java(likeService.isUserAlreadyLiked(userService.getCurrentUser().getId(), comment))")
    })
    public abstract CommentDTO toDTO(Comment comment);
}
