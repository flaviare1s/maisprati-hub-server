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

import java.util.List;
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
		
		when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
		when(passwordEncoder.encode("123")).thenReturn("encrypted");
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
		
		when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);
		assertThrows(RuntimeException.class, () -> userService.registerStudent(user));
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
		
		assertEquals("new@mail.com", result.getEmail());
		verify(userRepository, times(1)).save(existing);
	}
	
	// TEST 4 - Deletar usuário
	@Test
	void shouldDeleteUserById() {
		userService.deleteUser("1");
		verify(userRepository, times(1)).deleteById("1");
	}
	
	// TEST 5 - Deve retornar usuário por email
	@Test
	void shouldReturnUserByEmail() {
		User user = new User();
		user.setEmail("mail@mail.com");
		
		when(userRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(user));
		
		Optional<User> result = userService.getUserByEmail("mail@mail.com");
		
		assertTrue(result.isPresent());
		assertEquals("mail@mail.com", result.get().getEmail());
	}
	
	// TEST 6 - Deve retornar usuário quando encontrado por ID
	@Test
	void shouldReturnUserWhenFoundById() {
		User user = new User();
		user.setId("10");
		when(userRepository.findById("10")).thenReturn(Optional.of(user));
		
		Optional<User> result = userService.getUserById("10");
		
		assertTrue(result.isPresent());
		assertEquals("10", result.get().getId());
		verify(userRepository).findById("10");
	}
	
	// TEST 7 - Deve retornar vazio quando usuário não encontrado por ID
	@Test
	void shouldReturnEmptyWhenUserNotFoundById() {
		when(userRepository.findById("404")).thenReturn(Optional.empty());
		
		Optional<User> result = userService.getUserById("404");
		
		assertTrue(result.isEmpty());
		verify(userRepository).findById("404");
	}
	
	// TEST 8 - Deve retornar usuário quando encontrado por email
	@Test
	void shouldReturnUserWhenFoundByEmailAgain() {
		User user = new User();
		user.setEmail("found@mail.com");
		when(userRepository.findByEmail("found@mail.com")).thenReturn(Optional.of(user));
		
		Optional<User> result = userService.getUserByEmail("found@mail.com");
		
		assertTrue(result.isPresent());
		assertEquals("found@mail.com", result.get().getEmail());
		verify(userRepository).findByEmail("found@mail.com");
	}
	
	// TEST 9 - Deve retornar vazio quando usuário não encontrado por email
	@Test
	void shouldReturnEmptyWhenUserNotFoundByEmail() {
		when(userRepository.findByEmail("notfound@mail.com")).thenReturn(Optional.empty());
		
		Optional<User> result = userService.getUserByEmail("notfound@mail.com");
		
		assertTrue(result.isEmpty());
		verify(userRepository).findByEmail("notfound@mail.com");
	}
	
	// TEST 10 - Deve lançar exceção ao tentar atualizar usuário inexistente
	@Test
	void shouldThrowExceptionWhenUpdatingNonexistentUser() {
		User user = new User();
		user.setId("999");
		
		when(userRepository.findById("999")).thenReturn(Optional.empty());
		
		assertThrows(RuntimeException.class, () -> userService.updateUser(user));
		verify(userRepository, never()).save(any());
	}
	
	// TEST 11 - Atualizar estudante com whatsapp e groupClass preenchidos
	@Test
	void shouldUpdateStudentWithWhatsappAndGroupClass() {
		User existing = new User();
		existing.setId("1");
		existing.setType(UserType.STUDENT);
		
		User updated = new User();
		updated.setId("1");
		updated.setType(UserType.STUDENT);
		updated.setWhatsapp("99999-9999");
		updated.setGroupClass("3A");
		
		when(userRepository.findById("1")).thenReturn(Optional.of(existing));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		User result = userService.updateUser(updated);
		
		assertEquals("99999-9999", result.getWhatsapp());
		assertEquals("3A", result.getGroupClass());
		verify(userRepository).save(existing);
	}
	
	// TEST 12 - Atualizar estudante com campos vazios (devem virar null)
	@Test
	void shouldSetNullWhenStudentFieldsAreEmpty() {
		User existing = new User();
		existing.setId("1");
		existing.setType(UserType.STUDENT);
		existing.setWhatsapp("old");
		existing.setGroupClass("old");
		
		User updated = new User();
		updated.setId("1");
		updated.setType(UserType.STUDENT);
		updated.setWhatsapp("   ");
		updated.setGroupClass("");
		
		when(userRepository.findById("1")).thenReturn(Optional.of(existing));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		User result = userService.updateUser(updated);
		
		assertNull(result.getWhatsapp());
		assertNull(result.getGroupClass());
		verify(userRepository).save(existing);
	}
	
	// TEST 13 - Deve retornar todos os usuários
	@Test
	void shouldReturnAllUsers() {
		when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));
		
		var result = userService.getAllUsers();
		
		assertEquals(2, result.size());
		verify(userRepository).findAll();
	}
}
