package com.maisprati.hub.unit.presentation.controller;

import com.maisprati.hub.application.service.AppointmentService;
import com.maisprati.hub.domain.model.Appointment;
import com.maisprati.hub.presentation.controller.AppointmentController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentControllerTest {
	
	@Mock private AppointmentService appointmentService;
	@InjectMocks private AppointmentController appointmentController;
	
	private Appointment sampleAppointment;  // Exemplo de agendamento para os testes
	
	@BeforeEach
	void setUp() {
		// Inicializa mocks do Mockito
		MockitoAnnotations.openMocks(this);
		
		// Cria um agendamento de exemplo
		sampleAppointment = new Appointment();
		sampleAppointment.setId("1");
		sampleAppointment.setAdminId("admin1");
		sampleAppointment.setStudentId("student1");
		sampleAppointment.setTeamId("team1");
		sampleAppointment.setDate(LocalDate.now());
		sampleAppointment.setTime(LocalTime.now());
	}
	
	@Test
	void testGetAppointmentsByAdmin() {
		// Mocka service para retornar lista com o sampleAppointment para o admin
		when(appointmentService.getAppointmentsByAdmin("admin1"))
			.thenReturn(List.of(sampleAppointment));
		
		// Chama endpoint do controller
		ResponseEntity<List<Appointment>> response = appointmentController.getAppointments("admin1", null, null);
		
		// Valida status e quantidade de resultados
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
		
		// Verifica se service foi chamado corretamente
		verify(appointmentService, times(1)).getAppointmentsByAdmin("admin1");
	}
	
	@Test
	void testGetAppointmentsByStudent() {
		// Mocka service para retornar lista com o sampleAppointment para o student
		when(appointmentService.getAppointmentsByStudent("student1"))
			.thenReturn(List.of(sampleAppointment));
		
		ResponseEntity<List<Appointment>> response = appointmentController.getAppointments(null, "student1", null);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
		verify(appointmentService, times(1)).getAppointmentsByStudent("student1");
	}
	
	@Test
	void testGetAppointmentsByTeam() {
		// Mocka service para retornar lista com o sampleAppointment para o team
		when(appointmentService.getAppointmentsByTeam("team1"))
			.thenReturn(List.of(sampleAppointment));
		
		ResponseEntity<List<Appointment>> response = appointmentController.getAppointments(null, null, "team1");
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
		verify(appointmentService, times(1)).getAppointmentsByTeam("team1");
	}
	
	@Test
	void testGetAppointmentsBadRequest() {
		// Cenário em que nenhum parâmetro é fornecido → deve retornar 400
		ResponseEntity<List<Appointment>> response = appointmentController.getAppointments(null, null, null);
		
		assertEquals(400, response.getStatusCodeValue());
		
		// Service não deve ser chamado
		verifyNoInteractions(appointmentService);
	}
	
	@Test
	void testCreateAppointment() {
		// Cria request de agendamento
		AppointmentController.CreateAppointmentRequest request = new AppointmentController.CreateAppointmentRequest();
		request.setAdminId("admin1");
		request.setStudentId("student1");
		request.setTeamId("team1");
		request.setDate(LocalDate.now());
		request.setTime(LocalTime.now());
		
		// Mocka service para retornar sampleAppointment ao criar
		when(appointmentService.createAppointment(
			anyString(), anyString(), anyString(), any(), any()))
			.thenReturn(sampleAppointment);
		
		// Chama endpoint de criação
		ResponseEntity<Appointment> response = appointmentController.createAppointment(request);
		
		assertEquals(200, response.getStatusCodeValue());
		assertNotNull(response.getBody());
		
		// Verifica se service foi chamado corretamente com os parâmetros da request
		verify(appointmentService, times(1))
			.createAppointment(request.getStudentId(), request.getAdminId(),
				request.getTeamId(), request.getDate(), request.getTime());
	}
	
	@Test
	void testGetMyAppointments() {
		// Mocka service para retornar lista de agendamentos do estudante
		when(appointmentService.getAppointmentsByStudent("student1"))
			.thenReturn(List.of(sampleAppointment));
		
		ResponseEntity<List<Appointment>> response = appointmentController.getMyAppointments("student1");
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
		
		verify(appointmentService, times(1)).getAppointmentsByStudent("student1");
	}
	
	@Test
	void testCancelAppointment() {
		// Mocka service para cancelar agendamento
		when(appointmentService.cancelAppointment("1")).thenReturn(sampleAppointment);
		
		ResponseEntity<Appointment> response = appointmentController.cancelAppointment("1");
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(sampleAppointment, response.getBody());
		
		verify(appointmentService, times(1)).cancelAppointment("1");
	}
	
	@Test
	void testCompleteAppointment() {
		// Mocka service para completar agendamento
		when(appointmentService.completeAppointment("1")).thenReturn(sampleAppointment);
		
		ResponseEntity<Appointment> response = appointmentController.completeAppointment("1");
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(sampleAppointment, response.getBody());
		
		verify(appointmentService, times(1)).completeAppointment("1");
	}
}
