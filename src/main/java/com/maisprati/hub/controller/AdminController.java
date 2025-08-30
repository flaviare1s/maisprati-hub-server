package com.maisprati.hub.controller;

import com.maisprati.hub.model.User;
import com.maisprati.hub.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/admin")
@Slf4j
public class AdminController {
	
	private final UserService userService;
	
	/**
	 * POST api/admin/professors -> Cria um professor no sistema
	 */
	@PostMapping("/professors")
	public ResponseEntity<?> createProfessor(@RequestBody User user) {
		try {
			User saved = userService.registerProfessor(user);
			return ResponseEntity.status(HttpStatus.CREATED).body(saved);
		} catch (RuntimeException e) {
			log.error("Erro ao criar professor: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}
