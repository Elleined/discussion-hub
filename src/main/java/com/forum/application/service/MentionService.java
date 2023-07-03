package com.forum.application.service;

import com.forum.application.dto.MentionDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
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

    int save(int mentioningUserId, int mentionedUserId, Type type, int typeId) {
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
                .status(Status.ACTIVE)
                .build();

        mentionRepository.save(mention);
        log.debug("User with id of {} mentioned user with id of {} successfully!", mentioningUserId, mentionedUserId);
        return mention.getId();
    }

    List<User> getAllByProperty(int userId, String name) {
        return userRepository.fetchAllByProperty(name)
                .stream()
                .filter(user -> user.getId() != userId)
                .filter(user -> !blockService.isBlockedBy(userId, user.getId()))
                .filter(user -> !blockService.isYouBeenBlockedBy(userId, user.getId()))
                .toList();
    }

    Mention getById(int mentionId) {
        return mentionRepository.findById(mentionId).orElseThrow(() -> new ResourceNotFoundException("Mention with id of " + mentionId + " does not exists!"));
    }

    void deleteAllReceiveMentions(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId +  " does not exists"));
        user.getReceiveMentions().forEach(mention -> {
            mention.setStatus(Status.INACTIVE);
            mentionRepository.save(mention);
        });
    }

    List<MentionDTO> getAllUnreadReceiveMentions(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId +  " does not exists"));
        return user.getReceiveMentions()
                .stream()
                .filter(mention -> mention.getStatus() == Status.ACTIVE)
                .filter(mention -> mention.getNotificationStatus() == NotificationStatus.UNREAD)
                .map(this::convertToDTO)
                .toList();
    }

    public MentionDTO convertToDTO(Mention mention) {
        return MentionDTO.builder()
                .mentioningUserId(mention.getMentioningUser().getId())
                .mentionedUserId(mention.getMentionedUser().getId())
                .type(mention.getType().name())
                .typeId(mention.getTypeId())
                .notificationStatus(mention.getNotificationStatus().name())
                .createdAt(mention.getCreatedAt())
                .build();
    }
}
