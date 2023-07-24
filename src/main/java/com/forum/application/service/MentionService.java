package com.forum.application.service;

import com.forum.application.repository.MentionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MentionService {

    private MentionsRepository mentionsRepository;

    int addPostMention(int userId, int postId) {

    }
}
