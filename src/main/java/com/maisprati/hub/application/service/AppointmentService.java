package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.Appointment;
import com.maisprati.hub.domain.model.TimeSlotDay;
import com.maisprati.hub.domain.model.TimeSlot;
import com.maisprati.hub.domain.enums.AppointmentStatus;
import com.maisprati.hub.infrastructure.persistence.repository.AppointmentRepository;
import com.maisprati.hub.infrastructure.persistence.repository.TimeSlotDayRepository;
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
    private final TimeSlotDayRepository timeSlotDayRepository;

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
        // Buscar slots do dia
        LocalDate localDate = LocalDate.parse(date); // converter String para LocalDate
        Optional<TimeSlotDay> optDay = timeSlotDayRepository.findByDate(localDate);
        if (optDay.isEmpty()) {
            throw new RuntimeException("Dia não encontrado para a data " + date);
        }

        TimeSlotDay day = optDay.get();

        // Procurar slot específico
        TimeSlot slot = day.getSlots().stream()
                .filter(s -> s.getTime().equals(time))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Horário não encontrado"));

        // Validar disponibilidade
        if (!slot.isAvailable() || slot.isBooked()) {
            throw new RuntimeException("Horário indisponível");
        }

        // Marcar slot como agendado
        slot.setBooked(true);
        slot.setAvailable(false);
        timeSlotDayRepository.save(day);

        // Criar agendamento
        Appointment appointment = Appointment.builder()
                .studentId(studentId)
                .adminId(adminId)
                .teamId(teamId)
                .date(LocalDate.parse(date))
                .time(LocalTime.parse(time))
                .status(AppointmentStatus.SCHEDULED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Agendamento criado para aluno {} no time {} às {} ({})", studentId, teamId, time, date);

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
        Optional<TimeSlotDay> optDay = timeSlotDayRepository.findByAdminIdAndDate(
                appointment.getAdminId(), appointment.getDate());
        if (optDay.isPresent()) {
            TimeSlotDay day = optDay.get();
            day.getSlots().stream()
                    .filter(s -> s.getTime().equals(appointment.getTime()))
                    .findFirst()
                    .ifPresent(slot -> {
                        slot.setBooked(false);
                        slot.setAvailable(true);
                    });
            timeSlotDayRepository.save(day);
        }

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
