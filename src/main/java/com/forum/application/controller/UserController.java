package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.model.User;
import com.forum.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}/unreadComments")
    public List<CommentDTO> getAllUnreadComments(@PathVariable("userId") int userId) {
        return userService.getAllUnreadCommentsOf(userId);
    }

    @GetMapping("/{userId}/unreadReplies")
    public List<ReplyDTO> getAllUnreadReplies(@PathVariable("userId") int userId) {
        return userService.getAllUnreadReplyOf(userId);
    }

    @GetMapping("/{userId}/getAllBlockedUsers")
    public Set<User> getAllBlockedUserOf(@PathVariable("userId") int userId) {
        return userService.getAllBlockedUsers(userId);
    }

    @PatchMapping("/{userId}/blockUser")
    public ResponseEntity<String> blockUser(@PathVariable("userId") int userId,
                                            @RequestParam("userToBeBlocked") int userToBeBlockedId) {

        userService.blockUser(userId, userToBeBlockedId);
        return ResponseEntity.ok("User with id of " + userToBeBlockedId + " blocked successfully");
    }

    @PatchMapping("/{userId}/unBlockUser")
    public ResponseEntity<String> unblockUser(@PathVariable("userId") int userId,
                                              @RequestParam("userToBeUnblockedId") int userToBeUnblockedId) {
        userService.unBlockUser(userId, userToBeUnblockedId);
        return ResponseEntity.ok("User with id of " + userToBeUnblockedId + " unblocked successfully");
    }
}
