package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.domain.model.TimeSlot;
import com.maisprati.hub.domain.model.TimeSlotDay;
import com.maisprati.hub.application.service.TimeSlotDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/timeslots")
@RequiredArgsConstructor
public class TimeSlotDayController {

    private final TimeSlotDayService timeSlotDayService;
    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Criar um dia com slots → admin
    @PostMapping("/days")
    public ResponseEntity<TimeSlotDay> createDay(
            @RequestParam String adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody List<TimeSlot> slots
    ) {
        TimeSlotDay day = timeSlotDayService.createDay(adminId, date, slots);
        return ResponseEntity.ok(day);
    }

    // Listar todos os slots de um dia → estudante/admin
    @GetMapping("/days/{date}")
    public ResponseEntity<TimeSlotDay> getDaySlots(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String adminId
    ) {
        TimeSlotDay day = timeSlotDayService.getDayByAdminAndDate(adminId, date);
        return ResponseEntity.ok(day);
    }

    // Marcar slot como reservado → estudante/admin
    @PatchMapping("/{date}/{time}/book")
    public ResponseEntity<TimeSlotDay> bookSlot(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String time,
            @RequestParam String adminId
    ) {
        LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
        TimeSlotDay day = timeSlotDayService.markSlotAsBooked(adminId, date, localTime);
        return ResponseEntity.ok(day);
    }

    // Liberar slot → admin
    @PatchMapping("/{date}/{time}/release")
    public ResponseEntity<Void> releaseSlot(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String time,
            @RequestParam String adminId
    ) {
        LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
        timeSlotDayService.releaseSlot(adminId, date, localTime);
        return ResponseEntity.ok().build();
    }
}
