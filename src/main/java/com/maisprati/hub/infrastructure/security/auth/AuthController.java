package com.maisprati.hub.infrastructure.security.auth;

import com.maisprati.hub.application.service.PasswordResetService;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.application.service.UserService;
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

/**
 * Controller responsável pelos endpoints de autenticação e registro de usuários.
 *
 * <p>Este controller fornece métodos para:</p>
 * <ul>
 *     <li>Registrar um aluno no sistema</li>
 *     <li>Realizar login de usuários</li>
 *     <li>Buscar dados do usuário autenticado</li>
 * </ul>
 *
 * <p>As respostas HTTP são padronizadas utilizando {@link Map#of(Object, Object)} com:</p>
 * <ul>
 *     <li>Chave "message" para indicar mensagens de sucesso</li>
 *     <li>Chave "error" para indicar mensagens de erro</li>
 * </ul>
 *
 * <p>Isso evita a exposição direta de campos do usuário, prevenindo vulnerabilidades de XSS.</p>
 */

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
	
	private final UserService userService;
	private final AuthService authService;
	private final PasswordResetService passwordResetService;
	
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
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				       .body(Map.of("error", e.getMessage()));
		}
	}
	
	/**
	 * POST api/auth/login - Realiza login
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody User user) {
		try {
			String token = authService.login(user.getEmail(), user.getPassword());
			return ResponseEntity.ok(Map.of(
				"message", "Login realizado com sucesso!",
				"token", token
			));
		} catch (RuntimeException e) {
			log.error("Erro ao fazer login: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				       .body(Map.of("error", e.getMessage()));
		}
	}
	
	/**
	 * GET api/auth/me - Busca dados do usuário autenticado
	 */
	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			
			if (authentication == null || !authentication.isAuthenticated()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					       .body(Map.of("error", "Usuário não autenticado"));
			}
			
			String email = authentication.getName();
			User user = userService.getUserByEmail(email)
				            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
			
			return ResponseEntity.ok(user);
		} catch (RuntimeException e) {
			log.error("Erro ao buscar usuário atual: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				       .body(Map.of("error", e.getMessage()));
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
			return ResponseEntity.ok(
				Map.of("message", "Senha atualizada com sucesso!")
			);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
