package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.domain.model.Appointment;
import com.maisprati.hub.application.service.AppointmentService;
import com.maisprati.hub.infrastructure.integration.calendar.CalendarIntegrationService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {
	
	private final AppointmentService appointmentService;
	private final CalendarIntegrationService calendarIntegrationService;
	
	// Endpoint para buscar appointments por admin ou student
	@GetMapping
	public ResponseEntity<List<Appointment>> getAppointments(@RequestParam(required = false) String adminId,
	                                                         @RequestParam(required = false) String studentId,
	                                                         @RequestParam(required = false) String teamId) {
		if (adminId != null) {
			List<Appointment> appointments = appointmentService.getAppointmentsByAdmin(adminId);
			return ResponseEntity.ok(appointments);
		} else if (studentId != null) {
			List<Appointment> appointments = appointmentService.getAppointmentsByStudent(studentId);
			return ResponseEntity.ok(appointments);
		} else if (teamId != null) {
			List<Appointment> appointments = appointmentService.getAppointmentsByTeam(teamId);
			return ResponseEntity.ok(appointments);
		}
		return ResponseEntity.badRequest().build();
	}
	
	// Criar agendamento com @RequestBody para receber JSON
	@PostMapping
	public ResponseEntity<Appointment> createAppointment(
		@RequestBody CreateAppointmentRequest request,
		@AuthenticationPrincipal OAuth2User oauthUser // pega o usuário logado via OAuth2
	) {
		Appointment appointment = appointmentService.createAppointment(
			request.getStudentId(),
			request.getAdminId(),
			request.getTeamId(),
			request.getDate(),
			request.getTime()
		);
		
		// Tenta criar evento no Google Calendar
		try {
			calendarIntegrationService.agendarEvento(oauthUser, appointment);
		} catch (Exception e) {
			// Se falhar, não bloqueia a criação do agendamento
			log.warn("Falha ao criar evento no Google Calendar: {}", e.getMessage());
		}
		
		return ResponseEntity.ok(appointment);
	}
	
	@GetMapping("/my")
	public ResponseEntity<List<Appointment>> getMyAppointments(@RequestParam String studentId) {
		List<Appointment> appointments = appointmentService.getAppointmentsByStudent(studentId);
		return ResponseEntity.ok(appointments);
	}
	
	@PatchMapping("/{id}/cancel")
	public ResponseEntity<Appointment> cancelAppointment(@PathVariable String id) {
		Appointment appointment = appointmentService.cancelAppointment(id);
		return ResponseEntity.ok(appointment);
	}
	
	@PatchMapping("/{id}/complete")
	public ResponseEntity<Appointment> completeAppointment(@PathVariable String id) {
		Appointment appointment = appointmentService.completeAppointment(id);
		return ResponseEntity.ok(appointment);
	}
	
	// Classe interna para receber dados do appointment
	@Setter
	@Getter
	public static class CreateAppointmentRequest {
		// Getters e Setters
		private String studentId;
		private String adminId;
		private String teamId;
		private LocalDate date;
		private LocalTime time;
		
	}
}
