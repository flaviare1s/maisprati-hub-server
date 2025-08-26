package com.maisprati.hub.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "project_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectProgress {

    @Id
    private String id;

    @NotBlank
    private String teamId;

    private List<ProjectPhase> phases;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
}
