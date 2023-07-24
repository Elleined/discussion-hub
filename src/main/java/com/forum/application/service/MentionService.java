package com.forum.application.service;

import com.forum.application.model.ModalTracker;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Post;
import com.forum.application.model.User;
import com.forum.application.model.mention.PostMention;
import com.forum.application.repository.MentionRepository;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MentionService {

    private final UserRepository userRepository;
    private final MentionRepository mentionRepository;
    private final ModalTrackerService modalTrackerService;

    Post addPostMention(User currentUser, User mentionedUser, Post post) {
        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(mentionedUser.getId(), post.getId(), ModalTracker.Type.POST)
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
        log.debug("User with id of {} mentioned user with id of {} in post with id of {}", currentUser.getId(), mentionedUser.getId(), post.getId());
        return post;
    }

    List<Post> addAllPostMention(User currentUser, Set<User> mentionedUsers, Post post) {
        return mentionedUsers.stream()
                .map(mentionedUser -> addPostMention(currentUser, mentionedUser, post))
                .toList();
    }

    public List<User> getSuggestedMentions(String name) {
        return userRepository.fetchAllByProperty(name);
    }

    public Set<User> getAllBlockedUsers(int currentUserId) {
        return userRepository.fetchAllBlockedUserOf(currentUserId);
    }
}
