package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.exception.DayNotFoundException;
import com.maisprati.hub.domain.exception.SlotUnavailableException;
import com.maisprati.hub.domain.model.TimeSlot;
import com.maisprati.hub.domain.model.TimeSlotDay;
import com.maisprati.hub.infrastructure.persistence.repository.TimeSlotDayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TimeSlotDayServiceTest {
	
	@Mock private TimeSlotDayRepository timeSlotDayRepository;
	@InjectMocks private TimeSlotDayService timeSlotDayService;
	
	private final LocalDate date = LocalDate.of(2025, 11, 1);
	private final String adminId = "admin1";
	
	@BeforeEach
	void setUp() {
		// Inicializa os mocks antes de cada teste
		MockitoAnnotations.openMocks(this);
	}
	
	// TEST 1 — Preservar slots já reservados ao atualizar o dia
	@Test
	void shouldPreserveBookedSlotsWhenUpdatingDay_fixed() {
		// Arrange
		TimeSlot booked = new TimeSlot("09:00", false, true); // já reservado
		TimeSlot newSlot = new TimeSlot("10:00", true, false); // novo slot disponível
		
		TimeSlotDay existingDay = TimeSlotDay.builder()
			                          .adminId(adminId)
			                          .date(date)
			                          .slots(new ArrayList<>(List.of(booked))) // lista mutável
			                          .build();
		
		when(timeSlotDayRepository.findByAdminIdAndDate(adminId, date))
			.thenReturn(Optional.of(existingDay));
		when(timeSlotDayRepository.save(any(TimeSlotDay.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		
		// Act
		TimeSlotDay result = timeSlotDayService.createOrUpdateDay(adminId, date, List.of(newSlot));
		
		// Assert
		assertNotNull(result);
		assertEquals(2, result.getSlots().size(), "Expected booked slot + new slot");
		assertTrue(result.getSlots().stream().anyMatch(TimeSlot::isBooked), "Expected at least one booked slot preserved");
		assertTrue(result.getSlots().stream().anyMatch(s -> "10:00".equals(s.getTime())), "Expected new slot 10:00 to be present");
		
		verify(timeSlotDayRepository, times(1)).save(any(TimeSlotDay.class));
	}
	
	// TEST 2 — Marcar slot como reservado com sucesso
	@Test
	void shouldMarkSlotAsBookedSuccessfully() {
		// Arrange
		TimeSlot available = new TimeSlot("10:00", true, false);
		TimeSlotDay day = TimeSlotDay.builder()
			                  .adminId(adminId)
			                  .date(date)
			                  .slots(List.of(available))
			                  .build();
		
		when(timeSlotDayRepository.findByAdminIdAndDate(adminId, date)).thenReturn(Optional.of(day));
		when(timeSlotDayRepository.save(any(TimeSlotDay.class))).thenAnswer(i -> i.getArgument(0));
		
		// Act
		TimeSlotDay result = timeSlotDayService.markSlotAsBooked(adminId, date, LocalTime.of(10, 0));
		
		// Assert
		TimeSlot slot = result.getSlots().get(0);
		assertTrue(slot.isBooked());
		assertFalse(slot.isAvailable());
		verify(timeSlotDayRepository, times(1)).save(result);
	}
	
	// TEST 3 — Marcar slot em dia inexistente lança exceção
	@Test
	void shouldThrowExceptionWhenDayNotFound() {
		// Arrange
		when(timeSlotDayRepository.findByAdminIdAndDate(adminId, date)).thenReturn(Optional.empty());
		
		// Act + Assert
		assertThrows(DayNotFoundException.class,
			() -> timeSlotDayService.markSlotAsBooked(adminId, date, LocalTime.of(9, 0)));
	}
	
	// TEST 4 — Marcar slot já reservado lança exceção
	@Test
	void shouldThrowExceptionWhenSlotUnavailable() {
		// Arrange
		TimeSlot booked = new TimeSlot("09:00", false, true);
		TimeSlotDay day = TimeSlotDay.builder()
			                  .adminId(adminId)
			                  .date(date)
			                  .slots(List.of(booked))
			                  .build();
		
		when(timeSlotDayRepository.findByAdminIdAndDate(adminId, date)).thenReturn(Optional.of(day));
		
		// Act + Assert
		assertThrows(SlotUnavailableException.class,
			() -> timeSlotDayService.markSlotAsBooked(adminId, date, LocalTime.of(9, 0)));
	}
	
	// TEST 5 — Liberar slot reservado com sucesso
	@Test
	void shouldReleaseBookedSlotSuccessfully() {
		// Arrange
		TimeSlot booked = new TimeSlot("09:00", false, true);
		TimeSlotDay day = TimeSlotDay.builder()
			                  .adminId(adminId)
			                  .date(date)
			                  .slots(List.of(booked))
			                  .build();
		
		when(timeSlotDayRepository.findByAdminIdAndDate(adminId, date)).thenReturn(Optional.of(day));
		
		// Act
		timeSlotDayService.releaseSlot(adminId, date, LocalTime.of(9, 0));
		
		// Assert
		TimeSlot slot = day.getSlots().get(0);
		assertFalse(slot.isBooked());
		assertTrue(slot.isAvailable());
		verify(timeSlotDayRepository, times(1)).save(day);
	}
	
	// TEST 6 — Obter dia existente
	@Test
	void shouldReturnDayWhenFound() {
		// Arrange
		TimeSlotDay day = TimeSlotDay.builder()
			                  .adminId(adminId)
			                  .date(date)
			                  .slots(List.of())
			                  .build();
		
		when(timeSlotDayRepository.findByAdminIdAndDate(adminId, date)).thenReturn(Optional.of(day));
		
		// Act
		TimeSlotDay result = timeSlotDayService.getDayByAdminAndDate(adminId, date);
		
		// Assert
		assertEquals(day, result);
	}
	
	// TEST 7 — Obter dia inexistente lança exceção
	@Test
	void shouldThrowExceptionWhenDayNotFoundInGetDayByAdminAndDate() {
		// Arrange
		when(timeSlotDayRepository.findByAdminIdAndDate(adminId, date)).thenReturn(Optional.empty());
		
		// Act + Assert
		assertThrows(DayNotFoundException.class,
			() -> timeSlotDayService.getDayByAdminAndDate(adminId, date));
	}
	
	// TEST 8 — Obter slots de um mês específico
	@Test
	void shouldReturnSlotsWithinMonthRange() {
		// Arrange
		LocalDate start = LocalDate.of(2025, 11, 1);
		LocalDate end = start.plusMonths(1);
		
		List<TimeSlotDay> expected = List.of(new TimeSlotDay());
		when(timeSlotDayRepository.findByAdminIdAndDateBetween(adminId, start, end))
			.thenReturn(expected);
		
		// Act
		List<TimeSlotDay> result = timeSlotDayService.getSlotsByAdminAndMonth(adminId, 2025, 11);
		
		// Assert
		assertEquals(expected, result);
		verify(timeSlotDayRepository).findByAdminIdAndDateBetween(adminId, start, end);
	}
}
