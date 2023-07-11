package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Status;
import com.forum.application.model.Type;
import com.forum.application.model.User;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.PostRepository;
import com.forum.application.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MentionHelper {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    public boolean isDeleted(Type type, int typeId) {
        return switch (type) {
            case POST -> postRepository.findById(typeId).orElseThrow().getStatus() == Status.INACTIVE;
            case COMMENT -> commentRepository.findById(typeId).orElseThrow().getStatus() == Status.INACTIVE;
            case REPLY -> replyRepository.findById(typeId).orElseThrow().getStatus() == Status.INACTIVE;
        };
    }

    public int getParentId(Type type, int typeId) {
        return switch (type) {
            case POST -> 0; // 
            case COMMENT -> commentRepository.findById(typeId).orElseThrow().getPost().getId();
            case REPLY -> replyRepository.findById(typeId).orElseThrow().getComment().getId();
        };
    }

    public String getMessage(User mentioningUser, Type type, int typeId) throws ResourceNotFoundException {
        return switch (type) {
            case POST -> mentioningUser.getName() + " mentioned you in his/her post: " + "\"" + postRepository.findById(typeId).orElseThrow().getBody() + "\"";
            case COMMENT -> mentioningUser.getName() + " mentioned you in his/her comment: " + "\"" + commentRepository.findById(typeId).orElseThrow().getBody() + "\"";
            case REPLY -> mentioningUser.getName() + " mentioned you in his/her reply: " + "\"" + replyRepository.findById(typeId).orElseThrow().getBody() + "\"";
        };
    }
}
