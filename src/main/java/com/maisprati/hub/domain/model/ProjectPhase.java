package com.maisprati.hub.domain.model;

import com.maisprati.hub.domain.enums.PhaseStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPhase {

    private Integer id;
    private String title;
    private PhaseStatus status;
    private String assignedTo; // userId
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
