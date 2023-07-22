package com.forum.application.controller;

import com.forum.application.dto.PostDTO;
import com.forum.application.model.Post;
import com.forum.application.service.ForumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/forum/api/posts")
public class PostController {
    private final ForumService forumService;

    @GetMapping
    public List<PostDTO> getAllPost() {
        return forumService.getAllPost();
    }

    @GetMapping("/{postId}")
    public PostDTO getById(@PathVariable("postId") int postId) {
        return forumService.getPostById(postId);
    }

    @GetMapping("/author/{id}")
    public List<PostDTO> getAllByAuthorId(@PathVariable("id") int authorId) {
        return forumService.getAllByAuthorId(authorId);
    }

    @GetMapping("/commentSectionStatus/{postId}")
    public String getCommentSectionStatus(@PathVariable("postId") int postId) {
        return forumService.getCommentSectionStatus(postId);
    }

    @PostMapping
    public ResponseEntity<PostDTO> savePost(@RequestParam("body") String body,
                                            @RequestParam(required = false, name = "mentionedUserIds") Set<Integer> mentionedUserIds) {

        PostDTO postDTO = forumService.savePost(body, mentionedUserIds);
        return ResponseEntity.ok( postDTO );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<PostDTO> deletePost(@PathVariable("postId") int postId) {
        forumService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/commentSectionStatus/{postId}")
    public ResponseEntity<PostDTO> updateCommentSectionStatus(@PathVariable("postId") int postId,
                                                              @RequestParam("newStatus") Post.CommentSectionStatus status) {

        PostDTO postDTO = forumService.updateCommentSectionStatus(postId, status);
        return ResponseEntity.ok( postDTO );
    }

    @PatchMapping("/body/{postId}")
    public ResponseEntity<PostDTO> updatePostBody(@PathVariable("postId") int postId,
                                                  @RequestParam("newPostBody") String newPostBody) {

        PostDTO postDTO = forumService.updatePostBody(postId, newPostBody);
        return ResponseEntity.ok( postDTO );
    }

    @PatchMapping("/{postId}/like/{respondentId}")
    public ResponseEntity<PostDTO> likePost(@PathVariable("respondentId") int respondentId,
                                            @PathVariable("postId") int postId) {

        PostDTO postDTO = forumService.likePost(respondentId, postId);
        return ResponseEntity.ok( postDTO );
    }
}
