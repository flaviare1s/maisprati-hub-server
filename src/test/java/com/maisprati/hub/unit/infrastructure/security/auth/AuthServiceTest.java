package com.maisprati.hub.unit.infrastructure.security.auth;

import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import com.maisprati.hub.infrastructure.security.auth.AuthService;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AuthServiceTest {
	
	@Mock private UserRepository userRepository;
	@Mock private PasswordEncoder passwordEncoder;
	@Mock private JwtService jwtService;
	@InjectMocks private AuthService authService;
	
	private User user;
	
	@BeforeEach
	void setup() {
		// Inicializa os mocks antes de cada teste
		MockitoAnnotations.openMocks(this);
		
		// Cria um usuário de teste
		user = new User();
		user.setEmail("email@test.com");
		user.setPassword("encrypted");
	}
	
	// TEST 1 — Carregar usuário por email existente
	@Test
	void loadUserByUsername_ShouldReturnUserWhenExists() {
		// Arrange: Mock do repositório retornando o usuário
		when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
		
		// Act: Chama o método loadUserByUsername
		var result = authService.loadUserByUsername("email@test.com");
		
		// Assert: Verifica se o usuário retornado é o esperado
		assertEquals(user, result);
	}
	
	// TEST 2 — Carregar usuário por email inexistente lança exceção
	@Test
	void loadUserByUsername_ShouldThrowWhenNotExists() {
		// Arrange: Mock do repositório retornando vazio
		when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.empty());
		
		// Act + Assert: Deve lançar UsernameNotFoundException
		assertThrows(UsernameNotFoundException.class,
			() -> authService.loadUserByUsername("email@test.com"));
	}
	
	// TEST 3 — Login com senha correta retorna tokens
	@Test
	void shouldReturnTokensWhenPasswordMatches() {
		// Arrange: Mocks para usuário, verificação de senha e geração de tokens
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("123", "encrypted")).thenReturn(true);
		when(jwtService.generateAccessToken(user)).thenReturn("access");
		when(jwtService.generateRefreshToken(user)).thenReturn("refresh");
		
		// Act: Chama o método de login
		var result = authService.login("email@test.com", "123");
		
		// Assert: Verifica se os tokens gerados são os esperados
		assertEquals("access", result.accessToken());
		assertEquals("refresh", result.refreshToken());
	}
	
	// TEST 4 — Login com senha incorreta lança exceção
	@Test
	void throwsErrorWhenPasswordInvalid() {
		// Arrange: Mock do usuário e senha incorreta
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong", "encrypted")).thenReturn(false);
		
		// Act & Assert: Deve lançar RuntimeException por senha inválida
		assertThrows(RuntimeException.class,
			() -> authService.login("email@test.com", "wrong"));
	}
}
