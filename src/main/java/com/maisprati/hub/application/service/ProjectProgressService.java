package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.enums.PhaseStatus;
import com.maisprati.hub.domain.model.ProjectPhase;
import com.maisprati.hub.domain.model.ProjectProgress;
import com.maisprati.hub.infrastructure.persistence.repository.ProjectProgressRepository;
import com.maisprati.hub.presentation.controller.ProjectProgressController.UpdateProgressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectProgressService {

    private final ProjectProgressRepository projectProgressRepository;

    public ProjectProgress createProgress(String teamId) {
        // Verificar se já existe progresso para este time
        Optional<ProjectProgress> existing = projectProgressRepository.findByTeamId(teamId);
        if (existing.isPresent()) {
            return existing.get();
        }

        ProjectProgress progress = new ProjectProgress();
        progress.setTeamId(teamId);
        progress.setLastUpdated(LocalDateTime.now());

        List<ProjectPhase> initialPhases = List.of(
                ProjectPhase.builder().id(1).title("Frontend").status(PhaseStatus.TODO).build(),
                ProjectPhase.builder().id(2).title("Backend").status(PhaseStatus.TODO).build(),
                ProjectPhase.builder().id(3).title("Design").status(PhaseStatus.TODO).build(),
                ProjectPhase.builder().id(4).title("Banco de Dados").status(PhaseStatus.TODO).build(),
                ProjectPhase.builder().id(5).title("Testes").status(PhaseStatus.TODO).build(),
                ProjectPhase.builder().id(6).title("Deploy").status(PhaseStatus.TODO).build(),
                ProjectPhase.builder().id(7).title("Documentação").status(PhaseStatus.TODO).build()
        );

        progress.setPhases(initialPhases);
        return projectProgressRepository.save(progress);
    }

    public ProjectProgress getOrCreateProgress(String teamId) {
        return projectProgressRepository.findByTeamId(teamId)
                .orElseGet(() -> createProgress(teamId));
    }

    public ProjectProgress updatePhaseStatus(String teamId, String title, PhaseStatus status) {
        ProjectProgress progress = getOrCreateProgress(teamId);

        progress.getPhases().forEach(phase -> {
            if (phase.getTitle().equalsIgnoreCase(title)) {
                phase.setStatus(status);

                if (status == PhaseStatus.IN_PROGRESS && phase.getStartedAt() == null) {
                    phase.setStartedAt(LocalDateTime.now());
                } else if (status == PhaseStatus.DONE && phase.getCompletedAt() == null) {
                    phase.setCompletedAt(LocalDateTime.now());
                } else if (status == PhaseStatus.TODO) {
                    phase.setStartedAt(null);
                    phase.setCompletedAt(null);
                }
            }
        });

        progress.setLastUpdated(LocalDateTime.now());
        return projectProgressRepository.save(progress);
    }

    public ProjectProgress updateProgressById(String progressId, UpdateProgressRequest request) {
        Optional<ProjectProgress> progressOpt = projectProgressRepository.findById(progressId);

        if (progressOpt.isPresent()) {
            ProjectProgress progress = progressOpt.get();
            progress.setPhases(request.getPhases());
            progress.setLastUpdated(LocalDateTime.now());
            return projectProgressRepository.save(progress);
        }

        throw new RuntimeException("Progresso não encontrado com ID: " + progressId);
    }

    public List<ProjectProgress> getAllProgressByTeam(String teamId) {
        return projectProgressRepository.findAllByTeamId(teamId);
    }
}
