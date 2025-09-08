package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.exception.AppointmentNotFoundException;
import com.maisprati.hub.domain.model.Appointment;
import com.maisprati.hub.domain.enums.AppointmentStatus;
import com.maisprati.hub.infrastructure.persistence.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotDayService timeSlotDayService;

    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Criar agendamento
     */
    @Transactional
    public Appointment createAppointment(
            String studentId,
            String adminId,
            String teamId,
            LocalDate date,
            LocalTime time
    ) {
        // Marcar slot como reservado
        timeSlotDayService.markSlotAsBooked(adminId, date, time);

        // Criar agendamento
        Appointment appointment = Appointment.builder()
                .studentId(studentId)
                .adminId(adminId)
                .teamId(teamId)
                .date(date)
                .time(time)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Agendamento criado: aluno {} no time {} às {} ({})",
                studentId, teamId, time.format(TIME_FORMATTER), date);

        return saved;
    }

    /**
     * Cancelar agendamento
     */
    @Transactional
    public Appointment cancelAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        // Atualizar status
        appointment.setStatus(AppointmentStatus.CANCELLED);

        // Liberar slot via service
        timeSlotDayService.releaseSlot(appointment.getAdminId(), appointment.getDate(), appointment.getTime());

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Agendamento {} cancelado", appointmentId);
        return saved;
    }

    /**
     * Concluir agendamento
     */
    @Transactional
    public Appointment completeAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Agendamento {} concluído", appointmentId);

        return saved;
    }

    public List<Appointment> getAppointmentsByStudent(String studentId) {
        return appointmentRepository.findByStudentId(studentId);
    }
}
