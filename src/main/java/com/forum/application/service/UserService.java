package com.forum.application.service;

import com.forum.application.dto.MentionResponse;
import com.forum.application.dto.UserDTO;
import com.forum.application.exception.BlockedException;
import com.forum.application.exception.NoLoggedInUserException;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.mapper.NotificationMapper;
import com.forum.application.mapper.UserMapper;
import com.forum.application.model.ModalTracker;
import com.forum.application.model.Type;
import com.forum.application.model.User;
import com.forum.application.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BlockService blockService;
    private final ModalTrackerService modalTrackerService;
    private final MentionService mentionService;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final HttpSession session;
    public int save(User user) {
        int userId = userRepository.save(user).getId();
        log.debug("User registered successfully! with id of {}", userId);
        return userId;
    }

    private int getIdByEmail(String email) {
        return userRepository.fetchIdByEmail(email);
    }

    public User getCurrentUser() throws NoLoggedInUserException {
        String loginEmailSession = (String) session.getAttribute("email");
        if (loginEmailSession == null) throw new NoLoggedInUserException("However you see this error message please login first in the browser then come back here!");
        int userId = getIdByEmail(loginEmailSession);
        return getById(userId);
    }

    public boolean isEmailExists(String email) {
        return userRepository.fetchAllEmail().contains(email);
    }

    public boolean existsById(int userId) {
        return userRepository.existsById(userId);
    }

    public User getById(int userId) throws ResourceNotFoundException {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId +  " does not exists"));
    }

    public List<UserDTO> getSuggestedMentions(int userId, String name) {
        return mentionService.getSuggestedMentions(userId, name)
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public boolean isModalOpen(int userId, int associatedTypeId, Type type) {
        return modalTrackerService.isModalOpen(userId, associatedTypeId, type);
    }

    public ModalTracker saveTrackerOfUserById(int receiverId, int associateTypeIdOpened, String type) {
        return modalTrackerService.saveTrackerOfUserById(receiverId, associateTypeIdOpened, type);
    }

    public ModalTracker getTrackerOfUserById(int userId) {
        return modalTrackerService.getTrackerOfUserById(userId);
    }

    public void deleteTrackerOfUserById(int userId, String type) {
        modalTrackerService.deleteTrackerOfUserById(userId, Type.valueOf(type));
    }


    public Set<UserDTO> getAllBlockedUsers(int userId) {
        return userRepository.fetchAllBlockedUserOf(userId)
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toSet());
    }

    public void blockUser(int userId, int userToBeBlockedId) {
        blockService.blockUser(userId, userToBeBlockedId);
    }

    public void unBlockUser(int userId, int userToBeUnblockedId) {
        blockService.unBlockUser(userId, userToBeUnblockedId);
    }

    public boolean isBlockedBy(int userId, int userToCheckId) {
        return blockService.isBlockedBy(userId, userToCheckId);
    }

    public boolean isYouBeenBlockedBy(int userId, int suspectedUserId) {
        return blockService.isYouBeenBlockedBy(userId, suspectedUserId);
    }

    public Integer mentionUser(int mentioningUserId, int mentionedUserId, Type type, int typeId) throws BlockedException {
        boolean isBlockedBy = isBlockedBy(mentioningUserId, mentionedUserId);
        boolean isYouBeenBlockedBy = isYouBeenBlockedBy(mentioningUserId, mentionedUserId);
        if (isBlockedBy || isYouBeenBlockedBy) throw new BlockedException("Cannot mention user! One of the mentioned user blocked you!");
        return mentionService.save(mentioningUserId, mentionedUserId, type, typeId);
    }

    public Set<Integer> mentionUsers(int mentioningUserId, Set<Integer> usersToBeMentionIds, Type type, int typeId) throws BlockedException {
        return usersToBeMentionIds.stream()
                .map(mentionedUserId -> this.mentionUser(mentioningUserId, mentionedUserId, type, typeId))
                .collect(Collectors.toSet());
    }

    public MentionResponse getMentionById(int mentionId) {
        return notificationMapper.toMentionNotification(mentionId);
    }

    public List<MentionResponse> getAllUnreadReceiveMentions(int userId) {
        return mentionService.getAllUnreadReceiveMentions(userId);
    }
}
