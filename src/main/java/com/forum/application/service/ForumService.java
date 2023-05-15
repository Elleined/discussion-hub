package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.ReplyDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForumService {

    private final PostService postService;
    private final CommentService commentService;
    private final ReplyService replyService;

    public ForumService(PostService postService, CommentService commentService, ReplyService replyService) {
        this.postService = postService;
        this.commentService = commentService;
        this.replyService = replyService;
    }

    public void savePost(int authorId, String body) {
        postService.save(authorId, body);
    }

    public void saveComment(int commenterId, int postId, String body) {
        commentService.save(commenterId, postId, body);
    }

    public void saveReply(int replierId, int commentId, String body) {
        replyService.save(replierId, commentId, body);
    }

    public void deletePost(int postId) {
        postService.delete(postId);
    }

    public void deleteComment(int commentId) {
        commentService.delete(commentId);
    }

    public void deleteReply(int replyId) {
        replyService.delete(replyId);
    }

    public List<PostDTO> getAllPost() {
        return postService.getAll();
    }

    public List<CommentDTO> getAllCommentsOf(int postId) {
        return commentService.getAllCommentsOf(postId);
    }

    public List<ReplyDTO> getAllRepliesOf(int commentId) {
        return replyService.getAllRepliesOf(commentId);
    }
}
