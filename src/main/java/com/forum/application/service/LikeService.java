package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.model.like.CommentLike;
import com.forum.application.model.like.PostLike;
import com.forum.application.model.like.ReplyLike;
import com.forum.application.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class LikeService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final ModalTrackerService modalTrackerService;

    @Transactional
    int addPostLike(int respondentId, int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(respondentId, post.getId(), Type.POST)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        PostLike postLike = PostLike.postLikeBuilder()
                .post(post)
                .respondent(respondent)
                .notificationStatus(notificationStatus)
                .build();

        likeRepository.save(postLike);
        log.debug("User with id of {} liked post with id of {}", respondentId, postId);
        return postId;
    }


    int addCommentLike(int respondentId, int commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(respondentId, comment.getId(), Type.POST)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        CommentLike commentLike = CommentLike.commentLikeBuilder()
                .comment(comment)
                .respondent(respondent)
                .notificationStatus(notificationStatus)
                .build();

        likeRepository.save(commentLike);
        log.debug("User with id of {} liked comment with id of {}", respondent, commentId);
        return commentId;
    }

    int addReplyLike(int respondentId, int replyId) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId +  " does not exists"));

        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(respondentId, reply.getId(), Type.POST)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        ReplyLike replyLike = ReplyLike.replyLikeBuilder()
                .reply(reply)
                .respondent(respondent)
                .notificationStatus(notificationStatus)
                .build();

        likeRepository.save(replyLike);
        log.debug("User with id of {} liked reply with id of {}", respondentId, replyId);
        return replyId;
    }
}
