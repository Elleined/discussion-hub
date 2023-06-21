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
@Slf4j
public class WSService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CommentService commentService;
    private final ReplyService replyService;
    public void broadcastComment(int commentId) {
        final CommentDTO commentDTO = commentService.getById(commentId);
        commentDTO.setBody(HtmlUtils.htmlEscape(commentDTO.getBody()));

        final String destination = "/discussion/posts/" + commentDTO.getPostId() + "/comments";
        simpMessagingTemplate.convertAndSend(destination, commentDTO);
        log.debug("Comment with body of {} broadcast successfully to {}", commentDTO.getBody(), destination);
    }
    public void broadcastReply(int replyId) {
        final ReplyDTO replyDTO = replyService.getById(replyId);
        replyDTO.setBody(HtmlUtils.htmlEscape(replyDTO.getBody()));

        final String destination = "/discussion/posts/comments/" + replyDTO.getCommentId() + "/replies";
        simpMessagingTemplate.convertAndSend(destination, replyDTO);
        log.debug("Reply with body of {} broadcast successfully to {}", replyDTO.getBody(), destination);
    }
}
