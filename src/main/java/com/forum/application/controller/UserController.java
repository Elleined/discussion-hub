package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.NotificationResponse;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.dto.UserDTO;
import com.forum.application.model.ModalTracker;
import com.forum.application.service.CommentService;
import com.forum.application.service.ForumService;
import com.forum.application.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/api/users/{userId}")
public class UserController {
    private final ForumService forumService;
    private final CommentService commentService;
    private final ReplyService replyService;

    @GetMapping("/getAllNotification")
    public Set<NotificationResponse> getAllNotification(@PathVariable("userId") int userId) {
        return forumService.getAllNotification(userId);
    }

    @GetMapping("/unreadComments/{postId}")
    public List<CommentDTO> getAllUnreadComments(@PathVariable("userId") int authorId,
                                                 @PathVariable("postId") int postId) {
        return commentService.getAllUnreadComments(authorId, postId);
    }

    @GetMapping("/unreadCommentCountOfSpecificPost/{postId}")
    public int getNotificationCountForSpecificPost(@PathVariable("userId") int authorId,
                                                   @PathVariable("postId") int postId) {
        return commentService.getNotificationCountForSpecificPost(authorId, postId);
    }

    @GetMapping("/unreadCommentCountForRespondent/{postId}/{respondentId}")
    public int getCommentNotificationCountForRespondent(@PathVariable("userId") int authorId,
                                                        @PathVariable("postId") int postId,
                                                        @PathVariable("respondentId") int respondentId) {
        return commentService.getNotificationCountForRespondent(authorId, postId, respondentId);
    }

    @GetMapping("/unreadReplies/{commentId}")
    public List<ReplyDTO> getAllUnreadReplies(@PathVariable("userId") int commenterId,
                                              @PathVariable("commentId") int commentId) {
        return replyService.getAllUnreadReplies(commenterId, commentId);
    }

    @GetMapping("/unreadReplyCountOfSpecificComment/{commentId}")
    public int getReplyCountForSpecificComment(@PathVariable("userId") int commenterId,
                                               @PathVariable("commentId") int commentId) {
        return replyService.getReplyNotificationCountForSpecificComment(commenterId, commentId);
    }

    @GetMapping("/unreadReplyCountForRespondent/{commentId}/{respondentId}")
    public int getReplyNotificationCountForRespondent(@PathVariable("userId") int commenterId,
                                                      @PathVariable("commentId") int commentId,
                                                      @PathVariable("respondentId") int respondentId) {
        return replyService.getNotificationCountForRespondent(commenterId, commentId, respondentId);
    }

    @GetMapping("/getAllBlockedUsers")
    public Set<UserDTO> getAllBlockedUserOf(@PathVariable("userId") int userId) {
        return forumService.getAllBlockedUsers(userId);
    }

    @PatchMapping("/blockUser/{userToBeBlockedId}")
    public ResponseEntity<String> blockUser(@PathVariable("userId") int userId,
                                            @PathVariable("userToBeBlockedId") int userToBeBlockedId) {

        forumService.blockUser(userId, userToBeBlockedId);
        return ResponseEntity.ok("User with id of " + userToBeBlockedId + " blocked successfully");
    }

    @PatchMapping("/unblockUser/{userToBeUnblockedId}")
    public ResponseEntity<String> unblockUser(@PathVariable("userId") int userId,
                                              @PathVariable("userToBeUnblockedId") int userToBeUnblockedId) {
        forumService.unBlockUser(userId, userToBeUnblockedId);
        return ResponseEntity.ok("User with id of " + userToBeUnblockedId + " unblocked successfully");
    }

    @GetMapping("/isBlockedBy/{userToCheckId}")
    public boolean isBlockedBy(@PathVariable("userId") int userId,
                               @PathVariable("userToCheckId") int userToCheckId) {
        return forumService.isBlockedBy(userId, userToCheckId);
    }

    @GetMapping("/isYouBeenBlockedBy/{suspectedBlockerId}")
    public boolean isYouBeenBlockedBy(@PathVariable("userId") int userId,
                                      @PathVariable("suspectedBlockerId") int suspectedBlockerId) {
        return forumService.isYouBeenBlockedBy(userId, suspectedBlockerId);
    }

    @PostMapping("/saveTracker")
    public ResponseEntity<ModalTracker> saveTracker(@PathVariable("userId") int receiverId,
                                                    @RequestParam("associatedTypeId") int associateTypeId,
                                                    @RequestParam("type") String type) {

        ModalTracker modalTracker = forumService.saveTrackerOfUserById(receiverId, associateTypeId, type);
        return ResponseEntity.ok(modalTracker);
    }

    @GetMapping("/getTracker")
    public ModalTracker getTrackerByUserId(@PathVariable("userId") int userId) {
        return forumService.getTrackerOfUserById(userId);
    }

    @DeleteMapping("/deleteTracker")
    public ResponseEntity<ModalTracker> deleteTrackerByUserId(@PathVariable("userId") int userId,
                                                              @RequestParam("type") String type) {
        forumService.deleteTrackerOfUserById(userId, type);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getSuggestedMentions")
    public List<UserDTO> getSuggestedMentions(@PathVariable("userId") int userId,
                                              @RequestParam("name") String name) {
        return forumService.getSuggestedMentions(userId, name);
    }
}
