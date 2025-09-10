package com.maisprati.hub.application.service;

import com.maisprati.hub.application.dto.CommentDTO;
import com.maisprati.hub.application.dto.UserDTO;
import com.maisprati.hub.domain.model.Comment;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.CommentRepository;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public List<CommentDTO> getCommentsByPostId(String postId) {
        List<Comment> comments = commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId);

        return comments.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CommentDTO createComment(Comment comment, String authorId) {
        comment.setCreatedAt(LocalDateTime.now());

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        comment.setAuthorId(user.getId());
        Comment saved = commentRepository.save(comment);

        return toDTO(saved, user);
    }

    public void deleteComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));
        commentRepository.delete(comment);
    }

    public CommentDTO updateComment(String commentId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));

        comment.setContent(newContent);
        Comment updated = commentRepository.save(comment);

        User user = userRepository.findById(comment.getAuthorId()).orElse(null);
        return toDTO(updated, user);
    }

    private CommentDTO toDTO(Comment comment) {
        User user = userRepository.findById(comment.getAuthorId()).orElse(null);
        return toDTO(comment, user);
    }

    private CommentDTO toDTO(Comment comment, User user) {
        UserDTO author = null;
        if (user != null) {
            author = UserDTO.builder()
                    .id(user.getId())
                    .codename(user.getCodename())
                    .avatar(user.getAvatar())
                    .build();
        }

        return CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .author(author)
                .build();
    }
}
