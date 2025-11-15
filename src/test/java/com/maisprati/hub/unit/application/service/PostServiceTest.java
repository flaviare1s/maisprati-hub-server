package com.maisprati.hub.unit.application.service;

import com.maisprati.hub.application.service.PostService;
import com.maisprati.hub.domain.model.Post;
import com.maisprati.hub.infrastructure.persistence.repository.CommentRepository;
import com.maisprati.hub.infrastructure.persistence.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostServiceTest {
	
	@Mock private PostRepository postRepository;
	@Mock private CommentRepository commentRepository;
	@InjectMocks private PostService postService;
	
	private Post mockPost;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		
		mockPost = new Post();
		mockPost.setId("1");
		mockPost.setTitle("Título Original");
		mockPost.setContent("Conteúdo Original");
		mockPost.setCreatedAt(LocalDateTime.now());
		mockPost.setUpdatedAt(LocalDateTime.now());
	}
	
	/**
	 * Testa getAllPosts() — deve retornar a lista de posts.
	 */
	@Test
	void testGetAllPosts() {
		when(postRepository.findAll()).thenReturn(List.of(mockPost));
		
		List<Post> result = postService.getAllPosts();
		
		assertEquals(1, result.size());
		assertEquals("Título Original", result.get(0).getTitle());
		verify(postRepository, times(1)).findAll();
	}
	
	/**
	 * Testa createPost() — deve salvar e retornar o post criado.
	 */
	@Test
	void testCreatePost() {
		when(postRepository.save(any(Post.class))).thenReturn(mockPost);
		
		Post created = postService.createPost(mockPost);
		
		assertNotNull(created.getCreatedAt());
		assertNotNull(created.getUpdatedAt());
		assertEquals("Título Original", created.getTitle());
		verify(postRepository, times(1)).save(mockPost);
	}
	
	/**
	 * Testa updatePost() — deve atualizar um post existente.
	 */
	@Test
	void testUpdatePost_Found() {
		when(postRepository.findById("1")).thenReturn(Optional.of(mockPost));
		when(postRepository.save(any(Post.class))).thenReturn(mockPost);
		
		Optional<Post> result = postService.updatePost("1", "Novo Título", "Novo Conteúdo");
		
		assertTrue(result.isPresent());
		assertEquals("Novo Título", result.get().getTitle());
		assertEquals("Novo Conteúdo", result.get().getContent());
		verify(postRepository, times(1)).findById("1");
		verify(postRepository, times(1)).save(mockPost);
	}
	
	/**
	 * Testa updatePost() — deve retornar Optional.empty() se o post não existir.
	 */
	@Test
	void testUpdatePost_NotFound() {
		when(postRepository.findById("999")).thenReturn(Optional.empty());
		
		Optional<Post> result = postService.updatePost("999", "Sem Título", "Sem Conteúdo");
		
		assertTrue(result.isEmpty());
		verify(postRepository, times(1)).findById("999");
		verify(postRepository, never()).save(any(Post.class));
	}
	
	/**
	 * Testa deletePost() — deve excluir os comentários e o post.
	 */
	@Test
	void testDeletePost() {
		doNothing().when(commentRepository).deleteAllByPostId("1");
		doNothing().when(postRepository).deleteById("1");
		
		postService.deletePost("1");
		
		verify(commentRepository, times(1)).deleteAllByPostId("1");
		verify(postRepository, times(1)).deleteById("1");
	}
	
	/**
	 * Testa getPostById() — deve retornar o post correto.
	 */
	@Test
	void testGetPostById() {
		when(postRepository.findById("1")).thenReturn(Optional.of(mockPost));
		
		Optional<Post> result = postService.getPostById("1");
		
		assertTrue(result.isPresent());
		assertEquals("1", result.get().getId());
		verify(postRepository, times(1)).findById("1");
	}
}
