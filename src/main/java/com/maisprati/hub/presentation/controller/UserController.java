package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.domain.enums.EmotionalStatus;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.security.auth.AuthController;
import com.maisprati.hub.application.service.UserService;
import com.maisprati.hub.application.service.TeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller responsável pelas operações de usuário.
 *
 * <p>As rotas públicas (como login e registro) estão em {@link AuthController}.
 * Este controller lida com operações internas, com restrição de roles.</p>
 */

@Tag(name = "Users")
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final TeamService teamService;

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

	/**
	 * PATCH api/users/{id}/deactivate - Inativa um usuário
	 * <p>O próprio usuário pode se inativar, ou ADMIN pode inativar qualquer usuário.</p>
	 * <p>Ao inativar, o usuário é automaticamente removido de seu time ativo (se houver).</p>
	 */
	@PatchMapping("/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
	public ResponseEntity<Map<String, Object>> deactivateUser(@PathVariable String id) {
		try {
			// Verifica se usuário está em time ativo e remove antes de inativar
			if (teamService.isUserInActiveTeam(id)) {
				// Remove o usuário do time com a razão de inativação
				teamService.removeUserFromAllActiveTeams(id, "Usuário inativado");
			}

			User updated = userService.deactivateUser(id);

			return ResponseEntity.ok(Map.of(
					"user", updated,
					"message", "Usuário inativado com sucesso"
			));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest()
					.body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * PATCH api/users/{id}/activate - Reativa um usuário
	 * <p>Somente ADMIN pode reativar usuários.</p>
	 */
	@PatchMapping("/{id}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> activateUser(@PathVariable String id) {
		try {
			User updated = userService.activateUser(id);

			return ResponseEntity.ok(Map.of(
					"user", updated,
					"message", "Usuário reativado com sucesso"
			));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest()
					.body(Map.of("error", e.getMessage()));
		}
	}

	@PatchMapping("/{userId}/reset-group-preferences")
	public ResponseEntity<User> resetGroupPreferences(@PathVariable String userId) {
		try {
			User updatedUser = userService.resetGroupPreferences(userId);
			return ResponseEntity.ok(updatedUser);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(null);
		}
	}


	/**
	 * GET api/users/emotional-status - Lista todos os estados emocionais possíveis
	 */
	@GetMapping("/emotional-status")
	public ResponseEntity<List<EmotionalStatus>> getAllEmotionalStatuses() {
		return ResponseEntity.ok(userService.getAllEmotionalStatuses());
	}

	/**
	 * PATCH api/users/{id}/emotional-status - Atualiza o estado emocional do usuário
	 * O próprio usuário pode atualizar, ou o ADMIN.
	 */
	@PatchMapping("/{id}/emotional-status")
	@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
	public ResponseEntity<?> updateEmotionalStatus(
			@PathVariable String id,
			@RequestBody Map<String, String> body
	) {
		try {
			String statusString = body.get("emotionalStatus");
			EmotionalStatus newStatus = (statusString == null || statusString.isBlank())
					? null
					: EmotionalStatus.valueOf(statusString.toUpperCase());

			User updated = userService.updateEmotionalStatus(id, newStatus);
			return ResponseEntity.ok(updated);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Estado emocional inválido."));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
