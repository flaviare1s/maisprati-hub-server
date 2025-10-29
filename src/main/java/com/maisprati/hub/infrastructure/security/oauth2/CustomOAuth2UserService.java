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
		
		// Só busca, não cria
		User user = userRepository.findByEmail(email).orElse(null);
		
		return oAuth2User;
	}
}
