package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.PasswordResetToken;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.email.SmtpEmailService;
import com.maisprati.hub.infrastructure.persistence.repository.PasswordResetTokenRepository;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {
	
	@Mock private UserRepository userRepository;
	@Mock private PasswordResetTokenRepository resetTokenRepository;
	@Mock private SmtpEmailService smtpEmailService;
	@Mock private PasswordEncoder passwordEncoder;
	@InjectMocks private PasswordResetService passwordResetService;
	
	private final String email = "user@test.com";
	private final String userId = "user123";
	
	@Test
	void shouldGenerateAndSendToken() {
		// Arrange: preparar mocks
		User user = new User();
		user.setId(userId);
		user.setEmail(email);
		
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(resetTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		
		// Act: chamar o método que gera o token
		passwordResetService.generateAndSendToken(email);
		
		// Assert: verificar interações com os mocks
		verify(resetTokenRepository).save(any()); // Token salvo no repo
		verify(smtpEmailService).sendPasswordResetEmail(eq(email), anyString(), anyString()); // Email enviado
	}
	
	@Test
	void shouldResetPasswordSuccessfully() {
		// Arrange
		String rawToken = "token123";
		String hashedToken = org.springframework.util.DigestUtils.md5DigestAsHex(rawToken.getBytes());
		String newPassword = "newpass";
		
		PasswordResetToken token = new PasswordResetToken();
		token.setUserId(userId);
		token.setTokenHash(hashedToken);
		token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
		token.setUsed(false);
		
		User user = new User();
		user.setId(userId);
		
		when(resetTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(token));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(resetTokenRepository.save(any(PasswordResetToken.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		
		// Act
		passwordResetService.resetPassword(rawToken, newPassword);
		
		// Assert
		assertTrue(token.isUsed()); // Token deve ser marcado como usado
		assertEquals("encodedPassword", user.getPassword()); // Senha atualizada
		verify(userRepository).save(user);
		verify(resetTokenRepository).save(token);
	}
	
	@Test
	void shouldThrowWhenTokenInvalid() {
		// Arrange
		when(resetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());
		
		// Act & Assert: deve lançar exceção
		assertThrows(RuntimeException.class, () ->
			                                     passwordResetService.resetPassword("fakeToken", "newpass"));
	}
}
