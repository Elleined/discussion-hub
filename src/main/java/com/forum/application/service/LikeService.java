package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Comment;
import com.forum.application.model.Post;
import com.forum.application.model.Reply;
import com.forum.application.model.User;
import com.forum.application.repository.ReplyRepository;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class LikeService {
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;

    void likePost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        respondent.getLikedPosts().add(post);
        userRepository.save(respondent);
        log.debug("User with id of {} liked post with id of {}", respondentId, post.getId());
    }

    boolean isUserAlreadyLikedPost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        return respondent.getLikedPosts().stream().anyMatch(post::equals);
    }

    void unlikePost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        respondent.getLikedPosts().remove(post);
        userRepository.save(respondent);
        log.debug("User with id of {} unlike post with id of {}", respondentId, post.getId());
    }


    void likeComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        respondent.getLikedComments().add(comment);
        userRepository.save(respondent);
        log.debug("User with id of {} liked comment with id of {}", respondent, comment.getId());
    }

    boolean isUserAlreadyLikedComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        return respondent.getLikedComments().stream().anyMatch(comment::equals);
    }

    void unlikeComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        respondent.getLikedComments().remove(comment);
        userRepository.save(respondent);
        log.debug("User with id of {} unlike comment with id of {}", respondentId, comment.getId());
    }

    void likeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        respondent.getLikedReplies().add(reply);
        userRepository.save(respondent);
        log.debug("User with id of {} liked reply with id of {}", respondentId, reply.getId());
    }

    boolean isUserAlreadyLikeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not exists"));
        return respondent.getLikedReplies().stream().anyMatch(reply::equals);
    }

    void unlikeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not exists"));
        respondent.getLikedReplies().remove(reply);
        userRepository.save(respondent);
        log.debug("User with id of {} unliked reply with id of {}", respondentId, reply.getId());
    }
}
