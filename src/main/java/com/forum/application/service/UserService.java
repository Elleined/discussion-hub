package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Comment;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Post;
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
    private final CommentService commentService;
    private final ReplyService replyService;

    public int save(User user) {
        int userId = userRepository.save(user).getId();
        log.debug("User registered successfully! with id of {}", userId);
        return userId;
    }

    public int getIdByEmail(String email) {
        return userRepository.fetchIdByEmail(email);
    }

    public List<CommentDTO> getAllUnreadCommentsOf(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId + " does not exists!"));
        List<Post> posts = user.getPosts();

        return posts.stream()
                .map(Post::getComments)
                .flatMap(comments -> comments.stream()
                        .filter(comment -> comment.getNotificationStatus() == NotificationStatus.UNREAD))
                .map(commentService::convertToDTO)
                .toList();
    }

    public List<ReplyDTO> getAllUnreadReplyOf(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId + " does not exists!"));
        List<Comment> comments = user.getComments();

        return comments.stream()
                .map(Comment::getReplies)
                .flatMap(replies -> replies.stream()
                        .filter(reply -> reply.getNotificationStatus() == NotificationStatus.UNREAD))
                .map(replyService::convertToDTO)
                .toList();
    }

    public Set<User> getAllBlockedUsers(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId + " does not exists!"));
        return user.getBlockedUsers();
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

    public boolean isUserBlockedBy(int userId, int userToCheckId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId + " does not exists!"));
        User userToCheck = userRepository.findById(userToCheckId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userToCheckId + " does not exists!"));
        return user.getBlockedUsers().contains(userToCheck);
    }

    public boolean isEmailExists(String email) {
        return userRepository.fetchAllEmail().contains(email);
    }

    public User getById(int userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + userId +  " does not exists"));
    }
}
