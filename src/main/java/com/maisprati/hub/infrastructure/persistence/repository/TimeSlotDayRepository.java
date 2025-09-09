package com.maisprati.hub.infrastructure.persistence.repository;

import com.maisprati.hub.domain.model.TimeSlotDay;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface TimeSlotDayRepository extends MongoRepository<TimeSlotDay, String> {
    Optional<TimeSlotDay> findByDate(LocalDate date);
    Optional<TimeSlotDay> findByAdminIdAndDate(String adminId, LocalDate date);
    List<TimeSlotDay> findByAdminIdAndDateBetween(String adminId, LocalDate start, LocalDate end);

}
