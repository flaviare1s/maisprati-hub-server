package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.application.service.TimeSlotDayService;
import com.maisprati.hub.domain.model.TimeSlot;
import com.maisprati.hub.domain.model.TimeSlotDay;
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

class TimeSlotDayControllerTest {
	
	@Mock private TimeSlotDayService timeSlotDayService;
	@InjectMocks private TimeSlotDayController controller;
	
	private TimeSlotDay mockDay;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		
		// Cria um dia de slots fictício
		mockDay = TimeSlotDay.builder()
			          .adminId("admin123")
			          .date(LocalDate.of(2025, 11, 3))
			          .slots(List.of(
				          new TimeSlot("08:00", true, false),
				          new TimeSlot("09:00", false, true)
			          ))
			          .build();
	}
	
	/**
	 * Testa o endpoint POST /api/timeslots/days
	 * - Deve criar ou atualizar um dia com slots.
	 */
	@Test
	void testCreateDay() {
		when(timeSlotDayService.createOrUpdateDay(anyString(), any(LocalDate.class), anyList()))
			.thenReturn(mockDay);
		
		List<TimeSlot> slots = List.of(new TimeSlot("10:00", true, false));
		ResponseEntity<TimeSlotDay> response = controller.createDay("admin123", LocalDate.now(), slots);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("admin123", response.getBody().getAdminId());
		verify(timeSlotDayService, times(1))
			.createOrUpdateDay(eq("admin123"), any(LocalDate.class), eq(slots));
	}
	
	/**
	 * Testa o endpoint GET /api/timeslots/days/{date}
	 * - Deve retornar os slots de um dia.
	 */
	@Test
	void testGetDaySlots_Success() {
		when(timeSlotDayService.getDayByAdminAndDate(anyString(), any(LocalDate.class)))
			.thenReturn(mockDay);
		
		ResponseEntity<?> response = controller.getDaySlots(LocalDate.now(), "admin123");
		
		assertEquals(200, response.getStatusCodeValue());
		assertTrue(((List<?>) ((java.util.Map<?, ?>) response.getBody()).get("slots")).size() > 0);
		verify(timeSlotDayService, times(1))
			.getDayByAdminAndDate(eq("admin123"), any(LocalDate.class));
	}
	
	/**
	 * Testa o endpoint GET /api/timeslots/days/{date}
	 * - Deve retornar uma lista vazia quando ocorre exceção.
	 */
	@Test
	void testGetDaySlots_Exception() {
		when(timeSlotDayService.getDayByAdminAndDate(anyString(), any(LocalDate.class)))
			.thenThrow(new RuntimeException("Erro inesperado"));
		
		ResponseEntity<?> response = controller.getDaySlots(LocalDate.now(), "admin123");
		
		assertEquals(200, response.getStatusCodeValue());
		List<?> slots = (List<?>) ((java.util.Map<?, ?>) response.getBody()).get("slots");
		assertTrue(slots.isEmpty());
	}
	
	/**
	 * Testa o endpoint PATCH /api/timeslots/{date}/{time}/book
	 * - Deve marcar um horário como reservado.
	 */
	@Test
	void testBookSlot() {
		when(timeSlotDayService.markSlotAsBooked(anyString(), any(LocalDate.class), any(LocalTime.class)))
			.thenReturn(mockDay);
		
		ResponseEntity<TimeSlotDay> response =
			controller.bookSlot(LocalDate.of(2025, 11, 3), "08:00", "admin123");
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("admin123", response.getBody().getAdminId());
		verify(timeSlotDayService, times(1))
			.markSlotAsBooked(eq("admin123"), any(LocalDate.class), any(LocalTime.class));
	}
	
	/**
	 * Testa o endpoint PATCH /api/timeslots/{date}/{time}/release
	 * - Deve liberar um horário reservado.
	 */
	@Test
	void testReleaseSlot() {
		doNothing().when(timeSlotDayService)
			.releaseSlot(anyString(), any(LocalDate.class), any(LocalTime.class));
		
		ResponseEntity<Void> response =
			controller.releaseSlot(LocalDate.of(2025, 11, 3), "09:00", "admin123");
		
		assertEquals(200, response.getStatusCodeValue());
		verify(timeSlotDayService, times(1))
			.releaseSlot(eq("admin123"), any(LocalDate.class), any(LocalTime.class));
	}
	
	/**
	 * Testa o endpoint GET /api/timeslots/month
	 * - Deve retornar os slots de um mês.
	 */
	@Test
	void testGetMonthSlots() {
		when(timeSlotDayService.getSlotsByAdminAndMonth(anyString(), anyInt(), anyInt()))
			.thenReturn(List.of(mockDay));
		
		ResponseEntity<List<TimeSlotDay>> response = controller.getMonthSlots("admin123", 2025, 11);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
		verify(timeSlotDayService, times(1))
			.getSlotsByAdminAndMonth(eq("admin123"), eq(2025), eq(11));
	}
}
