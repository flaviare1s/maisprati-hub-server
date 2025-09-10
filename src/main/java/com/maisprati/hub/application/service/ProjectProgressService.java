package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.enums.PhaseStatus;
import com.maisprati.hub.domain.model.ProjectProgress;
import com.maisprati.hub.domain.model.ProjectPhase;
import com.maisprati.hub.infrastructure.persistence.repository.ProjectProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectProgressService {

    private final ProjectProgressRepository projectProgressRepository;

    public ProjectProgress createOrUpdateProgress(String teamId, List<ProjectPhase> phases) {
        Optional<ProjectProgress> existing = projectProgressRepository.findByTeamId(teamId);

        ProjectProgress progress = existing.orElse(new ProjectProgress());
        progress.setTeamId(teamId);
        progress.setPhases(phases);

        return projectProgressRepository.save(progress);
    }

    public ProjectProgress getProgressByTeam(String teamId) {
        return projectProgressRepository.findByTeamId(teamId)
                .orElseThrow(() -> new RuntimeException("Progress not found for team: " + teamId));
    }

    public List<ProjectProgress> getAllProgressByTeam(String teamId) {
        return projectProgressRepository.findAllByTeamId(teamId);
    }

    public ProjectProgress updatePhaseStatus(String teamId, String title, PhaseStatus status) {
        ProjectProgress progress = getProgressByTeam(teamId);

        progress.getPhases().forEach(phase -> {
            if (phase.getTitle().equalsIgnoreCase(title)) {
                phase.setStatus(status);
            }
        });

        return projectProgressRepository.save(progress);
    }
}
