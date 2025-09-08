package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.Appointment;
import com.maisprati.hub.domain.model.TimeSlotDay;
import com.maisprati.hub.domain.enums.AppointmentStatus;
import com.maisprati.hub.infrastructure.persistence.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotDayService timeSlotDayService;

    /**
     * Criar agendamento
     */
    @Transactional
    public Appointment createAppointment(
            String studentId,
            String adminId,
            String teamId,
            String date,
            String time
    ) {
        LocalDate localDate = LocalDate.parse(date);
        LocalTime localTime = LocalTime.parse(time);

        // Marcar slot como booked
        timeSlotDayService.markSlotAsBooked(adminId, localDate, time);

        // Criar agendamento
        Appointment appointment = Appointment.builder()
                .studentId(studentId)
                .adminId(adminId)
                .teamId(teamId)
                .date(localDate)
                .time(localTime)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Agendamento criado: aluno {} no time {} às {} ({})",
                studentId, teamId, time, date);

        return saved;
    }

    /**
     * Cancelar agendamento
     */
    @Transactional
    public Appointment cancelAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Atualizar status
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        // Liberar slot
        timeSlotDayService.releaseSlot(
                appointment.getAdminId(),
                appointment.getDate(),
                appointment.getTime().toString()
        );

        log.info("Agendamento {} cancelado", appointmentId);
        return appointment;
    }

    /**
     * Concluir agendamento
     */
    @Transactional
    public Appointment completeAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);

        log.info("Agendamento {} concluído", appointmentId);
        return saved;
    }
}
