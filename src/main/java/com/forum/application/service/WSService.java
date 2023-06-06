package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
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

    public void comment(int postId, CommentDTO commentDTO) {
        String destination = "/discussion/posts/" + postId + "/comments";
        commentDTO.setBody(HtmlUtils.htmlEscape(commentDTO.getBody()));
        simpMessagingTemplate.convertAndSend(destination, commentDTO);
    }
}
