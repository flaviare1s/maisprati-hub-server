package com.maisprati.hub.infrastructure.security.oauth2;

import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import com.maisprati.hub.infrastructure.security.jwt.JwtProperties;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
	
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	private final UserRepository userRepository;
	
	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request, HttpServletResponse response, Authentication authentication
	) throws IOException, ServletException {
		
		DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
		String email = oauthUser.getAttribute("email");
		
		User user = userRepository.findByEmail(email).orElseThrow();
		String token = jwtService.generateToken(user);
		
		ResponseCookie cookie = ResponseCookie.from("access_token", token)
			                        .httpOnly(true)
			                        .secure(jwtProperties.isSecureCookie())
			                        .path("/")
			                        .sameSite(jwtProperties.isSecureCookie() ? "None" : "Lax")
			                        .maxAge(jwtProperties.getExpirationSeconds())
			                        .build();
		
		response.addHeader("Set-Cookie", cookie.toString());
		response.sendRedirect("http://localhost:5173/dashboard"); // redirecionamento do frontend local
	}
}
