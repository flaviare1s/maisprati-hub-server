package com.maisprati.hub.infrastructure.persistence.repository;

import com.maisprati.hub.domain.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findAllByAuthorId(String authorId);
}
