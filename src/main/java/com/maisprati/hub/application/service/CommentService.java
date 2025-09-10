package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.Comment;
import com.maisprati.hub.infrastructure.persistence.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public List<Comment> getCommentsByPostId(String postId) {
        return commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId);
    }

    public Comment createComment(Comment comment) {
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public Optional<Comment> updateComment(String commentId, String content) {
        Optional<Comment> existingComment = commentRepository.findById(commentId);

        if (existingComment.isPresent()) {
            Comment comment = existingComment.get();
            comment.setContent(content);
            return Optional.of(commentRepository.save(comment));
        }

        return Optional.empty();
    }

    public void deleteComment(String commentId) {
        commentRepository.deleteById(commentId);
    }

    public List<Comment> getCommentsByAuthor(String authorId) {
        return commentRepository.findAllByAuthorId(authorId);
    }
}
