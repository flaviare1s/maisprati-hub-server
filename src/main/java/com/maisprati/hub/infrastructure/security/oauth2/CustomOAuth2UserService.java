package com.maisprati.hub.infrastructure.security.oauth2;

import com.maisprati.hub.domain.enums.UserType;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		String provider = userRequest.getClientRegistration().getRegistrationId();
		
		String email = (String) switch (provider) {
			case "google", "github" -> oAuth2User.getAttributes().get("email");
			default -> null;
		};
		
		String name = oAuth2User.getAttribute("name");
		
		// Cria ou atualiza usuário no MongoDB
		User user = userRepository.findByEmail(email).orElseGet(() ->
			                                                        User.builder()
				                                                        .email(email)
				                                                        .name(name != null ? name : email)
				                                                        .type(UserType.STUDENT)
				                                                        .createdAt(LocalDateTime.now())
				                                                        .updatedAt(LocalDateTime.now())
				                                                        .build()
		);
		user.setUpdatedAt(LocalDateTime.now());
		userRepository.save(user);
		
		return oAuth2User;
	}
}
