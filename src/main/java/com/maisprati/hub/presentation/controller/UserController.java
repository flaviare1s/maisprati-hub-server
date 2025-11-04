package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.security.auth.AuthController;
import com.maisprati.hub.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsável pelas operações de usuário.
 *
 * <p>As rotas públicas (como login e registro) estão em {@link AuthController}.
 * Este controller lida com operações internas, com restrição de roles.</p>
 */

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;
	
	/**
	 * GET api/users - Lista todos os usuários
	 */
	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
	public ResponseEntity<List<User>> all() {
		List<User> users = userService.getAllUsers();
		return ResponseEntity.ok(users);
	}

	/**
	 * GET api/users/{id} - Busca um usuário pelo id
	 * <p>O próprio usuário pode acessar seu perfil, ou ADMIN pode acessar qualquer.</p>
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
	public ResponseEntity<User> get(@PathVariable String id){
		return userService.getUserById(id)
			       .map(ResponseEntity::ok)
			       .orElse(ResponseEntity.notFound().build());
	}
	
	/**
	 * PUT api/users/{id} - Atualiza perfil de um usuário (aluno ou admin)
	 * <p>O próprio usuário pode atualizar seu perfil, ou ADMIN pode atualizar qualquer usuário.</p>
	 */
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
	public ResponseEntity<User> update(@PathVariable String id, @RequestBody User user){
		return userService.getUserById(id)
			       .map(existing -> {
				       user.setId(id);
				       User update = userService.updateUser(user);
				       return ResponseEntity.ok(update);
			       })
			       .orElse(ResponseEntity.notFound().build());
	}
	
	/**
	 * PUT api/users/admin - Atalho para atualizar o admin
	 */
	@PutMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> updateAdmin(@RequestBody User user){
		User admin = userService.getUserByEmail("admin@admin.com")
			             .orElseThrow(() -> new RuntimeException("Admin não encontrado"));
		
		user.setId(admin.getId()); // força o ID do admin
		User updated = userService.updateUser(user);
		return ResponseEntity.ok(updated);
	}
	
	/**
	 * DELETE api/users/{id} - Remove usuário
	 * <p>Somente ADMIN pode deletar usuários.</p>
	 */
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable String id) {
		if (userService.getUserById(id).isPresent()) {
			userService.deleteUser(id);
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * PATCH api/users/{id}/wants-group - Desativa o desejo de formar grupo
	 * <p>O próprio estudante pode desativar seu wantsGroup, ou ADMIN pode desativar para qualquer usuário.</p>
	 */
	@PatchMapping("/{id}/wants-group")
	@PreAuthorize("hasRole('ADMIN') or (#id == authentication.principal.id and hasRole('STUDENT'))")
	public ResponseEntity<User> disableWantsGroup(@PathVariable String id) {
		try {
			User updated = userService.disableWantsGroup(id);
			return ResponseEntity.ok(updated);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().build();
		}
	}
}
