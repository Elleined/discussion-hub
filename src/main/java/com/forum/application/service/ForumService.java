package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.ReplyDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ForumService {

    private final PostService postService;
    private final CommentService commentService;
    private final ReplyService replyService;

    public int savePost(int authorId, String body) {
        return postService.save(authorId, body);
    }

    public int saveComment(int commenterId, int postId, String body) {
        return commentService.save(commenterId, postId, body);
    }

    public int saveReply(int replierId, int commentId, String body) {
        return replyService.save(replierId, commentId, body);
    }

    public CommentDTO getCommentById(int commentId) {
        return commentService.getById(commentId);
    }
    public ReplyDTO getReplyById(int replyId) {
        return replyService.getById(replyId);
    }

    public boolean isEmpty(String body) {
        return body == null || body.isEmpty() || body.isBlank();
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
