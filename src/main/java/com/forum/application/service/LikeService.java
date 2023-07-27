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

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LikeService {
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final ModalTrackerService modalTrackerService;

    void likePost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(post.getAuthor().getId(), post.getId(), ModalTracker.Type.POST)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        PostLike postLike = PostLike.postLikeBuilder()
                .respondent(respondent)
                .post(post)
                .notificationStatus(notificationStatus)
                .createdAt(LocalDateTime.now())
                .build();

        respondent.getLikedPosts().add(postLike);
        post.getLikes().add(postLike);
        likeRepository.save(postLike);
        log.debug("User with id of {} liked post with id of {}", respondentId, post.getId());
    }

    public boolean isUserAlreadyLikedPost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        return respondent.getLikedPosts().stream()
                .map(PostLike::getPost)
                .anyMatch(post::equals);
    }

    void unlikePost(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        PostLike postLike = respondent.getLikedPosts()
                .stream()
                .filter(like -> like.getPost().equals(post))
                .findFirst()
                .orElseThrow();

        respondent.getLikedPosts().remove(postLike);
        post.getLikes().remove(postLike);
        likeRepository.delete(postLike);
        log.debug("User with id of {} unlike post with id of {}", respondentId, post.getId());
    }
    void likeComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(comment.getCommenter().getId(), comment.getId(), ModalTracker.Type.COMMENT)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        CommentLike commentLike = CommentLike.commentLikeBuilder()
                .respondent(respondent)
                .comment(comment)
                .notificationStatus(notificationStatus)
                .createdAt(LocalDateTime.now())
                .build();

        respondent.getLikedComments().add(commentLike);
        comment.getLikes().add(commentLike);
        likeRepository.save(commentLike);
        log.debug("User with id of {} liked comment with id of {}", respondentId, comment.getId());
    }

    public boolean isUserAlreadyLikedComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        return respondent.getLikedComments().stream()
                .map(CommentLike::getComment)
                .anyMatch(comment::equals);
    }

    void unlikeComment(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        CommentLike commentLike = respondent.getLikedComments().stream()
                .filter(likedComment -> likedComment.getComment().equals(comment))
                .findFirst()
                .orElseThrow();

        respondent.getLikedComments().remove(commentLike);
        comment.getLikes().remove(commentLike);
        likeRepository.delete(commentLike);
        log.debug("User with id of {} unlike comment with id of {}", respondentId, comment.getId());
    }

    void likeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(reply.getReplier().getId(), reply.getId(), ModalTracker.Type.REPLY)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        ReplyLike replyLike = ReplyLike.replyLikeBuilder()
                .respondent(respondent)
                .reply(reply)
                .notificationStatus(notificationStatus)
                .createdAt(LocalDateTime.now())
                .build();

        respondent.getLikedReplies().add(replyLike);
        reply.getLikes().add(replyLike);
        likeRepository.save(replyLike);
        log.debug("User with id of {} liked reply with id of {}", respondentId, reply.getId());
    }

    public boolean isUserAlreadyLikeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not exists"));
        return respondent.getLikedReplies().stream()
                .map(ReplyLike::getReply)
                .anyMatch(reply::equals);
    }


    void unlikeReply(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not exists"));
        ReplyLike replyLike = respondent.getLikedReplies()
                .stream()
                .filter(likedReply -> likedReply.getReply().equals(reply))
                .findFirst()
                .orElseThrow();

        respondent.getLikedReplies().remove(replyLike);
        reply.getLikes().remove(replyLike);
        likeRepository.delete(replyLike);
        log.debug("User with id of {} unliked reply with id of {}", respondentId, reply.getId());
    }
}
