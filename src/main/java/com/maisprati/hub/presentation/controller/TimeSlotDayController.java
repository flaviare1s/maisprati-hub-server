package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.domain.model.TimeSlot;
import com.maisprati.hub.domain.model.TimeSlotDay;
import com.maisprati.hub.application.service.TimeSlotDayService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Collections;

@Tag(name = "Time Slots")
@RestController
@RequestMapping("/api/timeslots")
@RequiredArgsConstructor
public class TimeSlotDayController {

    private final TimeSlotDayService timeSlotDayService;
    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @PostMapping("/days")
    public ResponseEntity<TimeSlotDay> createDay(
            @RequestParam String adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody List<TimeSlot> slots
    ) {
        TimeSlotDay day = timeSlotDayService.createOrUpdateDay(adminId, date, slots);
        return ResponseEntity.ok(day);
    }

    @GetMapping("/days/{date}")
    public ResponseEntity<?> getDaySlots(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String adminId
    ) {
        try {
            TimeSlotDay day = timeSlotDayService.getDayByAdminAndDate(adminId, date);

            return ResponseEntity.ok(Collections.singletonMap("slots", day.getSlots()));
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.singletonMap("slots", Collections.emptyList()));
        }
    }

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

    @GetMapping("/month")
    public ResponseEntity<List<TimeSlotDay>> getMonthSlots(
            @RequestParam String adminId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<TimeSlotDay> slots = timeSlotDayService.getSlotsByAdminAndMonth(adminId, year, month);
        return ResponseEntity.ok(slots);
    }
}
