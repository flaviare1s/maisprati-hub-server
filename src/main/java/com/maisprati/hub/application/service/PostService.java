package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.Post;
import com.maisprati.hub.infrastructure.persistence.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post createPost(Post post) {
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public Optional<Post> updatePost(String postId, String title, String content) {
        Optional<Post> existingPost = postRepository.findById(postId);

        if (existingPost.isPresent()) {
            Post post = existingPost.get();
            post.setTitle(title);
            post.setContent(content);
            post.setUpdatedAt(LocalDateTime.now());
            return Optional.of(postRepository.save(post));
        }

        return Optional.empty();
    }

    public void deletePost(String postId) {
        postRepository.deleteById(postId);
    }

    public Optional<Post> getPostById(String postId) {
        return postRepository.findById(postId);
    }
}
