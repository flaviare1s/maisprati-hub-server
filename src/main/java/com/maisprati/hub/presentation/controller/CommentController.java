package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.domain.model.Comment;
import com.maisprati.hub.application.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable String postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<Comment> createComment(
            @PathVariable String postId,
            @RequestBody Comment comment
    ) {
        comment.setPostId(postId);
        Comment created = commentService.createComment(comment);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/api/comments/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable String id,
            @RequestBody Comment comment
    ) {
        Optional<Comment> updated = commentService.updateComment(id, comment.getContent());
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable String id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
