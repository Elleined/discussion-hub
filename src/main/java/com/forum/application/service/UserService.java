package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.User;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public int save(User user) {
        int userId = userRepository.save(user).getId();
        log.debug("User registered successfully! with id of {}", userId);
        return userId;
    }

    public int getIdByEmail(String email) {
        return userRepository.fetchIdByEmail(email);
    }

    public Set<User> getAllBlockedUsers(int userId) {
        return userRepository.fetchAllBlockedUserOf(userId);
    }

    public void blockUser(int userId, int userToBeBlockedId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId + " does not exists!"));
        User userToBeBlocked = userRepository.findById(userToBeBlockedId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userToBeBlockedId + " does not exists!"));
        user.getBlockedUsers().add(userToBeBlocked);
        userRepository.save(user);
        log.debug("User with id of {} blocked successfully", userToBeBlockedId);
    }

    public void unBlockUser(int userId, int userToBeUnblockedId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId + " does not exists!"));
        User userToBeUnBlocked = userRepository.findById(userToBeUnblockedId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userToBeUnblockedId + " does not exists!"));
        user.getBlockedUsers().remove(userToBeUnBlocked);
        userRepository.save(user);
        log.debug("User with id of {} unblocked successfully", userToBeUnblockedId);
    }

    public boolean notBlockedBy(int userId, int userToCheckId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId + " does not exists!"));
        User userToCheck = userRepository.findById(userToCheckId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userToCheckId + " does not exists!"));
        return !user.getBlockedUsers().contains(userToCheck);
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
