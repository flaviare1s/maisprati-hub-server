package com.maisprati.hub.infrastructure.security.jwt;

import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
		String token = this.recoverToken(request);
		
		try {
			if (token != null) {
				// Extrai o email do token
				String username = jwtService.extractUsername(token);
				
				// Só continua se o username existir e ainda não houver autenticação no contexto
				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					// Busca o usuário no banco pelo email
					userRepository.findByEmail(username)
						.filter(u -> jwtService.validateToken(token, u))
						.ifPresent(userDetails -> {
							var authentication = new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities()
							);
							SecurityContextHolder.getContext().setAuthentication(authentication);
						});
				}
			}
		} catch (Exception e) {
			log.error("JWT inválido ou expirado: {}", e.getMessage());
		}
		// Continua a cadeia de filtros
		filterChain.doFilter(request, response);
	}
	
	/**
	 * Recupera o token JWT do cookie "access_token".
	 *
	 * @param request requisição HTTP
	 * @return token extraído do cookie, ou null se não existir
	 */
	private String recoverToken(HttpServletRequest request) {
		// pega do Cookie "access_token"
		if (request.getCookies() != null) {
			for (var cookie : request.getCookies()) {
				if ("access_token".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
}
