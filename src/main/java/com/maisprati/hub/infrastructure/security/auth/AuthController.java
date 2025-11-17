package com.maisprati.hub.infrastructure.security.auth;

import com.maisprati.hub.application.service.PasswordResetService;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.application.service.UserService;
import com.maisprati.hub.infrastructure.security.jwt.JwtProperties;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import com.maisprati.hub.presentation.dto.ForgotPasswordRequest;
import com.maisprati.hub.presentation.dto.LoginRequest;
import com.maisprati.hub.presentation.dto.RegisterStudentRequest;
import com.maisprati.hub.presentation.dto.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
@Tag(name = "Authorization")
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
	
	private final UserService userService;
	private final AuthService authService;
	private final PasswordResetService passwordResetService;
	private final JwtProperties jwtProperties;
	private final JwtService jwtService;
	
	/**
	 * POST api/auth/register - Cadastra um aluno
	 */
	@Operation(summary = "Registrar novo aluno", description = "🟢 **Público** - Qualquer pessoa pode se registrar como STUDENT")
	@SecurityRequirements
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Cadastro realizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Erro ao cadastrar aluno")
	})
	@PostMapping("/register")
	public ResponseEntity<?> registerStudent(@RequestBody RegisterStudentRequest request) {
		try {
			User newUser = new User();

			newUser.setName(request.getName());
			newUser.setEmail(request.getEmail());
			newUser.setPassword(request.getPassword());

			newUser.setType(request.getType());
			newUser.setWhatsapp(request.getWhatsapp());
			newUser.setGroupClass(request.getGroupClass());

			newUser.setHasGroup(request.getHasGroup() != null ? request.getHasGroup() : false);
			newUser.setWantsGroup(request.getWantsGroup() != null ? request.getWantsGroup() : false);
			newUser.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

			newUser.setCodename(request.getCodename());
			newUser.setAvatar(request.getAvatar());
			newUser.setEmotionalStatus(request.getEmotionalStatus());

			userService.registerStudent(newUser);

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("message", "Cadastro realizado com sucesso!"));
		} catch (RuntimeException e) {
			log.error("Erro ao cadastrar aluno: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
		}
	}
	
	/**
	 * POST api/auth/login - Realiza login
	 */
	@Operation(summary = "Realizar login", security = {})
	@SecurityRequirements
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
			@ApiResponse(responseCode = "401", description = "Credenciais inválidas")
	})
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
		try {
			var tokens = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
			
			ResponseCookie accessCookie = ResponseCookie.from("access_token", tokens.accessToken())
				                              .httpOnly(true)
				                              .secure(jwtProperties.isSecureCookie()) // false em dev, true em prod
				                              .path("/")
				                              .sameSite(jwtProperties.isSecureCookie() ? "None" : "Lax")
							                        // None: cookie pode ser enviado em requisições cross-site
							                        // Lax: cookie é enviado em requisições (axios) para outra porta no mesmo host
							                        .maxAge(jwtProperties.getAccessTokenExpiration())
							                        .build();
			
			ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.refreshToken())
				                               .httpOnly(true)
				                               .secure(jwtProperties.isSecureCookie())
				                               .path("/")
				                               .sameSite(jwtProperties.isSecureCookie() ? "None" : "Lax")
				                               .maxAge(jwtProperties.getRefreshTokenExpiration())
				                               .build();
			
			log.info("Access-Cookie: {}", accessCookie);
			log.info("Refresh-Cookie: {}", refreshCookie);
			
			return ResponseEntity.ok()
				       .header(HttpHeaders.SET_COOKIE, accessCookie.toString(), refreshCookie.toString())
				       .body(Map.of("message", "Login realizado com sucesso!"));
		} catch (RuntimeException e) {
			log.error("Erro ao fazer login: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
		}
	}
	
	/**
	 * POST api/auth/logout - Faz logout do usuário
	 */
	@Operation(summary = "Fazer logout")
	@PostMapping("/logout")
	public ResponseEntity<?> logout() {
		try {
			// cria um cookie "vazio" para sobrescrever o anterior e expirar imediatamente
			ResponseCookie expiredAccess = ResponseCookie.from("access_token", "")
				                               .httpOnly(true)
				                               .secure(jwtProperties.isSecureCookie())
				                               .path("/")
				                               .sameSite(jwtProperties.isSecureCookie() ? "None" : "Lax")
				                               .maxAge(0) // expira imediatamente
				                               .build();
			
			ResponseCookie expiredRefresh = ResponseCookie.from("refresh_token", "")
				                                .httpOnly(true)
				                                .secure(jwtProperties.isSecureCookie())
				                                .path("/")
				                                .sameSite(jwtProperties.isSecureCookie() ? "None" : "Lax")
				                                .maxAge(0)
				                                .build();

			return ResponseEntity.ok()
				       .header(HttpHeaders.SET_COOKIE, expiredAccess.toString(), expiredRefresh.toString())
				       .body(Map.of("message", "Logout realizado com sucesso!"));
		} catch (RuntimeException e) {
			log.error("Erro ao fazer logout: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
		}
	}
	
	/**
	 * GET api/auth/me - Busca dados do usuário autenticado
	 */
	@Operation(summary = "Buscar dados do usuário autenticado", description = "🔒 **Autenticado** - Qualquer usuário logado (ADMIN ou STUDENT)")
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

	@Operation(summary = "Esqueci minha senha")
	@SecurityRequirements
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

	@Operation(summary = "Redefinir senha")
	@SecurityRequirements
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
		try {
			passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
			return ResponseEntity.ok(Map.of("message", "Senha atualizada com sucesso!"));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@Operation(summary = "Renovar access token")
	@SecurityRequirements
	@PostMapping("/refresh")
	public ResponseEntity<?> refreshToken(
		@CookieValue(value = "refresh_token", required = false) String refreshToken) {
		log.info("Entrou no endpoint /refresh");
		log.info("Refresh token recebido: {}", refreshToken);
		
		if (refreshToken == null || refreshToken.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token ausente"));
		}
		
		try {
			String email = jwtService.extractUsernameFromRefreshToken(refreshToken);
			if (email == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					       .body(Map.of("error", "Token inválido ou expirado"));
			}
			
			User user = userService.getUserByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
			
			// gera novo access token
			String newAccessToken = jwtService.generateAccessToken(user);
			
			ResponseCookie newAccessCookie = ResponseCookie.from("access_token", newAccessToken)
				                                 .httpOnly(true)
				                                 .secure(jwtProperties.isSecureCookie())
				                                 .path("/")
				                                 .sameSite(jwtProperties.isSecureCookie() ? "None" : "Lax")
				                                 .maxAge(jwtProperties.getAccessTokenExpiration())
				                                 .build();
			
			log.info("New-Access-Cookie: {}", newAccessCookie);
			
			return ResponseEntity.ok()
				       .header(HttpHeaders.SET_COOKIE, newAccessCookie.toString())
				       .body(Map.of("message", "Access token renovado com sucesso!"));
			
		} catch (Exception e) {
			log.error("Erro ao renovar token: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				       .body(Map.of("error", "Falha ao validar refresh token"));
		}
	}
}
