package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.exception.DayNotFoundException;
import com.maisprati.hub.domain.exception.SlotUnavailableException;
import com.maisprati.hub.domain.model.TimeSlot;
import com.maisprati.hub.domain.model.TimeSlotDay;
import com.maisprati.hub.infrastructure.persistence.repository.TimeSlotDayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotDayService {

    private final TimeSlotDayRepository timeSlotDayRepository;

    /**
     * Criar um novo dia com slots.
     */
    @Transactional
    public TimeSlotDay createOrUpdateDay(String adminId, LocalDate date, List<TimeSlot> slots) {
        TimeSlotDay day = timeSlotDayRepository.findByAdminIdAndDate(adminId, date)
                .orElseGet(() -> {
                    TimeSlotDay newDay = TimeSlotDay.builder()
                            .adminId(adminId)
                            .date(date)
                            .slots(slots)
                            .build();
                    return newDay;
                });

        List<TimeSlot> bookedSlots = day.getSlots().stream()
                .filter(TimeSlot::isBooked)
                .toList();

        day.getSlots().clear();
        day.getSlots().addAll(bookedSlots);
        
        for (TimeSlot newSlot : slots) {
            boolean isAlreadyBooked = bookedSlots.stream()
                    .anyMatch(s -> s.getTime().equals(newSlot.getTime()));
            if (!isAlreadyBooked) {
                day.getSlots().add(newSlot);
            }
        }

        return timeSlotDayRepository.save(day);
    }

    /**
     * Marcar slot como agendado (booked=true, available=false).
     */
    @Transactional
    public TimeSlotDay markSlotAsBooked(String adminId, LocalDate date, LocalTime time) {
        TimeSlotDay day = timeSlotDayRepository.findByAdminIdAndDate(adminId, date)
                .orElseThrow(() -> new DayNotFoundException("Dia não encontrado"));

        String timeString = time.toString().substring(0, 5);

        TimeSlot slot = day.getSlots().stream()
                .filter(s -> s.getTime().equals(timeString))
                .findFirst()
                .orElseThrow(() -> new SlotUnavailableException("Horário não encontrado"));

        if (!slot.isAvailable() || slot.isBooked()) {
            throw new SlotUnavailableException("Horário indisponível");
        }

        slot.setBooked(true);
        slot.setAvailable(false);

        timeSlotDayRepository.save(day);

        return day;
    }

    /**
     * Liberar um slot (booked=false, available=true).
     */
    @Transactional
    public void releaseSlot(String adminId, LocalDate date, LocalTime time) {
        TimeSlotDay day = timeSlotDayRepository.findByAdminIdAndDate(adminId, date)
                .orElseThrow(() -> new DayNotFoundException("Dia não encontrado"));

        String timeString = time.toString().substring(0, 5);

        day.getSlots().stream()
                .filter(s -> s.getTime().equals(timeString))
                .findFirst()
                .ifPresent(slot -> {
                    slot.setBooked(false);
                    slot.setAvailable(true);
                });

        timeSlotDayRepository.save(day);
    }

    @Transactional(readOnly = true)
    public TimeSlotDay getDayByAdminAndDate(String adminId, LocalDate date) {
        return timeSlotDayRepository.findByAdminIdAndDate(adminId, date)
                .orElseThrow(() -> new DayNotFoundException("Dia não encontrado para admin " + adminId + " na data " + date));
    }
}
