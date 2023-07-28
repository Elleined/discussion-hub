package com.forum.application.service;

import com.forum.application.exception.BlockedException;
import com.forum.application.exception.MentionException;
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

    private final PostService postService;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final BlockService blockService;

    Mention addMention(User currentUser, int mentionedUserId, Post post) throws ResourceNotFoundException,
            BlockedException,
            MentionException {
        if (postService.isDeleted(post)) throw new ResourceNotFoundException("Cannot mention! The post with id of " + post.getId() + " you are trying to mention might already been deleted or does not exists!");
        if (blockService.isBlockedBy(currentUser.getId(), mentionedUserId)) throw new BlockedException("Cannot mention! You blocked the mentioned user with id of !" + mentionedUserId);
        if (blockService.isYouBeenBlockedBy(currentUser.getId(), mentionedUserId)) throw  new BlockedException("Cannot mention! Mentioned user with id of " + mentionedUserId + " already blocked you");
        if (currentUser.getId() == mentionedUserId) throw new MentionException("Cannot mention! You are trying to mention yourself which is not possible!");

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
        return postMention;
    }

    Mention addMention(User currentUser, int mentionedUserId, Comment comment) throws ResourceNotFoundException,
            BlockedException,
            MentionException {

        if (commentService.isDeleted(comment)) throw new ResourceNotFoundException("Cannot mention! The comment with id of " + comment.getId() + " you are trying to mention might already been deleted or does not exists!");
        if (blockService.isBlockedBy(currentUser.getId(), mentionedUserId)) throw new BlockedException("Cannot mention! You blocked the mentioned user with id of !" + mentionedUserId);
        if (blockService.isYouBeenBlockedBy(currentUser.getId(), mentionedUserId)) throw  new BlockedException("Cannot mention! Mentioned user with id of " + mentionedUserId + " already blocked you");
        if (currentUser.getId() == mentionedUserId) throw new MentionException("Cannot mention! You are trying to mention yourself which is not possible!");

        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("Mentioned user with id of " + mentionedUserId + " doesn't exists!"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(mentionedUserId, comment.getPost().getId(), ModalTracker.Type.COMMENT)
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
        return commentMention;
    }

    Mention addMention(User currentUser, int mentionedUserId, Reply reply) throws ResourceNotFoundException,
            BlockedException,
            MentionException {

        if (replyService.isDeleted(reply)) throw new ResourceNotFoundException("Cannot mention! The reply with id of " + reply.getId() + " you are trying to mention might already be deleted or does not exists!");
        if (blockService.isBlockedBy(currentUser.getId(), mentionedUserId)) throw new BlockedException("Cannot mention! You blocked the mentioned user with id of !" + mentionedUserId);
        if (blockService.isYouBeenBlockedBy(currentUser.getId(), mentionedUserId)) throw new BlockedException("Cannot mention! Mentioned userwith id of " + mentionedUserId + " already blocked you");
        if (currentUser.getId() == mentionedUserId) throw new MentionException("Cannot mention! You are trying to mention yourself which is not possible!");

        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("Mentioned user with id of " + mentionedUserId + " doesn't exists!"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(mentionedUserId, reply.getComment().getId(), ModalTracker.Type.REPLY)
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
        return replyMention;
    }

    Set<Mention> addAllMention(User currentUser, Set<Integer> mentionedUserIds, Post post) throws ResourceNotFoundException,
            BlockedException,
            MentionException {

        return mentionedUserIds.stream()
                .map(mentionedUserId -> addMention(currentUser, mentionedUserId, post))
                .collect(Collectors.toSet());
    }

    Set<Mention> addAllMention(User currentUser, Set<Integer> mentionedUserIds, Comment comment) throws ResourceNotFoundException,
            BlockedException,
            MentionException {

        return mentionedUserIds.stream()
                .map(mentionedUserId -> addMention(currentUser, mentionedUserId, comment))
                .collect(Collectors.toSet());
    }

    Set<Mention> addAllMention(User currentUser, Set<Integer> mentionedUserIds, Reply reply) throws ResourceNotFoundException,
            BlockedException,
            MentionException {

        return mentionedUserIds.stream()
                .map(mentionedUserId -> addMention(currentUser, mentionedUserId, reply))
                .collect(Collectors.toSet());
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
    private static class MentionNotificationReaderService {
        private final MentionNotificationService mentionNotificationService;
        private final MentionRepository mentionRepository;

        private void readPostMentions(User currentUser) {
            Set<PostMention> receiveUnreadPostMentions = mentionNotificationService.getUnreadPostMentions(currentUser);
            receiveUnreadPostMentions.forEach(this::readMention);
            mentionRepository.saveAll(receiveUnreadPostMentions);
            log.debug("Reading all post mentions for current user with id of {} success", currentUser.getId());
        }

        private void readCommentMentions(User currentUser) {
            Set<CommentMention> receiveUnreadCommentMentions = mentionNotificationService.getUnreadCommentMentions(currentUser);
            receiveUnreadCommentMentions.forEach(this::readMention);
            mentionRepository.saveAll(receiveUnreadCommentMentions);
            log.debug("Reading all comment mentions for current user with id of {} success", currentUser.getId());
        }

        private void readReplyMentions(User currentUser) {
            Set<ReplyMention> receiveUnreadReplyMentions = mentionNotificationService.getUnreadReplyMentions(currentUser);
            receiveUnreadReplyMentions.forEach(this::readMention);
            mentionRepository.saveAll(receiveUnreadReplyMentions);
            log.debug("Reading all reply mentions for current user with id of {} success", currentUser.getId());
        }

        private void readMention(Mention mention) {
            mention.setNotificationStatus(NotificationStatus.READ);
        }
    }
}
