package com.maisprati.hub.unit.infrastructure.security.oauth2;

import com.maisprati.hub.application.service.UserService;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.security.jwt.JwtProperties;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import com.maisprati.hub.infrastructure.security.oauth2.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

class OAuth2SuccessHandlerTest {
	
	@Mock private UserService userService;
	@Mock private JwtService jwtService;
	@Mock private JwtProperties jwtProperties;
	@Mock private HttpServletRequest request;
	@Mock private HttpServletResponse response;
	@Mock private Authentication authentication;
	@Mock private OAuth2User oAuth2User;
	
	@InjectMocks private OAuth2SuccessHandler successHandler;
	
	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		when(jwtProperties.isSecureCookie()).thenReturn(false);
		when(jwtProperties.getAccessTokenExpiration()).thenReturn(3600L);
		when(jwtProperties.getRefreshTokenExpiration()).thenReturn(7200L);
	}
	
	@Test
	void shouldReturnErrorWhenEmailIsNull() throws IOException {
		// Simula OAuth2User sem email
		when(authentication.getPrincipal()).thenReturn(oAuth2User);
		when(oAuth2User.getAttributes()).thenReturn(Map.of("name", "No Email"));
		
		// Executa o handler
		successHandler.onAuthenticationSuccess(request, response, authentication);
		
		// Verifica se o erro HTTP 400 foi enviado
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Email não fornecido pelo provedor OAuth2");
	}
	
	@Test
	void shouldRedirectToRegisterWhenUserDoesNotExist() throws IOException {
		// Simula OAuth2User com email de novo usuário
		when(authentication.getPrincipal()).thenReturn(oAuth2User);
		when(oAuth2User.getAttributes()).thenReturn(Map.of("email", "new@mail.com", "name", "New User"));
		when(userService.getUserByEmail("new@mail.com")).thenReturn(Optional.empty());
		
		// Corrige mock para redirecionamento
		when(response.encodeRedirectURL(anyString())).thenAnswer(inv -> inv.getArgument(0));
		
		// Executa handler
		successHandler.onAuthenticationSuccess(request, response, authentication);
		
		// URL esperada para redirecionamento
		String expectedUrl = UriComponentsBuilder
			                     .fromUriString("http://localhost:5173/register")
			                     .queryParam("email", "new@mail.com")
			                     .queryParam("name", "New User")
			                     .build()
			                     .toUriString();
		
		// Verifica que não houve envio de erro e houve redirecionamento correto
		verify(response, never()).sendError(anyInt(), anyString());
		verify(response).sendRedirect(expectedUrl);
	}
	
	@Test
	void shouldGenerateTokensAndRedirectWhenUserExists() throws IOException {
		// Simula usuário existente
		User user = new User();
		user.setEmail("exists@mail.com");
		
		when(authentication.getPrincipal()).thenReturn(oAuth2User);
		when(oAuth2User.getAttributes()).thenReturn(Map.of("email", "exists@mail.com", "name", "Existing User"));
		when(userService.getUserByEmail("exists@mail.com")).thenReturn(Optional.of(user));
		when(jwtService.generateAccessToken(user)).thenReturn("access");
		when(jwtService.generateRefreshToken(user)).thenReturn("refresh");
		
		// Executa handler
		successHandler.onAuthenticationSuccess(request, response, authentication);
		
		// Verifica geração de tokens e headers no response
		verify(jwtService).generateAccessToken(user);
		verify(jwtService).generateRefreshToken(user);
		verify(response, atLeastOnce()).setHeader(any(), contains("access_token"));
		verify(response, atLeastOnce()).addHeader(any(), contains("refresh_token"));
	}
}
