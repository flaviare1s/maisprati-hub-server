package com.maisprati.hub.config;

import com.maisprati.hub.repository.UserRepository;
import com.maisprati.hub.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticação JWT.
 * <p>
 * Este filtro intercepta cada requisição HTTP, extrai o token JWT do cabeçalho
 * "Authorization", valida o token e, caso seja válido, adiciona as informações
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
public class JwtTokenFilter extends OncePerRequestFilter {
	
	private final JwtService jwtService;
	private final UserRepository userRepository;
	
	@Override
	protected void doFilterInternal(@NotNull HttpServletRequest request,
	                                @NotNull HttpServletResponse response,
	                                @NotNull FilterChain filterChain) throws ServletException, IOException {
		
		// Recupera o token do header Authorization
		String token = this.recoverToken(request);
		
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
		// Continua a cadeia de filtros
		filterChain.doFilter(request, response);
	}
	
	/**
	 * Recupera o token JWT do header Authorization.
	 * @param request requisição HTTP
	 * @return token sem "Bearer ", ou null se não existir
	 */
	private String recoverToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		
		if (header == null || !header.startsWith("Bearer ")) return null;
		return header.replace("Bearer ", "");
	}
}
