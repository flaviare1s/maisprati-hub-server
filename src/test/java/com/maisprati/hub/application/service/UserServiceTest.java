package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.enums.UserType;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Padrão: AAA (Arrange - Act - Assert)
 */
class UserServiceTest {
	
	@Mock private UserRepository userRepository;
	@Mock private PasswordEncoder passwordEncoder;
	@InjectMocks private UserService userService;
	
	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
  // TEST 1 - Registrar estudante com email novo
	@Test
	void shouldRegisterStudentWithEncryptedPasswordAndRole() {
		// Arrange - criar um usuário de exemplo
		User user = new User();
		user.setEmail("test@mail.com");
		user.setPassword("123");
		
		// quando verificar email, ele NÃO existe no banco
		when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
		// simular criptografia da senha
		when(passwordEncoder.encode("123")).thenReturn("encrypted");
		// quando salvar no banco, apenas retornar o mesmo objeto
		when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		
		// Act - chama o método a ser testado
		User result = userService.registerStudent(user);
		
		// Assert - verifica regras de negócio
		assertEquals(UserType.STUDENT, result.getType());
		assertEquals("encrypted", result.getPassword());
		assertNotNull(result.getCreatedAt());
		verify(userRepository, times(1)).save(result);
	}
	
	// TEST 2 - Email duplicado não pode cadastrar
	@Test
	void throwsErrorWhenEmailAlreadyExists() {
		User user = new User();
		user.setEmail("exists@mail.com");
		
		// email já cadastrado
		when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);
		// lança exceção - regra de negócio
		assertThrows(RuntimeException.class, () -> userService.registerStudent(user));
		// não dave salvar com algo inválido
		verify(userRepository, never()).save(any());
	}
	
	// TEST 3 - Atualizar usuário existente
	@Test
	void shouldUpdateUserFields() {
		User existing = new User();
		existing.setId("1");
		existing.setEmail("old@mail.com");
		
		User updated = new User();
		updated.setId("1");
		updated.setEmail("new@mail.com");
		
		when(userRepository.findById("1")).thenReturn(Optional.of(existing));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		User result = userService.updateUser(updated);
		
		// verifica se realmente mudou
		assertEquals("new@mail.com", result.getEmail());
		verify(userRepository, times(1)).save(existing);
	}
	
	// TEST 4 - Deletar usuário
	@Test
	void shouldDeleteUserById() {
		userService.deleteUser("1");
		verify(userRepository, times(1)).deleteById("1");
	}
}
