package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.application.service.PostService;
import com.maisprati.hub.domain.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostControllerTest {
	
	@Mock private PostService postService;
	@InjectMocks private PostController postController;
	
	private Post mockPost;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mockPost = new Post();
		mockPost.setId("1");
		mockPost.setTitle("Título de Teste");
		mockPost.setContent("Conteúdo do post de teste");
	}
	
	/**
	 * Testa o endpoint POST /api/posts
	 * - Deve criar um novo post.
	 */
	@Test
	void testCreatePost() {
		when(postService.createPost(any(Post.class))).thenReturn(mockPost);
		
		ResponseEntity<Post> response = postController.createPost(mockPost);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("Título de Teste", response.getBody().getTitle());
		verify(postService, times(1)).createPost(mockPost);
	}
	
	/**
	 * Testa o endpoint GET /api/posts
	 * - Deve retornar a lista de posts.
	 */
	@Test
	void testGetAllPosts() {
		when(postService.getAllPosts()).thenReturn(List.of(mockPost));
		
		ResponseEntity<List<Post>> response = postController.getAllPosts();
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
		assertEquals("Título de Teste", response.getBody().get(0).getTitle());
		verify(postService, times(1)).getAllPosts();
	}
	
	/**
	 * Testa o endpoint GET /api/posts/{id}
	 * - Deve retornar um post existente.
	 */
	@Test
	void testGetPostById_Found() {
		when(postService.getPostById("1")).thenReturn(Optional.of(mockPost));
		
		ResponseEntity<Post> response = postController.getPostById("1");
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("1", response.getBody().getId());
		verify(postService, times(1)).getPostById("1");
	}
	
	/**
	 * Testa o endpoint GET /api/posts/{id}
	 * - Deve retornar 404 se o post não existir.
	 */
	@Test
	void testGetPostById_NotFound() {
		when(postService.getPostById("999")).thenReturn(Optional.empty());
		
		ResponseEntity<Post> response = postController.getPostById("999");
		
		assertEquals(404, response.getStatusCodeValue());
		assertNull(response.getBody());
		verify(postService, times(1)).getPostById("999");
	}
	
	/**
	 * Testa o endpoint PUT /api/posts/{id}
	 * - Deve atualizar um post existente.
	 */
	@Test
	void testUpdatePost_Found() {
		Post updated = new Post();
		updated.setId("1");
		updated.setTitle("Novo Título");
		updated.setContent("Novo conteúdo");
		
		when(postService.updatePost(eq("1"), anyString(), anyString()))
			.thenReturn(Optional.of(updated));
		
		ResponseEntity<Post> response = postController.updatePost("1", updated);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("Novo Título", response.getBody().getTitle());
		verify(postService, times(1)).updatePost("1", "Novo Título", "Novo conteúdo");
	}
	
	/**
	 * Testa o endpoint PUT /api/posts/{id}
	 * - Deve retornar 404 se o post não for encontrado.
	 */
	@Test
	void testUpdatePost_NotFound() {
		when(postService.updatePost(eq("999"), anyString(), anyString()))
			.thenReturn(Optional.empty());
		
		Post updateData = new Post();
		updateData.setTitle("Título Inexistente");
		updateData.setContent("Conteúdo...");
		
		ResponseEntity<Post> response = postController.updatePost("999", updateData);
		
		assertEquals(404, response.getStatusCodeValue());
		verify(postService, times(1))
			.updatePost(eq("999"), eq("Título Inexistente"), eq("Conteúdo..."));
	}
	
	/**
	 * Testa o endpoint DELETE /api/posts/{id}
	 * - Deve excluir o post.
	 */
	@Test
	void testDeletePost() {
		doNothing().when(postService).deletePost("1");
		
		ResponseEntity<Void> response = postController.deletePost("1");
		
		assertEquals(204, response.getStatusCodeValue());
		verify(postService, times(1)).deletePost("1");
	}
}
