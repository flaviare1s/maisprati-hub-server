package com.maisprati.hub.infrastructure.security.jwt;

import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticação JWT.
 * <p>
 * Este filtro intercepta cada requisição HTTP, extrai o Access Token do cabeçalho
 * 'Authorization: Bearer <token>', valida o token e, caso seja válido, adiciona as informações
 * do usuário autenticado no contexto de segurança do Spring.
 * </p>
 *
 * <p>
 * O objetivo é garantir que apenas usuários com um token válido possam acessar
 * endpoints protegidos da aplicação. O Refresh Token é manipulado
 * pelo endpoint '/api/auth/refresh' e não por este filtro.
 * </p>
 *
 * <p><b>Fluxo resumido:</b></p>
 * <ul>
 * <li>Intercepta a requisição</li>
 * <li>Extrai o token do cabeçalho 'Authorization'</li>
 * <li>Valida assinatura e expiração (apenas do Access Token)</li>
 * <li>Define a autenticação no contexto de segurança</li>
 * <li>Segue para o próximo filtro da cadeia</li>
 * </ul>
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, // Anotação removida
									HttpServletResponse response, // Anotação removida
									FilterChain filterChain // Anotação removida
	) throws ServletException, IOException {

		// Tenta recuperar o token do cabeçalho Authorization
		String authHeader = request.getHeader("Authorization");
		String accessToken = null;

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			// Remove o prefixo "Bearer " para isolar o token
			accessToken = authHeader.substring(7);
		}

		try {
			// Se o token existe e é válido, autentica o usuário.
			// Se for nulo ou inválido, o filtro continua e o Spring Security
			// (devido ao .anyRequest().authenticated()) emitirá o 401, se necessário.
			if (accessToken != null && jwtService.isAccessTokenValid(accessToken)) {
				authenticateUser(accessToken, request);
			}
		} catch (Exception e) {
			// Loga erro de validação (ex: assinatura inválida), mas não impede
			// a requisição de seguir para endpoints públicos.
			log.error("Falha na validação do Access Token (JWT): {}", e.getMessage());
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
			// Adiciona os detalhes da requisição, como endereço IP, ao token de autenticação
			auth.setDetails(request);
			SecurityContextHolder.getContext().setAuthentication(auth);
		});
	}
}
