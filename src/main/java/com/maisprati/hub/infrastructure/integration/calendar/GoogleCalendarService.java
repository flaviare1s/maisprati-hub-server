package com.maisprati.hub.infrastructure.integration.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZonedDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {
	
	private final WebClient.Builder webClientBuilder;
	private static final String GOOGLE_CALENDAR_API = "https://www.googleapis.com/calendar/v3";
	
	private WebClient getClient(String accessToken) {
		return webClientBuilder
			       .baseUrl(GOOGLE_CALENDAR_API)
			       .defaultHeader("Authorization", "Bearer " + accessToken)
			       .build();
	}
	
	public void createEvent(String accessToken, String summary, ZonedDateTime start, ZonedDateTime end) {
		Map<String, Object> event = Map.of(
			"summary", summary,
			"start", Map.of("dateTime", start.toString(), "timeZone", start.getZone().toString()),
			"end", Map.of("dateTime", end.toString(), "timeZone", end.getZone().toString())
		);
		
		try {
			String response = getClient(accessToken)
				                  .post()
				                  .uri("/calendars/primary/events")
				                  .bodyValue(event)
				                  .retrieve()
				                  .onStatus(HttpStatusCode::isError, clientResponse ->
					                                                     clientResponse.bodyToMono(String.class)
						                                                     .map(body -> new RuntimeException("Erro ao criar evento: " + body)))
				                  .bodyToMono(String.class)
				                  .block();
			
			log.info("Evento criado no Google Calendar: {}", response);
		} catch (Exception e) {
			log.error("Erro ao criar evento no Google Calendar: {}", e.getMessage(), e);
		}
	}
}
