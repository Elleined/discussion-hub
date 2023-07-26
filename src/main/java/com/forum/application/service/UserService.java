package com.forum.application.service;

import com.forum.application.dto.UserDTO;
import com.forum.application.exception.NoLoggedInUserException;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.User;
import com.forum.application.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
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

    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    public List<User> getSuggestedMentions(String name) {
        return userRepository.fetchAllByProperty(name);
    }
}
