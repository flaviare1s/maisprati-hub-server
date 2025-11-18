package com.maisprati.hub.infrastructure.integration.calendar;

import com.maisprati.hub.domain.model.GoogleToken;
import com.maisprati.hub.infrastructure.persistence.repository.GoogleTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GoogleTokenService {
	
	private final GoogleTokenRepository tokenRepository;
	private final GoogleCalendarService calendarService;
	
	public String getValidAccessToken(String userId) {
		GoogleToken token = tokenRepository.findByUserId(userId)
			                    .orElseThrow(() -> new IllegalStateException("Token Google não encontrado para o usuário"));
		
		// Se o accessToken estiver expirado ou nulo, gera novo usando refreshToken
		if (token.getAccessToken() == null ||
			    (token.getAccessTokenExpiry() != null && token.getAccessTokenExpiry().isBefore(LocalDateTime.now()))) {
			// Aqui chamamos a API do Google para gerar novo access token
			String newAccessToken = calendarService.refreshAccessToken(token.getRefreshToken());
			token.setAccessToken(newAccessToken);
			
			// opcional: salvar a data de expiração, se você souber
			token.setAccessTokenExpiry(LocalDateTime.now().plusMinutes(55)); // exemplo
			tokenRepository.save(token);
			
			return newAccessToken;
		}
		
		return token.getAccessToken();
		
		// refresh automático ( ? ):
		// if (token.getAccessTokenExpiry() != null && token.getAccessTokenExpiry().isBefore(LocalDateTime.now())) {
		//     String newAccessToken = calendarService.refreshAccessToken(token.getRefreshToken());
		//     token.setAccessToken(newAccessToken);
		//     // salvar a data de expiração fornecida pelo Google
		//     tokenRepository.save(token);
		//     return newAccessToken;
		// }
		// return token.getAccessToken();
	}
	
	public void saveTokens(String userId, String accessToken, String refreshToken, LocalDateTime expiry) {
		GoogleToken token = tokenRepository.findByUserId(userId).orElse(new GoogleToken());
		token.setUserId(userId);
		token.setAccessToken(accessToken);
		token.setRefreshToken(refreshToken);
		token.setAccessTokenExpiry(expiry); // null se não tiver a data exata
		tokenRepository.save(token);
	}
}
