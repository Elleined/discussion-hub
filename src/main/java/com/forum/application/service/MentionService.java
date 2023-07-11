package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.repository.*;
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
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    private final MentionRepository mentionRepository;
    private final ModalTrackerService modalTrackerService;

    int save(int mentioningUserId, int mentionedUserId, Type type, int typeId) throws ResourceNotFoundException {
        User mentioningUser = userRepository.findById(mentioningUserId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + mentioningUserId +  " does not exists"));
        User mentionedUser = userRepository.findById(mentionedUserId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + mentionedUserId +  " does not exists"));

        int parentId = getParentId(type, typeId);
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
                .filter(mention -> !isDeleted(mention.getType(), mention.getTypeId()))
                .toList();
    }

    private boolean isDeleted(Type type, int typeId) {
        return switch (type) {
            case POST -> postRepository.findById(typeId).orElseThrow().getStatus() == Status.INACTIVE;
            case COMMENT -> commentRepository.findById(typeId).orElseThrow().getStatus() == Status.INACTIVE;
            case REPLY -> replyRepository.findById(typeId).orElseThrow().getStatus() == Status.INACTIVE;
        };
    }

    private int getParentId(Type type, int typeId) {
        return switch (type) {
            case COMMENT -> commentRepository.findById(typeId).orElseThrow().getPost().getId();
            case REPLY -> replyRepository.findById(typeId).orElseThrow().getComment().getId();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
