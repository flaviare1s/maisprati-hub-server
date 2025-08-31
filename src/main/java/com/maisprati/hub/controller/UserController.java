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
	 * GET api/users - lista todos os usuários
	 * <p>
	 * TODO: restringir para ADMIN
	 */
	@GetMapping
	public ResponseEntity<List<User>> all() {
		List<User> users = userService.getAllUsers();
		return ResponseEntity.ok(users);
	}
	
	/**
	 * GET api/users/{id} - busca um usuário pelo id
	 */
	@GetMapping("/{id}")
	public ResponseEntity<User> get(@PathVariable String id){
		return userRepository.findById(id)
			       .map(ResponseEntity::ok)
			       .orElse(ResponseEntity.notFound().build());
	}
	
	/**
	 * PUT api/users/{id} - atualiza perfil de um usuário (aluno ou admin)
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
	 * PUT api/users/admin - atalho para atualizar o admin
	 */
	@PutMapping("/admin")
	public ResponseEntity<User> updateAdmin(@RequestBody User user){
		User admin = userRepository.findByEmail("admin@admin.com")
			             .orElseThrow(() -> new RuntimeException("Admin não encontrado"));
		
		user.setId(admin.getId()); // força o ID do admin
		User updated = userService.updateUser(user);
		return ResponseEntity.ok(updated);
	}
	
	/**
	 * DELETE api/users/{id} - remove usuário
	 * <p>
	 * TODO: restringir para ADMIN
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
