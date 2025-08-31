package com.maisprati.hub.controller;

import com.maisprati.hub.model.User;
import com.maisprati.hub.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller responsável pelos endpoints de autenticação e registro de usuários.
 *
 * <p>Este controller fornece métodos para:</p>
 * <ul>
 *     <li>Registrar um aluno no sistema</li>
 *     <li>Realizar login de usuários</li>
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
	
	/**
	 * POST api/auth/register - Cadastra um aluno
	 */
	@PostMapping("/register")
	public ResponseEntity<?> registerStudent(@RequestBody User user) {
		try {
			userService.registerStudent(user);
			Map<String, String> response = Map.of("message", "Cadastro realizado com sucesso!");
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (RuntimeException e) {
			log.error("Erro ao cadastrar aluno: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
		}
	}
	
	/**
	 * POST api/auth/login - Realiza login
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody User user) {
		try {
			userService.login(user.getEmail(), user.getPassword());
			Map<String, String> response = Map.of("message", "Login realizado com sucesso!");
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			log.error("Erro ao fazer login: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
		}
	}
}
