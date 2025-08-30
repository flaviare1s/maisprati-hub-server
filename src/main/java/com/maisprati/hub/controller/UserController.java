package com.maisprati.hub.controller;

import com.maisprati.hub.model.User;
import com.maisprati.hub.repository.UserRepository;
import com.maisprati.hub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;
	private final UserRepository userRepository;
	
	/**
	 * GET api/users -> busca todos os usuários
	 */
	@GetMapping()
	public ResponseEntity<List<User>> all() {
		List<User> users = userService.getAllUsers();
		return ResponseEntity.ok(users);
	}
	
	/**
	 * GET api/users/{id} -> busca um usuário
	 */
	@GetMapping("/{id}")
	public ResponseEntity<User> get(@PathVariable String id){
		return userRepository.findById(id)
			       .map(ResponseEntity::ok)
			       .orElse(ResponseEntity.notFound().build());
	}
	
	/**
	 * UPDATE api/users/{id} -> atualiza usuário
	 */
	@PutMapping("/{id}")
	public ResponseEntity<User> update(@PathVariable String id, @RequestBody User user){
		return userService.getUserById(id)
			       .map(existing -> {
				       user.setId(id);
				       User update = userRepository.save(user);
				       return ResponseEntity.ok(update);
			       })
			       .orElse(ResponseEntity.notFound().build());
	}
	
	/**
	 * DELETE api/users/{id} -> remove usuário
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable String id) {
		if (userRepository.existsById(id)) {
			userRepository.deleteById(id);
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}