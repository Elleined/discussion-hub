package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.mapper.CommentMapper;
import com.forum.application.mapper.ReplyMapper;
import com.forum.application.model.Comment;
import com.forum.application.model.Reply;
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
    private final CommentMapper commentMapper;
    private final ReplyMapper replyMapper;

    void broadcastComment(Comment comment) {
        final CommentDTO commentDTO = commentMapper.toDTO(comment);
        commentDTO.setBody(HtmlUtils.htmlEscape(commentDTO.getBody()));

        final String destination = "/discussion/posts/" + commentDTO.getPostId() + "/comments";
        simpMessagingTemplate.convertAndSend(destination, commentDTO);
        log.debug("Comment with id of {} and body of {} broadcast successfully to {}", comment.getId(), commentDTO.getBody(), destination);
    }

    void broadcastReply(Reply reply) {
        final ReplyDTO replyDTO = replyMapper.toDTO(reply);
        replyDTO.setBody(HtmlUtils.htmlEscape(replyDTO.getBody()));

        final String destination = "/discussion/posts/comments/" + replyDTO.getCommentId() + "/replies";
        simpMessagingTemplate.convertAndSend(destination, replyDTO);
        log.debug("Reply with id of {} and body of {} broadcast successfully to {}", reply.getId(), replyDTO.getBody(), destination);
    }
}
