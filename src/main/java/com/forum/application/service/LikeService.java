package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Comment;
import com.forum.application.model.Post;
import com.forum.application.model.Reply;
import com.forum.application.model.User;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.PostRepository;
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
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    void likePost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        respondent.getLikedPosts().add(post);
        post.getLikes().add(respondent);

        postRepository.save(post);
        userRepository.save(respondent);
        log.debug("User with id of {} liked post with id of {}", respondentId, post.getId());
    }

    boolean isUserAlreadyLikedPost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        return respondent.getLikedPosts().stream().anyMatch(post::equals);
    }

    boolean isUserAlreadyLikedPost(User respondent, int postId) {
        return respondent.getLikedPosts().stream().anyMatch(likedPost -> likedPost.getId() == postId);
    }

    void unlikePost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        respondent.getLikedPosts().remove(post);
        post.getLikes().remove(respondent);

        postRepository.save(post);
        userRepository.save(respondent);
        log.debug("User with id of {} unlike post with id of {}", respondentId, post.getId());
    }


    void likeComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        respondent.getLikedComments().add(comment);
        comment.getLikes().add(respondent);

        commentRepository.save(comment);
        userRepository.save(respondent);
        log.debug("User with id of {} liked comment with id of {}", respondent, comment.getId());
    }

    boolean isUserAlreadyLikedComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        return respondent.getLikedComments().stream().anyMatch(comment::equals);
    }

    boolean isUserAlreadyLikedComment(User respondent, int commentId) {
        return respondent.getLikedComments().stream().anyMatch(likedComment -> likedComment.getId() == commentId);
    }

    void unlikeComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        respondent.getLikedComments().remove(comment);
        comment.getLikes().remove(respondent);

        commentRepository.save(comment);
        userRepository.save(respondent);
        log.debug("User with id of {} unlike comment with id of {}", respondentId, comment.getId());
    }

    void likeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        respondent.getLikedReplies().add(reply);
        reply.getLikes().add(respondent);

        replyRepository.save(reply);
        userRepository.save(respondent);
        log.debug("User with id of {} liked reply with id of {}", respondentId, reply.getId());
    }

    boolean isUserAlreadyLikeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not exists"));
        return respondent.getLikedReplies().stream().anyMatch(reply::equals);
    }

    boolean isUserAlreadyLikeReply(User respondent, int replyId) {
        return respondent.getLikedReplies().stream().anyMatch(likedReply -> likedReply.getId() == replyId);
    }

    void unlikeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not exists"));
        respondent.getLikedReplies().remove(reply);
        reply.getLikes().remove(respondent);

        replyRepository.save(reply);
        userRepository.save(respondent);
        log.debug("User with id of {} unliked reply with id of {}", respondentId, reply.getId());
    }
}
