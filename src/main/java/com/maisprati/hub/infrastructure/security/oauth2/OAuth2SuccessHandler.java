package com.maisprati.hub.infrastructure.security.oauth2;

import com.maisprati.hub.application.service.UserService;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final UserService userService;
	private final JwtService jwtService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
										HttpServletResponse response,
										Authentication authentication) throws IOException {

		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		Map<String, Object> attributes = oAuth2User.getAttributes();

		String email = (String) attributes.getOrDefault("email", null);
		String name = (String) attributes.getOrDefault("name", email);

		if (email == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email não fornecido pelo provedor OAuth2");
			return;
		}

		User user = userService.getUserByEmail(email).orElse(null);

		String frontendBase = "https://maisprati-hub.vercel.app";
		String targetUrl;

		if (user == null) {
			// Usuário não existe -> redireciona para registro
			targetUrl = UriComponentsBuilder.fromUriString(frontendBase + "/register")
					.queryParam("email", email)
					.queryParam("name", name)
					.build()
					.toUriString();
		} else {
			if (user.getIsActive() == null || !user.getIsActive()) {
				log.warn("Tentativa de login OAuth2 de usuário inativo: {}", email);

				// Redireciona para login com erro
				targetUrl = UriComponentsBuilder.fromUriString(frontendBase + "/login")
						.queryParam("error", "account_inactive")
						.queryParam("message", "Sua conta está inativa. Entre em contato com o administrador.")
						.build()
						.toUriString();

				getRedirectStrategy().sendRedirect(request, response, targetUrl);
				return;
			}

			// Usuário existe e está ativo -> gera tokens
			String accessToken = jwtService.generateAccessToken(user);
			String refreshToken = jwtService.generateRefreshToken(user);

			log.info("OAuth2 Login bem-sucedido para: {}", email);

			targetUrl = UriComponentsBuilder.fromUriString(frontendBase + "/oauth2/callback")
					.queryParam("accessToken", accessToken)
					.queryParam("refreshToken", refreshToken)
					.build()
					.toUriString();
		}

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
