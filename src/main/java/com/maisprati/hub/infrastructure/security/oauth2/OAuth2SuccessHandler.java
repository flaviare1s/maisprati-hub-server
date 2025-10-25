package com.maisprati.hub.infrastructure.security.oauth2;

import com.maisprati.hub.application.service.UserService;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.security.jwt.JwtProperties;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	
	private final UserService userService;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    Authentication authentication) throws IOException {
		
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		Map<String, Object> attributes = oAuth2User.getAttributes();
		
		// Buscar email e nome de forma segura
		String email = (String) attributes.getOrDefault("email", null);
		String name = (String) attributes.getOrDefault("name", email);
		
		if (email == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email não fornecido pelo provedor OAuth2");
			return;
		}
		
		// Busca usuário, mas não cria
		User user = userService.getUserByEmail(email).orElse(null);
		
		String frontendBase = "http://localhost:5173"; // front local
		String targetUrl;
		
		if (user == null) {
			// Usuário não existe ~> manda para o registro
			targetUrl = UriComponentsBuilder.fromUriString(frontendBase + "/register")
				            .queryParam("email", email)
				            .queryParam("name", name)
				            .build()
				            .toUriString();
		} else {
			// Usuário já existe ~> manda para o dashboard
			targetUrl = frontendBase + "/dashboard";
			
			// Gera JWT e cookie só se o usuário existir
			String token = jwtService.generateToken(user, jwtProperties.getExpirationSeconds());
			ResponseCookie cookie = ResponseCookie.from("access_token", token)
				                        .httpOnly(true)
				                        .secure(jwtProperties.isSecureCookie())
				                        .path("/")
				                        .sameSite(jwtProperties.isSecureCookie() ? "None" : "Lax")
				                        .maxAge(jwtProperties.getExpirationSeconds())
				                        .build();
			response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		}
		// Redireciona
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
