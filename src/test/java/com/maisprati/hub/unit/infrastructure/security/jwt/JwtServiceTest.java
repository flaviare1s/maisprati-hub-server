package com.maisprati.hub.unit.infrastructure.security.jwt;

import com.maisprati.hub.domain.enums.UserType;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import com.maisprati.hub.infrastructure.security.jwt.JwtProperties;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {
	
	private JwtService jwtService;
	private UserRepository userRepository;
	
	@BeforeEach
	void setup() {
		// Mock do repositório e das propriedades do JWT
		userRepository = mock(UserRepository.class);
		JwtProperties jwtProperties = mock(JwtProperties.class);
		
		// Gera chave secreta segura de 512 bits para assinatura HS512
		Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
		String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
		
		// Configura mock das propriedades
		when(jwtProperties.getSecret()).thenReturn(base64Key);
		when(jwtProperties.getAccessTokenExpiration()).thenReturn(3600L);   // 1 hora
		when(jwtProperties.getRefreshTokenExpiration()).thenReturn(7200L);  // 2 horas
		
		// Cria instância do serviço e inicializa a chave
		jwtService = new JwtService(userRepository, jwtProperties);
		jwtService.init();
	}
	
	@Test
	void generateAndExtractAccessToken_ShouldWork() {
		// Cria usuário de teste
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		// Gera token de acesso
		String token = jwtService.generateAccessToken(user);
		assertNotNull(token);  // Deve gerar token
		
		// Extrai username do token e compara com o email do usuário
		String username = jwtService.extractUsernameFromAccessToken(token);
		assertEquals(user.getEmail(), username);
	}
	
	@Test
	void generateAndExtractRefreshToken_ShouldWork() {
		// Cria usuário de teste
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		// Gera token de refresh
		String token = jwtService.generateRefreshToken(user);
		assertNotNull(token);  // Deve gerar token
		
		// Extrai username do token de refresh e compara com o email do usuário
		String username = jwtService.extractUsernameFromRefreshToken(token);
		assertEquals(user.getEmail(), username);
	}
	
	@Test
	void isAccessTokenValid_ShouldReturnTrueForValidToken() {
		// Cria usuário de teste
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		// Gera token válido e verifica se o método retorna true
		String token = jwtService.generateAccessToken(user);
		assertTrue(jwtService.isAccessTokenValid(token));
	}
	
	@Test
	void isRefreshTokenValid_ShouldReturnTrueForValidToken() {
		// Cria usuário de teste
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		// Gera token de refresh válido e verifica se o método retorna true
		String token = jwtService.generateRefreshToken(user);
		assertTrue(jwtService.isRefreshTokenValid(token));
	}
	
	@Test
	void refreshAccessToken_ShouldReturnNewAccessToken() {
		// Cenário: gerar token de refresh e criar novo access token
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		String refreshToken = jwtService.generateRefreshToken(user);
		
		// Mock para garantir que o usuário existe no repositório
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		
		// Executa refresh e valida resultado
		String newAccessToken = jwtService.refreshAccessToken(refreshToken);
		assertNotNull(newAccessToken);
		assertTrue(jwtService.isAccessTokenValid(newAccessToken));
	}
	
	@Test
	void addAccessTokenToResponse_ShouldAddCookie() {
		// Mock de HttpServletResponse
		HttpServletResponse response = mock(HttpServletResponse.class);
		
		// Usuário de teste
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		// Gera token de acesso
		String token = jwtService.generateAccessToken(user);
		
		// Adiciona o token no response
		jwtService.addAccessTokenToResponse(response, token);
		
		// Captura o cookie adicionado e verifica nome e valor
		ArgumentCaptor<jakarta.servlet.http.Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
		verify(response).addCookie(captor.capture());
		assertEquals("access_token", captor.getValue().getName());
		assertEquals(token, captor.getValue().getValue());
	}
}
