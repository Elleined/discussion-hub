package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.ModalTracker;
import com.forum.application.model.User;
import com.forum.application.repository.ModalTrackerRepository;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModalTrackerRepository modalTrackerRepository;
    private final BlockService blockService;

    public int save(User user) {
        int userId = userRepository.save(user).getId();
        log.debug("User registered successfully! with id of {}", userId);
        return userId;
    }

    public int getIdByEmail(String email) {
        return userRepository.fetchIdByEmail(email);
    }

    public ModalTracker getTrackerOfUserById(int userId) {
        return modalTrackerRepository.findById(userId).orElse(null);
    }

    public void deleteTrackerOfUserById(int userId) {
        modalTrackerRepository.deleteById(userId);
    }

    public Set<User> getAllBlockedUsers(int userId) {
        return userRepository.fetchAllBlockedUserOf(userId);
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

    public boolean isEmailExists(String email) {
        return userRepository.fetchAllEmail().contains(email);
    }

    public boolean existsById(int userId) {
        return userRepository.existsById(userId);
    }

    public User getById(int userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId +  " does not exists"));
    }
}
