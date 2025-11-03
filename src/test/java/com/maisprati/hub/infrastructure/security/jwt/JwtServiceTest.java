package com.maisprati.hub.infrastructure.security.jwt;

import com.maisprati.hub.domain.enums.UserType;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
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
		userRepository = mock(UserRepository.class);
		JwtProperties jwtProperties = mock(JwtProperties.class);
		
		// Gera chave segura de 512 bits para HS512
		Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
		String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
		
		when(jwtProperties.getSecret()).thenReturn(base64Key);
		when(jwtProperties.getAccessTokenExpiration()).thenReturn(3600L);
		when(jwtProperties.getRefreshTokenExpiration()).thenReturn(7200L);
		
		jwtService = new JwtService(userRepository, jwtProperties);
		jwtService.init();
	}
	
	@Test
	void generateAndExtractAccessToken_ShouldWork() {
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		String token = jwtService.generateAccessToken(user);
		
		assertNotNull(token);
		String username = jwtService.extractUsernameFromAccessToken(token);
		assertEquals(user.getEmail(), username);
	}
	
	@Test
	void generateAndExtractRefreshToken_ShouldWork() {
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		String token = jwtService.generateRefreshToken(user);
		
		assertNotNull(token);
		String username = jwtService.extractUsernameFromRefreshToken(token);
		assertEquals(user.getEmail(), username);
	}
	
	@Test
	void isAccessTokenValid_ShouldReturnTrueForValidToken() {
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		String token = jwtService.generateAccessToken(user);
		assertTrue(jwtService.isAccessTokenValid(token));
	}
	
	@Test
	void isRefreshTokenValid_ShouldReturnTrueForValidToken() {
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		String token = jwtService.generateRefreshToken(user);
		assertTrue(jwtService.isRefreshTokenValid(token));
	}
	
	@Test
	void refreshAccessToken_ShouldReturnNewAccessToken() {
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		String refreshToken = jwtService.generateRefreshToken(user);
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		
		String newAccessToken = jwtService.refreshAccessToken(refreshToken);
		assertNotNull(newAccessToken);
		assertTrue(jwtService.isAccessTokenValid(newAccessToken));
	}
	
	@Test
	void addAccessTokenToResponse_ShouldAddCookie() {
		HttpServletResponse response = mock(HttpServletResponse.class);
		User user = new User();
		user.setId("1");
		user.setEmail("teste@email.com");
		user.setType(UserType.STUDENT);
		
		String token = jwtService.generateAccessToken(user);
		
		jwtService.addAccessTokenToResponse(response, token);
		
		ArgumentCaptor<jakarta.servlet.http.Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
		verify(response).addCookie(captor.capture());
		assertEquals("access_token", captor.getValue().getName());
		assertEquals(token, captor.getValue().getValue());
	}
}
