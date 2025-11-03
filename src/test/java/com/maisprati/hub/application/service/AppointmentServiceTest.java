package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.exception.AppointmentNotFoundException;
import com.maisprati.hub.domain.model.Appointment;
import com.maisprati.hub.domain.enums.AppointmentStatus;
import com.maisprati.hub.infrastructure.persistence.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
	
	@Mock	private AppointmentRepository appointmentRepository;
	@Mock private TimeSlotDayService timeSlotDayService;
	@Mock private NotificationService notificationService;
	@InjectMocks private AppointmentService appointmentService;
	
	@Test
	void shouldCreateAppointmentSuccessfully() {
		// Arrange: criar os dados de teste e configurar os mocks
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.of(10, 0);
		Appointment mockAppointment = Appointment.builder()
			                              .id("1").studentId("stu1").adminId("adm1").teamId("team1")
			                              .date(date).time(time)
			                              .status(AppointmentStatus.SCHEDULED).build();
		
		// Quando salvar um Appointment no repository, retornar o mock
		when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);
		
		// Act: chamar o método testado
		Appointment result = appointmentService.createAppointment(
			"stu1", "adm1", "team1", date, time);
		
		// Assert: validar resultados e interações
		assertNotNull(result);
		assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
		verify(timeSlotDayService).markSlotAsBooked("adm1", date, time); // slot marcado como reservado
		verify(notificationService).createNotificationForAppointment(any(), eq("SCHEDULED")); // notificação enviada
	}
	
	@Test
	void shouldCancelAppointment() {
		// Arrange
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.of(10, 0);
		Appointment appointment = Appointment.builder()
			                          .id("123").adminId("adm1").date(date).time(time)
			                          .status(AppointmentStatus.SCHEDULED).build();
		
		when(appointmentRepository.findById("123")).thenReturn(Optional.of(appointment));
		when(appointmentRepository.save(any(Appointment.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		
		// Act
		Appointment result = appointmentService.cancelAppointment("123");
		
		// Assert
		assertEquals(AppointmentStatus.CANCELLED, result.getStatus()); // status atualizado
		verify(timeSlotDayService).releaseSlot("adm1", date, time); // slot liberado
		verify(notificationService).createNotificationForAppointment(any(), eq("CANCELLED")); // notificação enviada
	}
	
	@Test
	void shouldThrowWhenCancelAppointmentNotFound() {
		// Arrange: repository retorna vazio
		when(appointmentRepository.findById("404")).thenReturn(Optional.empty());
		
		// Act & Assert: deve lançar exceção
		assertThrows(AppointmentNotFoundException.class, () ->
			                                                 appointmentService.cancelAppointment("404"));
	}
	
	@Test
	void shouldCompleteAppointment() {
		// Arrange
		Appointment appointment = Appointment.builder()
			                          .id("1").status(AppointmentStatus.SCHEDULED).build();
		
		when(appointmentRepository.findById("1")).thenReturn(Optional.of(appointment));
		when(appointmentRepository.save(any(Appointment.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		
		// Act
		Appointment result = appointmentService.completeAppointment("1");
		
		// Assert
		assertEquals(AppointmentStatus.COMPLETED, result.getStatus()); // status atualizado
		verify(notificationService).createNotificationForAppointment(any(), eq("COMPLETED")); // notificação enviada
	}
}
