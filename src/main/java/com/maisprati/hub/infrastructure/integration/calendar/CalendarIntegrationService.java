package com.maisprati.hub.infrastructure.integration.calendar;

import com.maisprati.hub.domain.model.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class CalendarIntegrationService {
	
	private final GoogleCalendarService googleCalendarService;
	private final GoogleTokenService googleTokenService; // serviço de tokens
	
	public void agendarEvento(String userId, Appointment appointment) {
		// Pega token válido do Google
		String accessToken = googleTokenService.getValidAccessToken(userId);
		
		ZonedDateTime start = appointment.getDate()
			                      .atTime(appointment.getTime())
			                      .atZone(ZoneId.systemDefault());
		ZonedDateTime end = start.plusHours(1);
		
		String summary = appointment.getTeamId() != null ?
			                 "Reunião do time " + appointment.getTeamId() :
			                 "Reunião individual com admin " + appointment.getAdminId();
		
		googleCalendarService.createEvent(accessToken, summary, start, end);
	}
}
