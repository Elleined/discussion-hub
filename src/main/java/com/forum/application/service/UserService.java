package com.forum.application.service;

import com.forum.application.dto.MentionDTO;
import com.forum.application.dto.UserDTO;
import com.forum.application.exception.NoLoggedInUserException;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.mapper.MentionMapper;
import com.forum.application.mapper.UserMapper;
import com.forum.application.model.Mention;
import com.forum.application.model.ModalTracker;
import com.forum.application.model.Type;
import com.forum.application.model.User;
import com.forum.application.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final MentionMapper mentionMapper;
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

    public List<UserDTO> getAllByProperty(int userId, String name) {
        return mentionService.getAllByProperty(userId, name)
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

    public int mentionUser(int mentioningUserId, int mentionedUserId, Type type, int typeId) {
        return mentionService.save(mentioningUserId, mentionedUserId, type, typeId);
    }

    public MentionDTO getMentionById(int mentionId) {
        Mention mention = mentionService.getById(mentionId);
        return mentionMapper.toDTO(mention);
    }

    public List<MentionDTO> getAllUnreadReceiveMentions(int userId) {
        return mentionService.getAllUnreadReceiveMentions(userId);
    }
}
