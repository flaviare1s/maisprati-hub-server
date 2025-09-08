package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.domain.model.Appointment;
import com.maisprati.hub.application.service.AppointmentService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // Endpoint para buscar appointments por admin ou student
    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments(
            @RequestParam(required = false) String adminId,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String teamId
    ) {
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
    public ResponseEntity<Appointment> createAppointment(@RequestBody CreateAppointmentRequest request) {
        Appointment appointment = appointmentService.createAppointment(
                request.getStudentId(),
                request.getAdminId(),
                request.getTeamId(),
                request.getDate(),
                request.getTime()
        );
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
    @Getter
    public static class CreateAppointmentRequest {
        // Getters e Setters
        private String studentId;
        private String adminId;
        private String teamId;
        private LocalDate date;
        private LocalTime time;

        public void setStudentId(String studentId) { this.studentId = studentId; }

        public void setAdminId(String adminId) { this.adminId = adminId; }

        public void setTeamId(String teamId) { this.teamId = teamId; }

        public void setDate(LocalDate date) { this.date = date; }

        public void setTime(LocalTime time) { this.time = time; }
    }
}
