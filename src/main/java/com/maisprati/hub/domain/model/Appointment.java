package com.maisprati.hub.domain.model;

import com.maisprati.hub.domain.enums.AppointmentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Document(collection = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    private String id;

    @NotBlank
    private String studentId; // solo ou membro do time

    private String teamId; // null se solo

    @NotBlank
    private String adminId;

    private boolean isSolo; // true se solo, false se em time

    private LocalDate date;
    private LocalTime time;
    private AppointmentStatus status;
    private String notes;
    private String meetingLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
