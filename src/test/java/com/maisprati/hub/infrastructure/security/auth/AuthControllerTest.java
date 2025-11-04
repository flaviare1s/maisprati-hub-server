package com.maisprati.hub.infrastructure.security.auth;

import com.maisprati.hub.application.service.PasswordResetService;
import com.maisprati.hub.application.service.UserService;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import com.maisprati.hub.presentation.dto.ForgotPasswordRequest;
import com.maisprati.hub.presentation.dto.ResetPasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

	@Mock private UserService userService;
	@Mock private AuthService authService;
	@Mock private PasswordResetService passwordResetService;
	@Mock private JwtService jwtService;
	@InjectMocks private AuthController authController;

	private User user;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		user = new User();
		user.setEmail("email@test.com");
		user.setPassword("encrypted");
	}

	// ==================== /register ====================
	@Test
	void registerStudent_ShouldReturnCreatedWhenSuccess() {
		// O service registerStudent não retorna User no controller, apenas lança exceção em caso de erro.
		// Ajuste: Apenas garantindo que não lança exceção.

		ResponseEntity<?> response = authController.registerStudent(user);

		assertEquals(201, response.getStatusCodeValue());
		assertEquals("Cadastro realizado com sucesso!", ((Map<?, ?>)response.getBody()).get("message"));
	}

	@Test
	void registerStudent_ShouldReturnBadRequestOnError() {
		doThrow(new RuntimeException("Erro")).when(userService).registerStudent(user);

		ResponseEntity<?> response = authController.registerStudent(user);

		assertEquals(400, response.getStatusCodeValue());
		assertEquals("Erro", ((Map<?, ?>)response.getBody()).get("error"));
	}

	// ==================== /login - BEARER TOKEN ====================
	@Test
	void login_ShouldReturnTokensInBody() {
		var tokens = new com.maisprati.hub.infrastructure.security.auth.AuthTokens("access-token-123", "refresh-token-456");
		when(authService.login(user.getEmail(), "123")).thenReturn(tokens);

		user.setPassword("123");

		ResponseEntity<?> response = authController.login(user);

		assertEquals(200, response.getStatusCodeValue());

		@SuppressWarnings("unchecked")
		Map<String, String> body = (Map<String, String>) response.getBody();

		assertEquals("Login realizado com sucesso!", body.get("message"));
		assertEquals("access-token-123", body.get("accessToken"));
		assertEquals("refresh-token-456", body.get("refreshToken"));

	}

	@Test
	void login_ShouldReturnUnauthorizedOnInvalidPassword() {
		when(authService.login(user.getEmail(), "wrong")).thenThrow(new RuntimeException("Credenciais inválidas"));

		user.setPassword("wrong");
		ResponseEntity<?> response = authController.login(user);

		assertEquals(401, response.getStatusCodeValue());
		assertEquals("Credenciais inválidas", ((Map<?, ?>)response.getBody()).get("error"));
	}

	// ==================== /me ====================
	@Test
	void getCurrentUser_ShouldReturnUserWhenAuthenticated() {
		Authentication auth = mock(Authentication.class);
		SecurityContext context = mock(SecurityContext.class);

		when(auth.getName()).thenReturn("email@test.com");
		when(auth.getPrincipal()).thenReturn(user);
		when(context.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(context);

		when(userService.getUserByEmail("email@test.com")).thenReturn(Optional.of(user));

		ResponseEntity<?> response = authController.getCurrentUser();

		assertEquals(200, response.getStatusCodeValue());
		assertEquals(user, response.getBody());
	}

	@Test
	void getCurrentUser_ShouldReturnUnauthorizedWhenAnonymous() {
		SecurityContext context = mock(SecurityContext.class);
		when(context.getAuthentication()).thenReturn(null);
		SecurityContextHolder.setContext(context);

		ResponseEntity<?> response = authController.getCurrentUser();

		assertEquals(401, response.getStatusCodeValue());
		assertEquals("Usuário não autenticado", ((Map<?, ?>)response.getBody()).get("error"));
	}

	// ==================== /refresh - BEARER TOKEN ====================
	@Test
	void refreshToken_ShouldReturnNewAccessToken() {
		String refreshToken = "refresh-token-valid";

		// O método espera um Map no body da requisição
		Map<String, String> requestBody = Map.of("refreshToken", refreshToken);

		when(jwtService.extractUsernameFromRefreshToken(refreshToken)).thenReturn("email@test.com");
		when(userService.getUserByEmail("email@test.com")).thenReturn(Optional.of(user));
		when(jwtService.generateAccessToken(user)).thenReturn("new-access-token-fresh");

		ResponseEntity<?> response = authController.refreshToken(requestBody); // Passa o Map

		assertEquals(200, response.getStatusCodeValue());

		@SuppressWarnings("unchecked")
		Map<String, String> body = (Map<String, String>) response.getBody();

		assertEquals("Access token renovado com sucesso!", body.get("message"));
		assertEquals("new-access-token-fresh", body.get("accessToken"));

	}

	@Test
	void refreshToken_ShouldReturnUnauthorizedWhenTokenMissing() {
		Map<String, String> requestBody = Map.of("refreshToken", "");
		ResponseEntity<?> response = authController.refreshToken(requestBody);

		assertEquals(401, response.getStatusCodeValue());
		assertEquals("Refresh token ausente", ((Map<?, ?>)response.getBody()).get("error"));
	}

	@Test
	void refreshToken_ShouldReturnUnauthorizedWhenTokenInvalid() {
		String refreshToken = "invalid-token";
		Map<String, String> requestBody = Map.of("refreshToken", refreshToken);
		when(jwtService.extractUsernameFromRefreshToken(refreshToken)).thenReturn(null);

		ResponseEntity<?> response = authController.refreshToken(requestBody);

		assertEquals(401, response.getStatusCodeValue());
		assertEquals("Token inválido ou expirado", ((Map<?, ?>)response.getBody()).get("error"));
	}

	// ==================== /forgot-password ====================
	@Test
	void forgotPassword_ShouldReturnOkWhenSuccess() {
		doNothing().when(passwordResetService).generateAndSendToken("email@test.com");
		ForgotPasswordRequest request = new ForgotPasswordRequest();
		request.setEmail("email@test.com");

		ResponseEntity<?> response = authController.forgotPassword(request);

		assertEquals(200, response.getStatusCodeValue());
		assertEquals("Se o e-mail existir, enviaremos um link de redefinição", ((Map<?, ?>)response.getBody()).get("message"));
	}

	@Test
	void forgotPassword_ShouldReturnNotFoundWhenError() {
		doThrow(new RuntimeException("Erro")).when(passwordResetService).generateAndSendToken("email@test.com");
		ForgotPasswordRequest request = new ForgotPasswordRequest();
		request.setEmail("email@test.com");

		ResponseEntity<?> response = authController.forgotPassword(request);

		assertEquals(404, response.getStatusCodeValue());
		assertEquals("Erro", ((Map<?, ?>)response.getBody()).get("error"));
	}

	// ==================== /reset-password ====================
	@Test
	void resetPassword_ShouldReturnOkWhenSuccess() {
		doNothing().when(passwordResetService).resetPassword("token", "newpass");
		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setToken("token");
		request.setNewPassword("newpass");

		ResponseEntity<?> response = authController.resetPassword(request);

		assertEquals(200, response.getStatusCodeValue());
		assertEquals("Senha atualizada com sucesso!", ((Map<?, ?>)response.getBody()).get("message"));
	}

	@Test
	void resetPassword_ShouldReturnBadRequestOnError() {
		doThrow(new RuntimeException("Erro")).when(passwordResetService).resetPassword("token", "newpass");
		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setToken("token");
		request.setNewPassword("newpass");

		ResponseEntity<?> response = authController.resetPassword(request);

		assertEquals(400, response.getStatusCodeValue());
		assertEquals("Erro", ((Map<?, ?>)response.getBody()).get("error"));
	}

	@Test
	void logout_ShouldReturnOkWithMessage() {
		ResponseEntity<?> response = authController.logout();

		assertEquals(200, response.getStatusCodeValue());

		assertEquals("Logout realizado com sucesso!", ((Map<?, ?>) response.getBody()).get("message"));
	}
}
