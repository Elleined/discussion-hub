package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.model.ModalTracker;
import com.forum.application.model.User;
import com.forum.application.service.CommentService;
import com.forum.application.service.ReplyService;
import com.forum.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/api/users/{userId}")
public class UserController {
    private final UserService userService;
    private final CommentService commentService;
    private final ReplyService replyService;

    @GetMapping("/unreadComments")
    public List<CommentDTO> getAllUnreadComments(@PathVariable("userId") int userId) {
        return commentService.getAllUnreadCommentsOf(userId);
    }

    @GetMapping("/unreadReplies")
    public List<ReplyDTO> getAllUnreadReplies(@PathVariable("userId") int userId) {
        return replyService.getAllUnreadReplyOf(userId);
    }

    @GetMapping("/getAllBlockedUsers")
    public Set<User> getAllBlockedUserOf(@PathVariable("userId") int userId) {
        return userService.getAllBlockedUsers(userId);
    }

    @PatchMapping("/blockUser/{userToBeBlockedId}")
    public ResponseEntity<String> blockUser(@PathVariable("userId") int userId,
                                            @PathVariable("userToBeBlockedId") int userToBeBlockedId) {

        userService.blockUser(userId, userToBeBlockedId);
        return ResponseEntity.ok("User with id of " + userToBeBlockedId + " blocked successfully");
    }

    @PatchMapping("/unblockUser/{userToBeUnblockedId}")
    public ResponseEntity<String> unblockUser(@PathVariable("userId") int userId,
                                              @PathVariable("userToBeUnblockedId") int userToBeUnblockedId) {
        userService.unBlockUser(userId, userToBeUnblockedId);
        return ResponseEntity.ok("User with id of " + userToBeUnblockedId + " unblocked successfully");
    }

    @GetMapping("/getTracker")
    public ModalTracker getTrackerByUserId(@PathVariable("userId") int userId) {
        return userService.getTrackerOfUserById(userId);
    }

    @GetMapping("/isBlockedBy/{userToCheckId}")
    public boolean isBlockedBy(@PathVariable("userId") int userId,
                               @PathVariable("userToCheckId") int userToCheckId) {
        return userService.isBlockedBy(userId, userToCheckId);
    }

    @GetMapping("/isYouBeenBlockedBy/{suspectedBlockerId}")
    public boolean isYouBeenBlockedBy(@PathVariable("userId") int userId,
                                      @PathVariable("suspectedBlockerId") int suspectedBlockerId) {
        return userService.isYouBeenBlockedBy(userId, suspectedBlockerId);
    }
}
