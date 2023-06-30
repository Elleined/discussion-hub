package com.forum.application.service;

import com.forum.application.dto.MentionDTO;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MentionService {
    private final UserRepository userRepository;
    private final ModalTrackerService modalTrackerService;
    private final MentionRepository mentionRepository;

    int save(int mentioningUserId, int mentionedUserId, Type type, int typeId) {
        User mentioningUser = userRepository.findById(mentioningUserId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + mentioningUserId +  " does not exists"));
        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + mentionedUserId +  " does not exists"));

        NotificationStatus status = modalTrackerService.isModalOpen(mentionedUserId, typeId, type) ? NotificationStatus.READ : NotificationStatus.UNREAD;
        Mention mention = Mention.builder()
                .mentioningUser(mentioningUser)
                .mentionedUser(mentionedUser)
                .type(type)
                .typeId(typeId)
                .createdAt(LocalDateTime.now())
                .status(status)
                .build();

        mentionRepository.save(mention);
        log.debug("User with id of {} mentioned user with id of {} successfully!", mentioningUserId, mentionedUserId);
        return mention.getId();
    }

    public Mention getById(int mentionId) {
        return mentionRepository.findById(mentionId).orElseThrow(() -> new ResourceNotFoundException("Mention with id of " + mentionId + " does not exists!"));
    }

    public List<MentionDTO> getAllUnreadReceiveMentions(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId +  " does not exists"));
        return user.getReceiveMentions()
                .stream()
                .filter(mention -> mention.getStatus() == NotificationStatus.UNREAD)
                .map(this::convertToDTO)
                .toList();
    }

    public MentionDTO convertToDTO(Mention mention) {
        return MentionDTO.builder()
                .mentioningUserId(mention.getMentioningUser().getId())
                .mentionedUserId(mention.getMentionedUser().getId())
                .type(mention.getType().name())
                .typeId(mention.getTypeId())
                .notificationStatus(mention.getStatus().name())
                .createdAt(mention.getCreatedAt())
                .build();
    }
}
