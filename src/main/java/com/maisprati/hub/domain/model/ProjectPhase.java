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
    private String assignedTo;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public ProjectPhase(String title, PhaseStatus status) {
        this.title = title;
        this.status = status;
    }

    public ProjectPhase(Integer id, String title, PhaseStatus status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }
}
