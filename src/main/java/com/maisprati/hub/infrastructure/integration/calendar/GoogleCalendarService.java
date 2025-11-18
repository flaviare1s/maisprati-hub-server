package com.maisprati.hub.infrastructure.integration.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {
	
	private final WebClient.Builder webClientBuilder;
	
	@Value("${GOOGLE_CLIENT_ID}")
	private String clientId;
	
	@Value("${GOOGLE_CLIENT_SECRET}")
	private String clientSecret;
	
	private static final String GOOGLE_CALENDAR_API = "https://www.googleapis.com/calendar/v3";
	
	private WebClient getClient(String accessToken) {
		return webClientBuilder
			       .baseUrl(GOOGLE_CALENDAR_API)
			       .defaultHeader("Authorization", "Bearer " + accessToken)
			       .build();
	}
	
	public void createEvent(String accessToken, String summary, ZonedDateTime start, ZonedDateTime end) {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		
		Map<String, Object> event = Map.of(
			"summary", summary,
			"start", Map.of(
				"dateTime", start.format(formatter),
				"timeZone", start.getZone().getId()
			),
			"end", Map.of(
				"dateTime", end.format(formatter),
				"timeZone", end.getZone().getId()
			)
		);
		
		try {
			String response = getClient(accessToken)
				                  .post()
				                  .uri("/calendars/primary/events")
				                  .bodyValue(event)
				                  .retrieve()
				                  .onStatus(HttpStatusCode::isError, clientResponse ->
					                                                     clientResponse.bodyToMono(String.class)
						                                                     .map(body -> new RuntimeException("Erro ao criar evento: " + body))
				                  )
				                  .bodyToMono(String.class)
				                  .block();
			
			log.info("Evento criado no Google Calendar: {}", response);
		} catch (Exception e) {
			log.error("Erro ao criar evento no Google Calendar: {}", e.getMessage(), e);
			throw new RuntimeException("Falha ao criar evento no Google Calendar", e);
		}
	}
	
	public String refreshAccessToken(String refreshToken) {
		// Parâmetros para o token endpoint do Google
		Map<String, String> params = Map.of(
			"client_id", clientId,
			"client_secret", clientSecret,
			"refresh_token", refreshToken,
			"grant_type", "refresh_token"
		);
		
		Map response = webClientBuilder.build()
			               .post()
			               .uri("https://oauth2.googleapis.com/token")
			               .bodyValue(params)
			               .retrieve()
			               .bodyToMono(Map.class)
			               .block();
		
		if (response == null || response.get("access_token") == null) {
			throw new RuntimeException("Não foi possível gerar access token do Google");
		}
		
		return (String) response.get("access_token");
	}
}
