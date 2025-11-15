package com.maisprati.hub.unit.infrastructure.security.jwt;

import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import com.maisprati.hub.infrastructure.security.jwt.JwtTokenFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenFilterTest {
	
	// Dependências mockadas
	@Mock private JwtService jwtService;
	@Mock private UserRepository userRepository;
	@Mock private HttpServletRequest request;
	@Mock private HttpServletResponse response;
	@Mock private FilterChain filterChain;
	
	private JwtTokenFilter filter;
	
	@BeforeEach
	void setup() {
		// Inicializa os mocks e cria a instância do filtro
		MockitoAnnotations.openMocks(this);
		filter = new JwtTokenFilter(jwtService, userRepository);
		SecurityContextHolder.clearContext(); // limpa contexto antes de cada teste
	}
	
	@Test
	void shouldAuthenticateUserWhenAccessTokenIsValid() throws Exception {
		// Cenário: token de acesso válido
		Cookie accessCookie = new Cookie("access_token", "validAccess");
		when(request.getCookies()).thenReturn(new Cookie[]{accessCookie});
		when(jwtService.isAccessTokenValid("validAccess")).thenReturn(true);
		when(jwtService.extractUsernameFromAccessToken("validAccess")).thenReturn("user@mail.com");
		
		// Mock de usuário autenticável
		User mockUser = mock(User.class);
		when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(mockUser));
		when(mockUser.getAuthorities()).thenReturn(null);
		
		// Ação: executa o filtro
		filter.doFilterInternal(request, response, filterChain);
		
		// Verificações
		verify(jwtService).isAccessTokenValid("validAccess");
		verify(userRepository).findByEmail("user@mail.com");
		verify(filterChain).doFilter(request, response);
		
		// Deve haver autenticação configurada
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
	}
	
	@Test
	void shouldRefreshTokenWhenAccessTokenExpiredAndRefreshValid() throws Exception {
		// Cenário: access token inválido, mas refresh válido
		Cookie refreshCookie = new Cookie("refresh_token", "validRefresh");
		when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
		
		when(jwtService.isAccessTokenValid(any())).thenReturn(false);
		when(jwtService.isRefreshTokenValid("validRefresh")).thenReturn(true);
		when(jwtService.refreshAccessToken("validRefresh")).thenReturn("newAccess");
		when(jwtService.extractUsernameFromAccessToken("newAccess")).thenReturn("user@mail.com");
		
		// Mock do usuário
		User user = mock(User.class);
		when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
		when(user.getAuthorities()).thenReturn(null);
		
		// Executa o filtro
		filter.doFilterInternal(request, response, filterChain);
		
		// Verifica se o refresh foi feito corretamente
		verify(jwtService).isRefreshTokenValid("validRefresh");
		verify(jwtService).refreshAccessToken("validRefresh");
		verify(jwtService).addAccessTokenToResponse(response, "newAccess");
		verify(userRepository).findByEmail("user@mail.com");
		verify(filterChain).doFilter(request, response);
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
	}
	
	@Test
	void shouldSkipWhenNoTokensPresent() throws Exception {
		// Cenário: sem cookies (sem tokens)
		when(request.getCookies()).thenReturn(null);
		
		filter.doFilterInternal(request, response, filterChain);
		
		// Nenhum método de validação de token deve ser chamado
		verify(jwtService, never()).isAccessTokenValid(any());
		verify(jwtService, never()).isRefreshTokenValid(any());
		verify(filterChain).doFilter(request, response);
		
		// Nenhuma autenticação deve ser criada
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}
	
	@Test
	void shouldHandleExceptionGracefully() throws Exception {
		// Cenário: exceção durante a validação do token
		Cookie cookie = new Cookie("access_token", "invalid");
		when(request.getCookies()).thenReturn(new Cookie[]{cookie});
		when(jwtService.isAccessTokenValid("invalid")).thenThrow(new RuntimeException("boom"));
		
		// A exceção não deve interromper a execução
		filter.doFilterInternal(request, response, filterChain);
		
		verify(filterChain).doFilter(request, response);
	}
	
	@Test
	void shouldReturnNullIfCookieNotFound() {
		// Cenário: nenhum cookie com o nome esperado
		when(request.getCookies()).thenReturn(new Cookie[]{
			new Cookie("other", "123")
		});
		
		// Chama o método privado via reflexão
		String token = invokePrivateGetCookie("access_token");
		
		// Deve retornar null
		assertNull(token);
	}
	
	/**
	 * Método auxiliar para acessar o método privado getCookie()
	 * via reflexão e testá-lo separadamente.
	 */
	private String invokePrivateGetCookie(String name) {
		try {
			var method = JwtTokenFilter.class.getDeclaredMethod("getCookie", HttpServletRequest.class, String.class);
			method.setAccessible(true);
			return (String) method.invoke(filter, request, name);
		} catch (Exception e) {
			fail("Erro ao chamar método privado: " + e.getMessage());
			return null;
		}
	}
}
