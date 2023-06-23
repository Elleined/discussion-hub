package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.User;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BlockService {

    private final UserRepository userRepository;

    public void blockUser(int userId, int userToBeBlockedId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId + " does not exists!"));
        User userToBeBlocked = userRepository.findById(userToBeBlockedId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userToBeBlockedId + " does not exists!"));
        user.getBlockedUsers().add(userToBeBlocked);
        userRepository.save(user);
        log.debug("User {} blocked User {} successfully", userId, userToBeBlockedId);
    }

    public void unBlockUser(int userId, int userToBeUnblockedId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId + " does not exists!"));
        User userToBeUnBlocked = userRepository.findById(userToBeUnblockedId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userToBeUnblockedId + " does not exists!"));
        user.getBlockedUsers().remove(userToBeUnBlocked);
        userRepository.save(user);
        log.debug("User {} unblocked user {} successfully", userId, userToBeUnblockedId);
    }

    public boolean isBlockedBy(int userId, int userToCheckId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId + " does not exists!"));
        return user.getBlockedUsers().stream().anyMatch(blockedUser -> blockedUser.getId() == userToCheckId);
    }

    public boolean isYouBeenBlockedBy(int userId, int suspectedUserId) {
        User suspected = userRepository.findById(suspectedUserId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + suspectedUserId + " does not exists!"));
        return suspected.getBlockedUsers().stream().anyMatch(blockedUser -> blockedUser.getId() == userId);
    }
}
