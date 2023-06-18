package com.forum.application.service;

import com.forum.application.dto.NotificationResponse;
import com.forum.application.dto.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final PostService postService;

    public void sendCommentNotification(int postId, int commenterId) {
        int authorId = postService.getById(postId).getAuthorId();
        String commenterName = userService.getById(commenterId).getName();
        String commenterPicture = userService.getById(commenterId).getPicture();

        String postBody = postService.getById(postId).getBody();
        var notificationResponse = NotificationResponse.builder()
                .message(commenterName + " commented in your post: " + postBody)
                .commenterPicture(commenterPicture)
                .postId(postId)
                .build();
        final String destination = "/discussion/forum-notification/" + authorId;
        simpMessagingTemplate.convertAndSend(destination, notificationResponse);
    }
}
