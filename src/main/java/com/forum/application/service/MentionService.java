package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.model.mention.CommentMention;
import com.forum.application.model.mention.Mention;
import com.forum.application.model.mention.PostMention;
import com.forum.application.model.mention.ReplyMention;
import com.forum.application.repository.MentionRepository;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MentionService {

    private final UserRepository userRepository;
    private final MentionRepository mentionRepository;
    private final ModalTrackerService modalTrackerService;

    private final MentionNotificationService mentionNotificationService;
    private final MentionNotificationReaderService mentionNotificationReaderService;

    void addPostMention(User currentUser, int mentionedUserId, Post post) {
        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("Mentioned user with id of " + mentionedUserId + " doesn't exists!"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(mentionedUserId, post.getId(), ModalTracker.Type.POST)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        PostMention postMention = PostMention.postMentionBuilder()
                .mentioningUser(currentUser)
                .mentionedUser(mentionedUser)
                .createdAt(LocalDateTime.now())
                .notificationStatus(notificationStatus)
                .post(post)
                .build();

        currentUser.getSentPostMentions().add(postMention);
        mentionedUser.getReceivePostMentions().add(postMention);
        post.getMentions().add(postMention);
        mentionRepository.save(postMention);
        log.debug("User with id of {} mentioned user with id of {} in post with id of {}", currentUser.getId(), mentionedUserId, post.getId());
    }

    void addCommentMention(User currentUser, int mentionedUserId, Comment comment) {
        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("Mentioned user with id of " + mentionedUserId + " doesn't exists!"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(mentionedUserId, comment.getId(), ModalTracker.Type.COMMENT)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        CommentMention commentMention = CommentMention.commentMentionBuilder()
                .mentioningUser(currentUser)
                .mentionedUser(mentionedUser)
                .createdAt(LocalDateTime.now())
                .comment(comment)
                .notificationStatus(notificationStatus)
                .build();

        currentUser.getSentCommentMentions().add(commentMention);
        mentionedUser.getReceiveCommentMentions().add(commentMention);
        comment.getMentions().add(commentMention);
        mentionRepository.save(commentMention);
        log.debug("User with id of {} mentioned user with id of {} in comment with id of {}", currentUser.getId(), mentionedUserId, comment.getId());
    }

    void addReplyMention(User currentUser, int mentionedUserId, Reply reply) {
        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("Mentioned user with id of " + mentionedUserId + " doesn't exists!"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(mentionedUserId, reply.getId(), ModalTracker.Type.REPLY)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        ReplyMention replyMention = ReplyMention.replyMentionBuilder()
                .mentioningUser(currentUser)
                .mentionedUser(mentionedUser)
                .createdAt(LocalDateTime.now())
                .reply(reply)
                .notificationStatus(notificationStatus)
                .build();

        currentUser.getSentReplyMentions().add(replyMention);
        mentionedUser.getReceiveReplyMentions().add(replyMention);
        reply.getMentions().add(replyMention);
        mentionRepository.save(replyMention);
        log.debug("User with id of {} mentioned user with id of {} in reply with id of {}", currentUser.getId(), mentionedUserId, reply.getId());
    }

    Set<PostMention> getUnreadPostMentions(User currentUser) {
        return mentionNotificationService.getUnreadPostMentions(currentUser);
    }

    Set<CommentMention> getUnreadCommentMentions(User currentUser) {
        return mentionNotificationService.getUnreadCommentMentions(currentUser);
    }
    Set<ReplyMention> getUnreadReplyMentions(User currentUser) {
        return mentionNotificationService.getUnreadReplyMentions(currentUser);
    }

    void readPostMentions(User currentUser) {
        mentionNotificationReaderService.readPostMentions(currentUser);
    }
    void readCommentMentions(User currentUser) {
        mentionNotificationReaderService.readCommentMentions(currentUser);
    }
    void readReplyMentions(User currentUser) {
        mentionNotificationReaderService.readReplyMentions(currentUser);
    }

    @Service
    @RequiredArgsConstructor
    private static class MentionNotificationService {
    
        private final BlockService blockService;

        private Set<PostMention> getUnreadPostMentions(User currentUser) {
            return currentUser.getReceivePostMentions()
                    .stream()
                    .filter(mention -> !blockService.isBlockedBy(mention.getMentioningUser().getId(), mention.getMentioningUser().getId()))
                    .filter(mention -> !blockService.isYouBeenBlockedBy(mention.getMentioningUser().getId(), mention.getMentioningUser().getId()))
                    .filter(mention -> mention.getPost().getStatus() == Status.ACTIVE)
                    .filter(mention -> mention.getNotificationStatus() == NotificationStatus.UNREAD)
                    .collect(Collectors.toSet());
        }
    
        private Set<CommentMention> getUnreadCommentMentions(User currentUser) {
            return currentUser.getReceiveCommentMentions()
                    .stream()
                    .filter(mention -> !blockService.isBlockedBy(mention.getMentioningUser().getId(), mention.getMentioningUser().getId()))
                    .filter(mention -> !blockService.isYouBeenBlockedBy(mention.getMentioningUser().getId(), mention.getMentioningUser().getId()))
                    .filter(mention -> mention.getComment().getStatus() == Status.ACTIVE)
                    .filter(mention -> mention.getNotificationStatus() == NotificationStatus.UNREAD)
                    .collect(Collectors.toSet());
        }

        private Set<ReplyMention> getUnreadReplyMentions(User currentUser) {
            return currentUser.getReceiveReplyMentions()
                    .stream()
                    .filter(mention -> !blockService.isBlockedBy(mention.getMentioningUser().getId(), mention.getMentioningUser().getId()))
                    .filter(mention -> !blockService.isYouBeenBlockedBy(mention.getMentioningUser().getId(), mention.getMentioningUser().getId()))
                    .filter(mention -> mention.getReply().getStatus() == Status.ACTIVE)
                    .filter(mention -> mention.getNotificationStatus() == NotificationStatus.UNREAD)
                    .collect(Collectors.toSet());
        }
    }

    @Service
    @RequiredArgsConstructor
    @Transactional
    private static class MentionNotificationReaderService {
        private final MentionNotificationService mentionNotificationService;
        private final MentionRepository mentionRepository;

        private void readPostMentions(User currentUser) {
            Set<PostMention> receiveUnreadPostMentions = mentionNotificationService.getUnreadPostMentions(currentUser);
            receiveUnreadPostMentions.forEach(this::readMention);
            mentionRepository.saveAll(receiveUnreadPostMentions);
        }

        private void readCommentMentions(User currentUser) {
            Set<CommentMention> receiveUnreadCommentMentions = mentionNotificationService.getUnreadCommentMentions(currentUser);
            receiveUnreadCommentMentions.forEach(this::readMention);
            mentionRepository.saveAll(receiveUnreadCommentMentions);
        }

        private void readReplyMentions(User currentUser) {
            Set<ReplyMention> receiveUnreadReplyMentions = mentionNotificationService.getUnreadReplyMentions(currentUser);
            receiveUnreadReplyMentions.forEach(this::readMention);
            mentionRepository.saveAll(receiveUnreadReplyMentions);
        }

        private void readMention(Mention mention) {
            mention.setNotificationStatus(NotificationStatus.READ);
        }
    }
}
