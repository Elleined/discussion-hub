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
    private final MentionRepository mentionRepository;
    private final ModalTrackerService modalTrackerService;
    private final BlockService blockService;

    int save(int mentioningUserId, int mentionedUserId, Type type, int typeId) throws ResourceNotFoundException {
        User mentioningUser = userRepository.findById(mentioningUserId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + mentioningUserId +  " does not exists"));
        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + mentionedUserId +  " does not exists"));

        NotificationStatus notificationStatus  = modalTrackerService.isModalOpen(mentionedUserId, typeId, type) ? NotificationStatus.READ : NotificationStatus.UNREAD;
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

    List<User> getSuggestedMentions(int userId, String name) {
        return userRepository.fetchAllByProperty(name)
                .stream()
                .filter(user -> user.getId() != userId)
                .filter(user -> !blockService.isBlockedBy(userId, user.getId()))
                .filter(user -> !blockService.isYouBeenBlockedBy(userId, user.getId()))
                .toList();
    }

    public Mention getById(int mentionId) throws ResourceNotFoundException {
        return mentionRepository.findById(mentionId).orElseThrow(() -> new ResourceNotFoundException("Mention with id of " + mentionId + " does not exists!"));
    }

    List<Mention> getAllUnreadReceiveMentions(int userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId +  " does not exists"));
        return user.getReceiveMentions()
                .stream()
                .filter(mention -> mention.getNotificationStatus() == NotificationStatus.UNREAD)
                .toList();
    }
}
