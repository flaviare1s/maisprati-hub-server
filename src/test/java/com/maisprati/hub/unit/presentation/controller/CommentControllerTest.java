package com.maisprati.hub.unit.presentation.controller;

import com.maisprati.hub.application.dto.CommentDTO;
import com.maisprati.hub.application.dto.UserDTO;
import com.maisprati.hub.application.service.CommentService;
import com.maisprati.hub.domain.model.Comment;
import com.maisprati.hub.presentation.controller.CommentController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentControllerTest {
	
	@Mock private CommentService commentService;
	@InjectMocks private CommentController commentController;
	
	private CommentDTO mockCommentDTO;
	private Comment mockComment;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		
		UserDTO author = new UserDTO("1", "vitoria", "avatar.png");
		
		mockCommentDTO = CommentDTO.builder()
			                 .id("c1")
			                 .postId("p1")
			                 .content("Comentário de teste")
			                 .createdAt(LocalDateTime.now())
			                 .author(author)
			                 .build();
		
		mockComment = new Comment();
		mockComment.setId("c1");
		mockComment.setPostId("p1");
		mockComment.setAuthorId("1");
		mockComment.setContent("Comentário de teste");
	}
	
	/**
	 * Testa GET /api/posts/{postId}/comments
	 */
	@Test
	void testGetCommentsByPost() {
		when(commentService.getCommentsByPostId("p1")).thenReturn(List.of(mockCommentDTO));
		
		ResponseEntity<List<CommentDTO>> response = commentController.getCommentsByPost("p1");
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
		assertEquals("Comentário de teste", response.getBody().get(0).getContent());
		verify(commentService, times(1)).getCommentsByPostId("p1");
	}
	
	/**
	 * Testa POST /api/posts/{postId}/comments
	 */
	@Test
	void testCreateComment() {
		when(commentService.createComment(any(Comment.class), eq("1"))).thenReturn(mockCommentDTO);
		
		ResponseEntity<CommentDTO> response = commentController.createComment("p1", mockComment);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("Comentário de teste", response.getBody().getContent());
		verify(commentService, times(1)).createComment(mockComment, "1");
	}
	
	/**
	 * Testa DELETE /api/comments/{commentId}
	 */
	@Test
	void testDeleteComment() {
		doNothing().when(commentService).deleteComment("c1");
		
		ResponseEntity<Void> response = commentController.deleteComment("c1");
		
		assertEquals(200, response.getStatusCodeValue());
		verify(commentService, times(1)).deleteComment("c1");
	}
	
	/**
	 * Testa PUT /api/comments/{commentId}
	 */
	@Test
	void testUpdateComment() {
		when(commentService.updateComment(eq("c1"), anyString())).thenReturn(mockCommentDTO);
		
		ResponseEntity<CommentDTO> response = commentController.updateComment("c1", mockComment);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("Comentário de teste", response.getBody().getContent());
		verify(commentService, times(1)).updateComment(eq("c1"), anyString());
	}
}
