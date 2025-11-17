package com.maisprati.hub.infrastructure.security.auth;

import com.maisprati.hub.application.service.PasswordResetService;
import com.maisprati.hub.application.service.UserService;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.security.jwt.JwtProperties;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import com.maisprati.hub.presentation.dto.ForgotPasswordRequest;
import com.maisprati.hub.presentation.dto.LoginRequest;
import com.maisprati.hub.presentation.dto.RegisterStudentRequest;
import com.maisprati.hub.presentation.dto.ResetPasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpHeaders;
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
	@Mock private JwtProperties jwtProperties;
	@InjectMocks private AuthController authController;
	
	private User user;
	
	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		
		user = new User();
		user.setEmail("email@test.com");
		user.setPassword("encrypted");
	}
	
	// TEST 1 - /register
	@Test
	void registerStudent_ShouldReturnCreatedWhenSuccess() {
		
		RegisterStudentRequest req = new RegisterStudentRequest();
		req.setName("Test User");
		req.setEmail("email@test.com");
		req.setPassword("123");
		
		when(userService.registerStudent(any(User.class))).thenReturn(user);
		
		ResponseEntity<?> response = authController.registerStudent(req);
		
		assertEquals(201, response.getStatusCodeValue());
		assertEquals("Cadastro realizado com sucesso!", ((Map<?, ?>) response.getBody()).get("message"));
	}
	
	// TEST 2
	@Test
	void registerStudent_ShouldReturnBadRequestOnError() {
		
		RegisterStudentRequest req = new RegisterStudentRequest();
		req.setName("Test User");
		req.setEmail("email@test.com");
		req.setPassword("123");
		
		when(userService.registerStudent(any(User.class)))
			.thenThrow(new RuntimeException("Erro"));
		
		ResponseEntity<?> response = authController.registerStudent(req);
		
		assertEquals(400, response.getStatusCodeValue());
		assertEquals("Erro", ((Map<?, ?>) response.getBody()).get("error"));
	}
	
	// TEST 3 - /login
	@Test
	void login_ShouldReturnTokens() {
		
		var tokens = new AuthTokens("access-token", "refresh-token");
		
		LoginRequest req = new LoginRequest();
		req.setEmail("email@test.com");
		req.setPassword("123");
		
		when(authService.login(req.getEmail(), req.getPassword())).thenReturn(tokens);
		when(jwtProperties.isSecureCookie()).thenReturn(false);
		when(jwtProperties.getAccessTokenExpiration()).thenReturn(3600L);
		when(jwtProperties.getRefreshTokenExpiration()).thenReturn(7200L);
		
		ResponseEntity<?> response = authController.login(req);
		
		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
		assertEquals("Login realizado com sucesso!", ((Map<?, ?>) response.getBody()).get("message"));
	}
	
	// TEST 4
	@Test
	void login_ShouldReturnUnauthorizedOnInvalidPassword() {
		
		LoginRequest req = new LoginRequest();
		req.setEmail("email@test.com");
		req.setPassword("wrong");
		
		when(authService.login(req.getEmail(), req.getPassword()))
			.thenThrow(new RuntimeException("Credenciais inválidas"));
		
		ResponseEntity<?> response = authController.login(req);
		
		assertEquals(401, response.getStatusCodeValue());
		assertEquals("Credenciais inválidas", ((Map<?, ?>) response.getBody()).get("error"));
	}
	
	// TEST 5 - /me
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
	
	// TEST 6
	@Test
	void getCurrentUser_ShouldReturnUnauthorizedWhenAnonymous() {
		SecurityContext context = mock(SecurityContext.class);
		when(context.getAuthentication()).thenReturn(null);
		SecurityContextHolder.setContext(context);
		
		ResponseEntity<?> response = authController.getCurrentUser();
		
		assertEquals(401, response.getStatusCodeValue());
		assertEquals("Usuário não autenticado", ((Map<?, ?>) response.getBody()).get("error"));
	}
	
	// TEST 7 - /refresh
	@Test
	void refreshToken_ShouldReturnNewAccessToken() {
		String refreshToken = "refresh-token";
		
		when(jwtService.extractUsernameFromRefreshToken(refreshToken)).thenReturn("email@test.com");
		when(userService.getUserByEmail("email@test.com")).thenReturn(Optional.of(user));
		when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
		when(jwtProperties.isSecureCookie()).thenReturn(false);
		when(jwtProperties.getAccessTokenExpiration()).thenReturn(3600L);
		
		ResponseEntity<?> response = authController.refreshToken(refreshToken);
		
		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
		assertEquals("Access token renovado com sucesso!", ((Map<?, ?>) response.getBody()).get("message"));
	}
	
	// TEST 8
	@Test
	void refreshToken_ShouldReturnUnauthorizedWhenTokenMissing() {
		ResponseEntity<?> response = authController.refreshToken(null);
		assertEquals(401, response.getStatusCodeValue());
		assertEquals("Refresh token ausente", ((Map<?, ?>) response.getBody()).get("error"));
	}
	
	// TEST 9
	@Test
	void refreshToken_ShouldReturnUnauthorizedWhenTokenInvalid() {
		String refreshToken = "invalid-token";
		
		when(jwtService.extractUsernameFromRefreshToken(refreshToken)).thenReturn(null);
		
		ResponseEntity<?> response = authController.refreshToken(refreshToken);
		
		assertEquals(401, response.getStatusCodeValue());
		assertEquals("Token inválido ou expirado", ((Map<?, ?>) response.getBody()).get("error"));
	}
	
	// TEST 10 - /forgot-password
	@Test
	void forgotPassword_ShouldReturnOkWhenSuccess() {
		
		ForgotPasswordRequest req = new ForgotPasswordRequest();
		req.setEmail("email@test.com");
		
		doNothing().when(passwordResetService).generateAndSendToken("email@test.com");
		
		ResponseEntity<?> response = authController.forgotPassword(req);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("Se o e-mail existir, enviaremos um link de redefinição",
			((Map<?, ?>) response.getBody()).get("message"));
	}
	
	// TEST 11
	@Test
	void forgotPassword_ShouldReturnNotFoundWhenError() {
		
		ForgotPasswordRequest req = new ForgotPasswordRequest();
		req.setEmail("email@test.com");
		
		doThrow(new RuntimeException("Erro"))
			.when(passwordResetService).generateAndSendToken("email@test.com");
		
		ResponseEntity<?> response = authController.forgotPassword(req);
		
		assertEquals(404, response.getStatusCodeValue());
		assertEquals("Erro", ((Map<?, ?>) response.getBody()).get("error"));
	}
	
	// TEST 12 - /reset-password
	@Test
	void resetPassword_ShouldReturnOkWhenSuccess() {
		
		ResetPasswordRequest req = new ResetPasswordRequest();
		req.setToken("token");
		req.setNewPassword("newpass");
		
		doNothing().when(passwordResetService).resetPassword("token", "newpass");
		
		ResponseEntity<?> response = authController.resetPassword(req);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("Senha atualizada com sucesso!", ((Map<?, ?>) response.getBody()).get("message"));
	}
	
	// TEST 13
	@Test
	void resetPassword_ShouldReturnBadRequestOnError() {
		
		ResetPasswordRequest req = new ResetPasswordRequest();
		req.setToken("token");
		req.setNewPassword("newpass");
		
		doThrow(new RuntimeException("Erro"))
			.when(passwordResetService).resetPassword("token", "newpass");
		
		ResponseEntity<?> response = authController.resetPassword(req);
		
		assertEquals(400, response.getStatusCodeValue());
		assertEquals("Erro", ((Map<?, ?>) response.getBody()).get("error"));
	}
	
	// TEST 14 - /logout
	@Test
	void logout_ShouldReturnOkAndExpireCookies() {
		// Configura o mock do jwtProperties
		when(jwtProperties.isSecureCookie()).thenReturn(false);
		
		ResponseEntity<?> response = authController.logout();
		
		// Verifica o status
		assertEquals(200, response.getStatusCodeValue());
		
		// Verifica o corpo
		assertEquals("Logout realizado com sucesso!", ((Map<?, ?>) response.getBody()).get("message"));
		
		// Recupera todos os cookies
		var cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
		assertNotNull(cookies);
		assertEquals(2, cookies.size());
		
		// Verifica se os cookies expirados estão corretos
		assertTrue(cookies.get(0).contains("access_token="));
		assertTrue(cookies.get(0).contains("Max-Age=0"));
		
		assertTrue(cookies.get(1).contains("refresh_token="));
		assertTrue(cookies.get(1).contains("Max-Age=0"));
	}
}
