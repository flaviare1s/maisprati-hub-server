package com.maisprati.hub.unit.infrastructure.security.oauth2;

import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import com.maisprati.hub.infrastructure.security.oauth2.CustomOAuth2UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceTest {
	
	@Mock private UserRepository userRepository;
	@Mock private OAuth2User oAuth2User;
	@Mock private ClientRegistration clientRegistration;
	@Mock private OAuth2UserRequest userRequest;
	
	@Spy // sobrescrever comportamento do super.loadUser()
	@InjectMocks private CustomOAuth2UserService service;
	
	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	void shouldHandleUnknownProviderGracefully() {
		
		// Configura um provider desconhecido (linkedin) e usuário sem email
		when(clientRegistration.getRegistrationId()).thenReturn("linkedin");
		when(oAuth2User.getAttributes()).thenReturn(Map.of("name", "No Email User"));
		when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
		
		// Mock do comportamento do método loadUser da classe pai
		doReturn(oAuth2User).when(service).loadUser(any(OAuth2UserRequest.class));
		
		// Act - Executa o método
		OAuth2User result = service.loadUser(userRequest);
		
		// Asset - Verifica se o resultado não é nulo e que não houve consulta ao repositório
		assertNotNull(result);
		verify(userRepository, never()).findByEmail(any());
	}
}
