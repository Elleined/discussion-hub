package com.forum.application.service;

import com.forum.application.dto.MentionDTO;
import com.forum.application.dto.UserDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Mention;
import com.forum.application.model.ModalTracker;
import com.forum.application.model.Type;
import com.forum.application.model.User;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BlockService blockService;
    private final ModalTrackerService modalTrackerService;
    private final MentionService mentionService;

    public int save(User user) {
        int userId = userRepository.save(user).getId();
        log.debug("User registered successfully! with id of {}", userId);
        return userId;
    }

    public int getIdByEmail(String email) {
        return userRepository.fetchIdByEmail(email);
    }

    public boolean isEmailExists(String email) {
        return userRepository.fetchAllEmail().contains(email);
    }

    public boolean existsById(int userId) {
        return userRepository.existsById(userId);
    }

    public User getById(int userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId +  " does not exists"));
    }

    public List<UserDTO> getAllByProperty(String name) {
        return userRepository.fetchAllByProperty(name)
                .stream()
                .map(this::convertToDTO)
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
                .map(this::convertToDTO)
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

    public int mentionUser(int mentioningUserId, int mentionedUserId, Type type, int typeId) {
        return mentionService.save(mentioningUserId, mentionedUserId, type, typeId);
    }

    public MentionDTO getMentionById(int mentionId) {
        Mention mention = mentionService.getById(mentionId);
        return mentionService.convertToDTO(mention);
    }

    public List<MentionDTO> getAllUnreadReceiveMentions(int userId) {
        return mentionService.getAllUnreadReceiveMentions(userId);
    }

    public void deleteAllReceiveMentions(int userId) {
        mentionService.deleteAllReceiveMentions(userId);
    }

    public UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .picture(user.getPicture())
                .name(user.getName())
                .build();
    }
}
