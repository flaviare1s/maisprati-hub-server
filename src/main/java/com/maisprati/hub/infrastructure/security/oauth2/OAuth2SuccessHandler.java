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
		
		String frontendBase = "https://maisprati-hub.vercel.app";
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
			String accessToken = jwtService.generateAccessToken(user);
			String refreshToken = jwtService.generateRefreshToken(user);
			
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
		}
		// Redireciona
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
