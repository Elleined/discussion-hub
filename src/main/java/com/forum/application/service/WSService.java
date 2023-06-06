package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.ReplyDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
@Service
@RequiredArgsConstructor
public class WSService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    public void broadcastComment(int postId, CommentDTO commentDTO) {
        String destination = "/discussion/posts/" + postId + "/comments";
        commentDTO.setBody(HtmlUtils.htmlEscape(commentDTO.getBody()));
        simpMessagingTemplate.convertAndSend(destination, commentDTO);
    }

    public void broadcastReply(int commentId, ReplyDTO replyDTO) {
        String destination = "/discussion/posts/comments/" + commentId + "/replies";
        replyDTO.setBody(HtmlUtils.htmlEscape(replyDTO.getBody()));
        simpMessagingTemplate.convertAndSend(destination, replyDTO);
    }
}
