package com.maisprati.hub.application.controller;

import com.maisprati.hub.domain.model.Appointment;
import com.maisprati.hub.application.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // Aluno agenda
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(
            @RequestParam String studentId,
            @RequestParam String adminId,
            @RequestParam String teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time
    ) {
        Appointment appointment = appointmentService.createAppointment(studentId, adminId, teamId, date, time);
        return ResponseEntity.ok(appointment);
    }

    // Aluno vê seus agendamentos
    @GetMapping("/my")
    public ResponseEntity<List<Appointment>> getMyAppointments(@RequestParam String studentId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByStudent(studentId);
        return ResponseEntity.ok(appointments);
    }

    // Cancelar → aluno/admin
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancelAppointment(@PathVariable String id) {
        Appointment appointment = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(appointment);
    }

    // Concluir → admin
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Appointment> completeAppointment(@PathVariable String id) {
        Appointment appointment = appointmentService.completeAppointment(id);
        return ResponseEntity.ok(appointment);
    }
}
