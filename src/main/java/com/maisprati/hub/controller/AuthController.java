package com.maisprati.hub.controller;

import com.maisprati.hub.model.User;
import com.maisprati.hub.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
	
	private final UserService userService;
	
	/**
	 * POST api/auth/register -> Cadastra um aluno no sistema
	 */
	@PostMapping("/register")
	public ResponseEntity<?> registerStudent(@RequestBody User user) {
		try {
			User saved = userService.registerStudent(user);
			return ResponseEntity.status(HttpStatus.CREATED).body(saved);
		} catch (RuntimeException e) {
			log.error("Erro ao cadastrar usuário: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
	
	/**
	 * POST api/auth/register -> Realiza login do usuário
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody User user) {
		try {
			User logged = userService.login(user.getEmail(), user.getPassword());
			// TODO: gerar token JWT
			return ResponseEntity.ok("Login realizado com sucesso para: " + logged.getEmail());
		} catch (RuntimeException e) {
			log.error("Erro ao fazer login: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}
}
