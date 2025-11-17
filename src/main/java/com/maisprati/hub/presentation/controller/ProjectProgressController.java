package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.application.service.ProjectProgressService;
import com.maisprati.hub.domain.enums.PhaseStatus;
import com.maisprati.hub.domain.model.ProjectPhase;
import com.maisprati.hub.domain.model.ProjectProgress;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Project Progress")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectProgressController {

    private final ProjectProgressService projectProgressService;

    @GetMapping("/progress/{teamId}")
    public List<ProjectProgress> getProgress(@PathVariable String teamId) {
        ProjectProgress progress = projectProgressService.getOrCreateProgress(teamId);
        return List.of(progress);
    }

    @PostMapping("/progress")
    public ProjectProgress createProgress(@RequestParam String teamId, @RequestBody Object phases) {
        return projectProgressService.createProgress(teamId);
    }

    @PatchMapping("/projectProgress/{progressId}")
    public ProjectProgress updateProgress(
            @PathVariable String progressId,
            @RequestBody UpdateProgressRequest request
    ) {
        return projectProgressService.updateProgressById(progressId, request);
    }

    public static class UpdateProgressRequest {
        private List<ProjectPhase> phases;
        private String lastUpdated;

        public List<ProjectPhase> getPhases() { return phases; }
        public void setPhases(List<ProjectPhase> phases) { this.phases = phases; }
        public String getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    @GetMapping("/projectProgress/{teamId}")
    public ProjectProgress getProgressSingle(@PathVariable String teamId) {
        return projectProgressService.getOrCreateProgress(teamId);
    }

    @PutMapping("/projectProgress/{teamId}/phase")
    public ProjectProgress updatePhase(
            @PathVariable String teamId,
            @RequestParam String title,
            @RequestParam PhaseStatus status
    ) {
        return projectProgressService.updatePhaseStatus(teamId, title, status);
    }
}
