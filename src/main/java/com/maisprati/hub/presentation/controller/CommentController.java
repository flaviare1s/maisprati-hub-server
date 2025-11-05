package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.application.dto.CommentDTO;
import com.maisprati.hub.domain.model.Comment;
import com.maisprati.hub.application.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentDTO>> getCommentsByPost(@PathVariable String postId) {
        List<CommentDTO> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable String postId,
            @RequestBody Comment comment
    ) {
        CommentDTO created = commentService.createComment(comment, comment.getAuthorId());
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable String commentId, @RequestBody Comment comment) {
        CommentDTO updated = commentService.updateComment(commentId, comment.getContent());
        return ResponseEntity.ok(updated);
    }
}

