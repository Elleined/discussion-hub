package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.model.mention.CommentMention;
import com.forum.application.model.mention.PostMention;
import com.forum.application.model.mention.ReplyMention;
import com.forum.application.repository.MentionRepository;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MentionService {

    private final UserRepository userRepository;
    private final MentionRepository mentionRepository;
    private final ModalTrackerService modalTrackerService;

    Post addPostMention(User currentUser, int mentionedUserId, Post post) {
        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("Mentioned user with id of " + mentionedUserId + " doesn't exists!"));
        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(mentionedUserId, post.getId(), ModalTracker.Type.POST)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        PostMention postMention = PostMention.postMentionBuilder()
                .mentioningUser(currentUser)
                .mentionedUser(mentionedUser)
                .notificationStatus(notificationStatus)
                .createdAt(LocalDateTime.now())
                .post(post)
                .build();

        currentUser.getSentPostMentions().add(postMention);
        mentionedUser.getReceivePostMentions().add(postMention);
        post.getMentions().add(postMention);
        mentionRepository.save(postMention);
        log.debug("User with id of {} mentioned user with id of {} in post with id of {}", currentUser.getId(), mentionedUserId, post.getId());
        return post;
    }

    Comment addCommentMention(User currentUser, int mentionedUserId, Comment comment) {
        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("Mentioned user with id of " + mentionedUserId + " doesn't exists!"));
        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(mentionedUserId, comment.getId(), ModalTracker.Type.COMMENT)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        CommentMention commentMention = CommentMention.commentMentionBuilder()
                .mentioningUser(currentUser)
                .mentionedUser(mentionedUser)
                .notificationStatus(notificationStatus)
                .createdAt(LocalDateTime.now())
                .comment(comment)
                .build();

        currentUser.getSentCommentMentions().add(commentMention);
        mentionedUser.getReceiveCommentMentions().add(commentMention);
        comment.getMentions().add(commentMention);
        mentionRepository.save(commentMention);
        log.debug("User with id of {} mentioned user with id of {} in comment with id of {}", currentUser.getId(), mentionedUserId, comment.getId());
        return comment;
    }

    Reply addReplyMention(User currentUser, int mentionedUserId, Reply reply) {
        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("Mentioned user with id of " + mentionedUserId + " doesn't exists!"));
        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(mentionedUserId, reply.getId(), ModalTracker.Type.POST)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        ReplyMention replyMention = ReplyMention.replyMentionBuilder()
                .mentioningUser(currentUser)
                .mentioningUser(mentionedUser)
                .notificationStatus(notificationStatus)
                .createdAt(LocalDateTime.now())
                .reply(reply)
                .build();

        currentUser.getSentReplyMentions().add(replyMention);
        mentionedUser.getReceiveReplyMentions().add(replyMention);
        reply.getMentions().add(replyMention);
        log.debug("User with id of {} mentioned user with id of {} in reply with id of {}", currentUser.getId(), mentionedUserId, reply.getId());
        return reply;
    }

    public List<User> getSuggestedMentions(String name) {
        return userRepository.fetchAllByProperty(name);
    }
}
