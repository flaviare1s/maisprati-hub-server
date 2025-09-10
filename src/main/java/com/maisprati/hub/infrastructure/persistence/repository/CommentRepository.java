package com.maisprati.hub.infrastructure.persistence.repository;

import com.maisprati.hub.domain.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findAllByPostIdOrderByCreatedAtAsc(String postId);
    List<Comment> findAllByAuthorId(String authorId);
}
