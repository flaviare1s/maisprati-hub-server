package com.maisprati.hub.infrastructure.security.oauth2;

import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	
	private final UserRepository userRepository;
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		String provider = userRequest.getClientRegistration().getRegistrationId();
		
		String email = switch (provider) {
			case "google", "github" -> (String) oAuth2User.getAttributes().get("email");
			default -> null;
		};
		
		String name = oAuth2User.getAttribute("name");
		
		// Captura os tokens de acesso/atualização
		String accessToken = userRequest.getAccessToken().getTokenValue();
		String refreshToken = null;
		if (userRequest.getAdditionalParameters().containsKey("refresh_token")) {
			refreshToken = (String) userRequest.getAdditionalParameters().get("refresh_token");
		}
		
		System.out.println("Access Token: " + accessToken);
		System.out.println("Refresh Token: " + refreshToken);
		
		// Buscar o usuário
		User user = userRepository.findByEmail(email).orElse(null);
		
//		// salvar refresh token no banco
//		if (user != null && refreshToken != null) {
//			user.setGoogleRefreshToken(refreshToken);
//			userRepository.save(user);
//		}
		
		return oAuth2User;
	}
}
