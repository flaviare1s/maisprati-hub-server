package com.maisprati.hub.infrastructure.security.auth;

import com.maisprati.hub.application.service.PasswordResetService;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.application.service.UserService;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import com.maisprati.hub.presentation.dto.ForgotPasswordRequest;
import com.maisprati.hub.presentation.dto.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final UserService userService;
	private final AuthService authService;
	private final PasswordResetService passwordResetService;
	private final JwtService jwtService;

	/**
	 * POST api/auth/register - Cadastra um aluno
	 */
	@PostMapping("/register")
	public ResponseEntity<?> registerStudent(@RequestBody User user) {
		try {
			userService.registerStudent(user);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("message", "Cadastro realizado com sucesso!"));
		} catch (RuntimeException e) {
			log.error("Erro ao cadastrar aluno: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * POST api/auth/login - Realiza login e retorna tokens no body
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody User user) {
		try {
			var tokens = authService.login(user.getEmail(), user.getPassword());

			// Retorna os tokens no body da resposta
			return ResponseEntity.ok(Map.of(
					"message", "Login realizado com sucesso!",
					"accessToken", tokens.accessToken(),
					"refreshToken", tokens.refreshToken()
			));
		} catch (RuntimeException e) {
			log.error("Erro ao fazer login: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * POST api/auth/logout - Faz logout do usuário
	 */
	@PostMapping("/logout")
	public ResponseEntity<?> logout() {
		// Com Bearer Token, o logout é feito no frontend removendo o token
		return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso!"));
	}

	/**
	 * GET api/auth/me - Busca dados do usuário autenticado
	 */
	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			// verifica se é null ou se é anônimo
			if (authentication == null ||
					authentication.getPrincipal() == null ||
					authentication.getPrincipal().equals("anonymousUser")
			) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "Usuário não autenticado"));
			}

			String email = authentication.getName();
			return userService.getUserByEmail(email)
					.<ResponseEntity<?>>map(ResponseEntity::ok)
					.orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.body(Map.of("error", "Usuário não encontrado")));
		} catch (Exception e) {
			log.error("Erro ao buscar usuário atual: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
		try {
			passwordResetService.generateAndSendToken(request.getEmail());
			return ResponseEntity.ok(
					Map.of("message", "Se o e-mail existir, enviaremos um link de redefinição")
			);
		} catch (RuntimeException e) {
			return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
		try {
			passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
			return ResponseEntity.ok(Map.of("message", "Senha atualizada com sucesso!"));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * POST api/auth/refresh - Renova o access token usando o refresh token
	 */
	@PostMapping("/refresh")
	public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
		String refreshToken = request.get("refreshToken");

		if (refreshToken == null || refreshToken.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token ausente"));
		}

		try {
			String email = jwtService.extractUsernameFromRefreshToken(refreshToken);
			if (email == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "Token inválido ou expirado"));
			}

			User user = userService.getUserByEmail(email)
					.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

			// gera novo access token
			String newAccessToken = jwtService.generateAccessToken(user);

			return ResponseEntity.ok(Map.of(
					"message", "Access token renovado com sucesso!",
					"accessToken", newAccessToken
			));

		} catch (Exception e) {
			log.error("Erro ao renovar token: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Falha ao validar refresh token"));
		}
	}
}
