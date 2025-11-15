package com.maisprati.hub.infrastructure.security.oauth2;

import com.maisprati.hub.application.service.UserService;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.security.jwt.JwtProperties;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
	private final JwtProperties jwtProperties;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    Authentication authentication) throws IOException {
		
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
		String provider = oauthToken.getAuthorizedClientRegistrationId(); // google + github
		OAuth2User oAuth2User = oauthToken.getPrincipal();
		Map<String, Object> attributes = oAuth2User.getAttributes();
		
		String email = (String) attributes.get("email");
		String name = (String) attributes.getOrDefault("name", email);
		
		if (email == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email não fornecido pelo provedor OAuth2");
			return;
		}
		
		// Busca ou cria usuário OAuth2
		User user = userService.registerOrLoginOAuth2User(email, name, provider);
		log.info("Usuário autenticado via OAuth2 [{}]: {}", provider, email);
		
		// Verifica se o usuário está ativo
		if (user.getIsActive() == null || !user.getIsActive()) {
			String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/login")
				                   .queryParam("error", "account_inactive")
				                   .queryParam("message", "Sua conta está inativa. Entre em contato com o administrador.")
				                   .build()
				                   .toUriString();
			
			getRedirectStrategy().sendRedirect(request, response, targetUrl);
			return;
		}
		
		// Gera tokens JWT
		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);
		
		// Define cookies JWT
		ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
			                              .httpOnly(true)
			                              .secure(jwtProperties.isSecureCookie())
			                              .sameSite(jwtProperties.isSecureCookie() ? "None" : "Lax")
			                              .path("/")
			                              .maxAge(jwtProperties.getAccessTokenExpiration())
			                              .build();
		
		ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
			                               .httpOnly(true)
			                               .secure(jwtProperties.isSecureCookie())
			                               .sameSite(jwtProperties.isSecureCookie() ? "None" : "Lax")
			                               .path("/")
			                               .maxAge(jwtProperties.getRefreshTokenExpiration())
			                               .build();
		
		response.setHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
		
		// Redireciona para o dashboard
		String targetUrl = "http://localhost:5173/dashboard";
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
