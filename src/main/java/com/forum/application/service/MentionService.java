package com.forum.application.service;

import com.forum.application.model.User;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MentionService {

    private final UserRepository userRepository;

    int addPostMention(int userId, int postId) {
        return 0;
    }

    public List<User> getSuggestedMentions(int userId, String name) {
        return userRepository.fetchAllByProperty(name);
    }

    public Set<User> getAllBlockedUsers(int userId) {
        return userRepository.fetchAllBlockedUserOf(userId);
    }
}
