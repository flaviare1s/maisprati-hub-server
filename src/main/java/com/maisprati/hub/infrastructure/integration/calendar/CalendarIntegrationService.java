package com.maisprati.hub.infrastructure.integration.calendar;

import com.maisprati.hub.domain.model.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class CalendarIntegrationService {
	
	private final GoogleCalendarService googleCalendarService;
	private final OAuth2AuthorizedClientService authorizedClientService;
	
	public void agendarEvento(@AuthenticationPrincipal OAuth2User user, Appointment appointment) {
		if (user == null) throw new IllegalStateException("Usuário não autenticado.");
		
		OAuth2AuthorizedClient client =
			authorizedClientService.loadAuthorizedClient("google", user.getName());
		
		if (client == null || client.getAccessToken() == null)
			throw new IllegalStateException("Usuário não autenticado com Google.");
		
		String accessToken = client.getAccessToken().getTokenValue();
		
		ZonedDateTime start = appointment.getDate().atTime(appointment.getTime()).atZone(ZoneId.systemDefault());
		ZonedDateTime end = start.plusHours(1); // duração padrão de 1h
		
		String summary = appointment.getTeamId() != null ?
			                 "Reunião do time " + appointment.getTeamId() :
			                 "Reunião individual com admin " + appointment.getAdminId();
		
		googleCalendarService.createEvent(accessToken, summary, start, end);
	}
}
