package com.maisprati.hub.application.service;

import com.maisprati.hub.application.dto.CommentDTO;
import com.maisprati.hub.domain.model.Comment;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.CommentRepository;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
	
	@Mock private CommentRepository commentRepository;
	@Mock private UserRepository userRepository;
	@InjectMocks private CommentService commentService;
	
	private final String userId = "user123";
	private final String postId = "post456";
	
	@Test
	void shouldReturnCommentsForPost() {
		// Arrange: Configurar o cenário
		Comment comment = new Comment();
		comment.setId("c1");
		comment.setPostId(postId);
		comment.setContent("Hello");
		
		when(commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId))
			.thenReturn(List.of(comment));
		
		// Act: Executar o método testado
		List<CommentDTO> result = commentService.getCommentsByPostId(postId);
		
		// Assert: Verificar se o resultado está correto
		assertEquals(1, result.size());
		assertEquals("Hello", result.get(0).getContent());
		verify(commentRepository).findAllByPostIdOrderByCreatedAtAsc(postId);
	}
	
	@Test
	void shouldCreateCommentSuccessfully() {
		// Arrange
		Comment comment = new Comment();
		comment.setPostId(postId);
		comment.setContent("New comment");
		
		User user = new User();
		user.setId(userId);
		user.setCodename("Tester");
		user.setAvatar("avatar.png");
		
		// Mock do repositório
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(commentRepository.save(any(Comment.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		
		// Act
		CommentDTO result = commentService.createComment(comment, userId);
		
		// Assert
		assertNotNull(result); // Retorno não pode ser nulo
		assertEquals("New comment", result.getContent());
		assertNotNull(result.getCreatedAt()); // Deve ter data de criação
		assertEquals("Tester", result.getAuthor().getCodename());
		verify(commentRepository).save(any(Comment.class));
	}
	
	@Test
	void shouldDeleteComment() {
		// Arrange
		Comment comment = new Comment();
		comment.setId("c1");
		when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
		
		// Act
		commentService.deleteComment("c1");
		
		// Assert
		verify(commentRepository).delete(comment); // Verifica se o delete foi chamado
	}
	
	@Test
	void shouldUpdateComment() {
		// Arrange
		Comment comment = new Comment();
		comment.setId("c1");
		comment.setAuthorId(userId);
		comment.setContent("Old content");
		
		User user = new User();
		user.setId(userId);
		user.setCodename("Tester");
		
		when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
		when(commentRepository.save(any(Comment.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		
		// Act
		CommentDTO result = commentService.updateComment("c1", "Updated content");
		
		// Assert
		assertEquals("Updated content", result.getContent());
		assertEquals("Tester", result.getAuthor().getCodename());
	}
}
