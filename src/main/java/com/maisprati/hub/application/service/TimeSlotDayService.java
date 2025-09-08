package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.TimeSlot;
import com.maisprati.hub.domain.model.TimeSlotDay;
import com.maisprati.hub.infrastructure.persistence.repository.TimeSlotDayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    public TimeSlotDay createDay(String adminId, LocalDate date, List<TimeSlot> slots) {
        if (timeSlotDayRepository.findByAdminIdAndDate(adminId, date).isPresent()) {
            throw new RuntimeException("Já existe um dia de horários para " + date);
        }

        TimeSlotDay day = TimeSlotDay.builder()
                .adminId(adminId)
                .date(date)
                .slots(slots)
                .build();

        return timeSlotDayRepository.save(day);
    }

    /**
     * Marcar slot como agendado (booked=true, available=false).
     */
    @Transactional
    public TimeSlotDay markSlotAsBooked(String adminId, LocalDate date, String time) {
        TimeSlotDay day = timeSlotDayRepository.findByAdminIdAndDate(adminId, date)
                .orElseThrow(() -> new RuntimeException("Dia não encontrado"));

        TimeSlot slot = day.getSlots().stream()
                .filter(s -> s.getTime().equals(time))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Horário não encontrado"));

        if (!slot.isAvailable() || slot.isBooked()) {
            throw new RuntimeException("Horário indisponível");
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
    public void releaseSlot(String adminId, LocalDate date, String time) {
        TimeSlotDay day = timeSlotDayRepository.findByAdminIdAndDate(adminId, date)
                .orElseThrow(() -> new RuntimeException("Dia não encontrado"));

        day.getSlots().stream()
                .filter(s -> s.getTime().equals(time))
                .findFirst()
                .ifPresent(slot -> {
                    slot.setBooked(false);
                    slot.setAvailable(true);
                });

        timeSlotDayRepository.save(day);
    }
}
