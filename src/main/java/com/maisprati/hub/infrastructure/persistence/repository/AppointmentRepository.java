package com.maisprati.hub.infrastructure.persistence.repository;

import com.maisprati.hub.domain.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByStudentId(String studentID);
    List<Appointment> findByTeamId(String teamId);
    List<Appointment> findByAdminId(String adminId);

    List<Appointment> findByStudentIdAndDate(String studentId, LocalDate date);
    List<Appointment> findByAdminIdAndDate(String adminId, LocalDate date);
}
