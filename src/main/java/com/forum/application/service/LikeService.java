package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.model.like.CommentLike;
import com.forum.application.model.like.Like;
import com.forum.application.model.like.PostLike;
import com.forum.application.model.like.ReplyLike;
import com.forum.application.repository.LikeRepository;
import com.forum.application.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LikeService {
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final ModalTrackerService modalTrackerService;
    private final LikeNotificationService likeNotificationService;
    private final LikeNotificationReaderService likeNotificationReaderService;

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

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(comment.getCommenter().getId(), comment.getPost().getId(), ModalTracker.Type.COMMENT)
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

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(reply.getReplier().getId(), reply.getComment().getId(), ModalTracker.Type.REPLY)
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

    Set<PostLike> getUnreadPostLikes(User currentUser) {
        return likeNotificationService.getUnreadPostLikes(currentUser);
    }
    Set<CommentLike> getUnreadCommentLikes(User currentUser) {
        return likeNotificationService.getUnreadCommentLikes(currentUser);
    }
    Set<ReplyLike> getUnreadReplyLikes(User currentUser) {
        return likeNotificationService.getUnreadReplyLikes(currentUser);
    }

    void readPostLikes(User currentUser) {
        likeNotificationReaderService.readPostLikes(currentUser);
    }
    void readCommentLikes(User currentUser) {
        likeNotificationReaderService.readCommentLikes(currentUser);
    }
    void readReplyLikes(User currentUser) {
        likeNotificationReaderService.readReplyLikes(currentUser);
    }

    @Service
    @RequiredArgsConstructor
    private static class LikeNotificationService {
        private final BlockService blockService;

        private Set<PostLike> getUnreadPostLikes(User currentUser) {
            return currentUser.getPosts()
                    .stream()
                    .map(Post::getLikes)
                    .flatMap(likes -> likes.stream()
                            .filter(like -> like.getRespondent() != currentUser)
                            .filter(like -> like.getPost().getStatus() == Status.ACTIVE)
                            .filter(like -> like.getNotificationStatus() == NotificationStatus.UNREAD)
                            .filter(like -> !blockService.isBlockedBy(currentUser.getId(), like.getRespondent().getId()))
                            .filter(like -> !blockService.isYouBeenBlockedBy(currentUser.getId(), like.getRespondent().getId())))
                    .collect(Collectors.toSet());
        }

        private Set<CommentLike> getUnreadCommentLikes(User currentUser) {
            return currentUser.getComments()
                    .stream()
                    .map(Comment::getLikes)
                    .flatMap(likes -> likes.stream()
                            .filter(like -> like.getRespondent() != currentUser)
                            .filter(like -> like.getComment().getStatus() == Status.ACTIVE)
                            .filter(like -> like.getNotificationStatus() == NotificationStatus.UNREAD)
                            .filter(like -> !blockService.isBlockedBy(currentUser.getId(), like.getRespondent().getId()))
                            .filter(like -> !blockService.isYouBeenBlockedBy(currentUser.getId(), like.getRespondent().getId())))
                    .collect(Collectors.toSet());
        }

        private Set<ReplyLike> getUnreadReplyLikes(User currentUser) {
            return currentUser.getReplies()
                    .stream()
                    .map(Reply::getLikes)
                    .flatMap(likes -> likes.stream()
                            .filter(like -> like.getRespondent() != currentUser)
                            .filter(like -> like.getReply().getStatus() == Status.ACTIVE)
                            .filter(like -> like.getNotificationStatus() == NotificationStatus.UNREAD)
                            .filter(like -> !blockService.isBlockedBy(currentUser.getId(), like.getRespondent().getId()))
                            .filter(like -> !blockService.isYouBeenBlockedBy(currentUser.getId(), like.getRespondent().getId())))
                    .collect(Collectors.toSet());
        }
    }

    @Service
    @RequiredArgsConstructor
    private static class LikeNotificationReaderService {
        private final LikeNotificationService likeNotificationService;
        private final LikeRepository likeRepository;

        private void readPostLikes(User currentUser) {
            Set<PostLike> postLikes = likeNotificationService.getUnreadPostLikes(currentUser);
            postLikes.forEach(this::readLikeNotification);
            likeRepository.saveAll(postLikes);
            log.debug("Reading all unread post like for current user with id of {} success", currentUser.getId());
        }

        private void readCommentLikes(User currentUser) {
            Set<CommentLike> commentLikes = likeNotificationService.getUnreadCommentLikes(currentUser);
            commentLikes.forEach(this::readLikeNotification);
            likeRepository.saveAll(commentLikes);
            log.debug("Reading all unread comment like for current user with id of {} success", currentUser.getId());
        }

        private void readReplyLikes(User currentUser) {
            Set<ReplyLike> replyLikes = likeNotificationService.getUnreadReplyLikes(currentUser);
            replyLikes.forEach(this::readLikeNotification);
            likeRepository.saveAll(replyLikes);
            log.debug("Reading all unread reply like for current user with id of {} success", currentUser.getId());
        }

        private void readLikeNotification(Like like) {
            like.setNotificationStatus(NotificationStatus.READ);
        }
    }
}
