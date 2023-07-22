package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Post;
import com.forum.application.model.Type;
import com.forum.application.model.User;
import com.forum.application.model.like.PostLike;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.PostRepository;
import com.forum.application.repository.ReplyRepository;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LikeService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final ModalTrackerService modalTrackerService;

    Post addPostLike(int userId, int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        User respondent = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId +  " does not exists"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(userId, post.getId(), Type.POST)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        PostLike postLike = PostLike.postLikeBuilder()
                .post(post)
                .respondent(respondent)
                .notificationStatus(notificationStatus)
                .build();

        post.getLikes().add(postLike);
        respondent.getLikedPosts().add(postLike);

        return post;
    }

}
