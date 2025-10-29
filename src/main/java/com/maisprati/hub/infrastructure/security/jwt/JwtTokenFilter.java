package com.maisprati.hub.infrastructure.security.jwt;

import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticação JWT.
 * <p>
 * Este filtro intercepta cada requisição HTTP, extrai o token JWT do cookie,
 * valida o token e, caso seja válido, adiciona as informações
 * do usuário autenticado no contexto de segurança do Spring.
 * </p>
 *
 * <p>
 * O objetivo é garantir que apenas usuários autenticados possam acessar
 * endpoints protegidos da aplicação.
 * </p>
 *
 * <p><b>Fluxo resumido:</b></p>
 * <ul>
 *   <li>Intercepta a requisição</li>
 *   <li>Extrai o token JWT</li>
 *   <li>Valida assinatura e expiração</li>
 *   <li>Define a autenticação no contexto de segurança</li>
 *   <li>Segue para o próximo filtro da cadeia</li>
 * </ul>
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
	
	private final JwtService jwtService;
	private final UserRepository userRepository;
	
	@Override
	protected void doFilterInternal(@NotNull HttpServletRequest request,
	                                @NotNull HttpServletResponse response,
	                                @NotNull FilterChain filterChain
	) throws ServletException, IOException {
		
		// Recupera o token dos cookies
		String accessToken = this.getCookie(request, "access_token");
		String refreshToken = this.getCookie(request, "refresh_token");
		
		try {
			if (accessToken != null && jwtService.isAccessTokenValid(accessToken)) {
				authenticateUser(accessToken, request);
			}
			else if (refreshToken != null && jwtService.isRefreshTokenValid(refreshToken)) {
				log.info("Access token expirado. Realizando refresh automático…");
				
				String newAccess = jwtService.refreshAccessToken(refreshToken);
				
				if (newAccess != null) {
					jwtService.addAccessTokenToResponse(response, newAccess);
					authenticateUser(newAccess, request);
					log.info("Refresh token OK, usuário autenticado novamente ✨");
				}
				else {
					log.warn("Refresh token inválido ou expirado.");
				}
			}
		} catch (Exception e) {
			log.error("Erro durante validação/refresh JWT: {}", e.getMessage());
		}
		// Continua a cadeia de filtros
		filterChain.doFilter(request, response);
	}
	
	private void authenticateUser(String token, HttpServletRequest request) {
		String username = jwtService.extractUsernameFromAccessToken(token);
		if (username == null) return;
		
		userRepository.findByEmail(username).ifPresent(userDetails -> {
			var auth = new UsernamePasswordAuthenticationToken(
				userDetails,
				null,
				userDetails.getAuthorities()
			);
			auth.setDetails(request);
			SecurityContextHolder.getContext().setAuthentication(auth);
		});
	}
	
	private String getCookie(HttpServletRequest request, String name) {
		if (request.getCookies() == null) return null;
		for (Cookie cookie : request.getCookies()) {
			if (name.equals(cookie.getName())) return cookie.getValue();
		}
		return null;
	}
}
