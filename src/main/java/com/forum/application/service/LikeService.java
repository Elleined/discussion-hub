package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.model.like.CommentLike;
import com.forum.application.model.like.PostLike;
import com.forum.application.model.like.ReplyLike;
import com.forum.application.repository.LikeRepository;
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
    private final LikeRepository likeRepository;
    private final ModalTrackerService modalTrackerService;

    void likePost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(respondentId, post.getId(), ModalTracker.Type.POST)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        PostLike postLike = PostLike.postLikeBuilder()
                .post(post)
                .respondent(respondent)
                .notificationStatus(notificationStatus)
                .build();

        post.getLikes().add(postLike);
        respondent.getLikedPosts().add(postLike);
        likeRepository.save(postLike);
        log.debug("User with id of {} liked post with id of {}", respondentId, post.getId());
    }

    boolean isUserAlreadyLikedPost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        return respondent.getLikedPosts().stream().anyMatch(postLike -> postLike.getPost() == post);
    }

    void unlikePost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        PostLike postLike = respondent.getLikedPosts()
                .stream()
                .filter(like -> like.getPost() == post)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not like post with id of " + post.getId()));

        post.getLikes().remove(postLike);
        respondent.getLikedPosts().remove(postLike);
        likeRepository.delete(postLike);
        log.debug("User with id of {} unlike post with id of {}", respondentId, post.getId());
    }


    void likeComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(respondentId, comment.getId(), ModalTracker.Type.POST)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        CommentLike commentLike = CommentLike.commentLikeBuilder()
                .comment(comment)
                .respondent(respondent)
                .notificationStatus(notificationStatus)
                .build();

        comment.getLikes().add(commentLike);
        respondent.getLikedComments().add(commentLike);
        likeRepository.save(commentLike);
        log.debug("User with id of {} liked comment with id of {}", respondent, comment.getId());
    }

    boolean isUserAlreadyLikedComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        return respondent.getLikedComments().stream().anyMatch(like -> like.getComment() == comment);
    }

    void unlikeComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        CommentLike commentLike = respondent.getLikedComments()
                .stream()
                .filter(like -> like.getComment() == comment)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not liked comment with id of " + comment.getId()));

        comment.getLikes().remove(commentLike);
        respondent.getLikedComments().remove(commentLike);
        likeRepository.delete(commentLike);
        log.debug("User with id of {} unlike comment with id of {}", respondentId, comment.getId());
    }

    void likeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(respondentId, reply.getId(), ModalTracker.Type.POST)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        ReplyLike replyLike = ReplyLike.replyLikeBuilder()
                .reply(reply)
                .respondent(respondent)
                .notificationStatus(notificationStatus)
                .build();

        reply.getLikes().add(replyLike);
        respondent.getLikedReplies().add(replyLike);
        likeRepository.save(replyLike);
        log.debug("User with id of {} liked reply with id of {}", respondentId, reply.getId());
    }

    boolean isUserAlreadyLikeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not exists"));
        return respondent.getLikedReplies().stream().anyMatch(like -> like.getReply() == reply);
    }

    void unlikeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not exists"));
        ReplyLike replyLike = respondent.getLikedReplies()
                .stream()
                .filter(like -> like.getReply() == reply)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not liked reply with id of " + reply.getId()));

        reply.getLikes().remove(replyLike);
        respondent.getLikedReplies().remove(replyLike);
        likeRepository.delete(replyLike);
        log.debug("User with id of {} unliked reply with id of {}", respondentId, reply.getId());
    }
}
