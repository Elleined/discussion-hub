package com.forum.application.service;

import com.forum.application.dto.NotificationResponse;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.mapper.NotificationMapper;
import com.forum.application.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final MentionService mentionService;
    private final LikeService likeService;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final NotificationMapper notificationMapper;

    public Set<NotificationResponse> getAllNotification(User currentUser) throws ResourceNotFoundException {
        Set<NotificationResponse> unreadComments = commentService.getUnreadCommentsOfAllPost(currentUser).stream()
                .map(comment -> notificationMapper.toCommentNotification(comment, currentUser))
                .collect(Collectors.toSet());

        Set<NotificationResponse> unreadReply = replyService.getUnreadRepliesOfAllComments(currentUser).stream()
                .map(reply -> notificationMapper.toReplyNotification(reply, currentUser))
                .collect(Collectors.toSet());

        // mention and like notification here
        return Stream.of(unreadComments, unreadReply)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public long getTotalNotificationCount(User currentUser) throws ResourceNotFoundException {
        return commentService.getUnreadCommentsOfAllPost(currentUser).size() +
                replyService.getUnreadRepliesOfAllComments(currentUser).size() +
                likeService.getUnreadPostLikes(currentUser).size() +
                likeService.getUnreadCommentLikes(currentUser).size() +
                likeService.getUnreadReplyLikes(currentUser).size() +
                mentionService.getUnreadCommentMentions(currentUser).size() +
                mentionService.getUnreadReplyMentions(currentUser).size();
    }

}
