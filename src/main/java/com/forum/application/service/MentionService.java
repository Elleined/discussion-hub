package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Mention;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Type;
import com.forum.application.model.User;
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
    private final ModalTrackerService modalTrackerService;

    private final MentionRepository mentionRepository;
    private final MentionHelper mentionHelper;

    int save(int mentioningUserId, int mentionedUserId, Type type, int typeId) throws ResourceNotFoundException {
        User mentioningUser = userRepository.findById(mentioningUserId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + mentioningUserId +  " does not exists"));
        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + mentionedUserId +  " does not exists"));

        int parentId = mentionHelper.getParentId(type, typeId);
        NotificationStatus notificationStatus  = modalTrackerService.isModalOpen(mentionedUserId, parentId, type) ? NotificationStatus.READ : NotificationStatus.UNREAD;
        Mention mention = Mention.builder()
                .mentioningUser(mentioningUser)
                .mentionedUser(mentionedUser)
                .type(type)
                .typeId(typeId)
                .createdAt(LocalDateTime.now())
                .notificationStatus(notificationStatus)
                .build();

        mentionRepository.save(mention);
        log.debug("User with id of {} mentioned user with id of {} successfully!", mentioningUserId, mentionedUserId);
        return mention.getId();
    }

    public Mention getById(int mentionId) throws ResourceNotFoundException {
        return mentionRepository.findById(mentionId).orElseThrow(() -> new ResourceNotFoundException("Mention with id of " + mentionId + " does not exists!"));
    }

    List<Mention> getAllUnreadReceiveMentions(int userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId +  " does not exists"));
        return user.getReceiveMentions()
                .stream()
                .filter(mention -> mention.getNotificationStatus() == NotificationStatus.UNREAD)
                .filter(mention -> !mentionHelper.isDeleted(mention.getType(), mention.getTypeId()))
                .toList();
    }

    public void readAllComments(int currentUserId, int postId) {
        mentionRepository.readAllComments(postId, currentUserId);
        log.debug("All unread mentions of the current user in post with id of {} are updated to READ", postId);
    }

    public void readAllReplies(int currentUserId, int commentId) {
        mentionRepository.readAllReplies(commentId, currentUserId);
        log.debug("All unread mentions of the current user in comment with id of {} are updated to READ", commentId);
    }
}
