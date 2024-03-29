package com.forum.application.service;

import com.forum.application.exception.BlockedException;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.model.like.CommentLike;
import com.forum.application.model.like.Like;
import com.forum.application.model.like.PostLike;
import com.forum.application.model.like.ReplyLike;
import com.forum.application.repository.LikeRepository;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
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

    private final PostService postService;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final BlockService blockService;

    Optional<Like> like(Post post, int respondentId) throws ResourceNotFoundException, BlockedException {
        if (postService.isDeleted(post)) throw new ResourceNotFoundException("Cannot like/unlike! The post with id of " + post.getId() + " you are trying to like/unlike might already been deleted or does not exists!");
        if (blockService.isBlockedBy(respondentId, post.getAuthor().getId())) throw new BlockedException("Cannot like/unlike! You blocked the author of this post with id of !" + post.getAuthor().getId());
        if (blockService.isYouBeenBlockedBy(respondentId, post.getAuthor().getId())) throw  new BlockedException("Cannot like/unlike! The author of this post with id of " + post.getAuthor().getId() + " already blocked you");

        if (isUserAlreadyLiked(respondentId, post)) {
            unlike(respondentId, post);
            return Optional.empty();
        }
        return Optional.of(like(respondentId, post));
    }

    Optional<Like> like(Comment comment, int respondentId) throws ResourceNotFoundException, BlockedException {
        if (commentService.isDeleted(comment)) throw new ResourceNotFoundException("Cannot like/unlike! The comment with id of " + comment.getId() + " you are trying to like/unlike might already been deleted or does not exists!");
        if (blockService.isBlockedBy(respondentId, comment.getCommenter().getId())) throw new BlockedException("Cannot like/unlike! You blocked the author of this comment with id of !" + comment.getCommenter().getId());
        if (blockService.isYouBeenBlockedBy(respondentId, comment.getCommenter().getId())) throw new BlockedException("Cannot like/unlike! The author of this comment with id of " + comment.getCommenter().getId() + " already blocked you");

        if (isUserAlreadyLiked(respondentId, comment))  {
            unlike(respondentId, comment);
            return Optional.empty();
        }
        return Optional.of(like(respondentId, comment));
    }

    Optional<Like> like(Reply reply, int respondentId) throws ResourceNotFoundException, BlockedException {
        if (replyService.isDeleted(reply)) throw new ResourceNotFoundException("Cannot like/unlike! The reply with id of " + reply.getId() + " you are trying to like/unlike might already be deleted or does not exists!");
        if (blockService.isBlockedBy(respondentId, reply.getReplier().getId())) throw new BlockedException("Cannot like/unlike! You blocked the author of this reply with id of !" + reply.getReplier().getId());
        if (blockService.isYouBeenBlockedBy(respondentId, reply.getReplier().getId())) throw  new BlockedException("Cannot like/unlike! The author of this reply with id of " + reply.getReplier().getId() + " already blocked you");
        if (isUserAlreadyLiked(respondentId, reply)) {
            unlike(respondentId, reply);
            return Optional.empty();
        }
        return Optional.of(like(respondentId, reply));
    }

    private Like like(int respondentId, Post post) {
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
        return postLike;
    }

    public boolean isUserAlreadyLiked(int respondentId, Post post) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        return respondent.getLikedPosts().stream()
                .map(PostLike::getPost)
                .anyMatch(post::equals);
    }

    private void unlike(int respondentId, Post post) {
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
    private Like like(int respondentId, Comment comment) {
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
        return commentLike;
    }

    public boolean isUserAlreadyLiked(int respondentId, Comment comment) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));
        return respondent.getLikedComments().stream()
                .map(CommentLike::getComment)
                .anyMatch(comment::equals);
    }

    private void unlike(int respondentId, Comment comment) {
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

    private Like like(int respondentId, Reply reply) {
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
        return replyLike;
    }

    public boolean isUserAlreadyLiked(int respondentId, Reply reply) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not exists"));
        return respondent.getLikedReplies().stream()
                .map(ReplyLike::getReply)
                .anyMatch(reply::equals);
    }


    private void unlike(int respondentId, Reply reply) {
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
