package com.maisprati.hub.unit.application.service;

import com.maisprati.hub.application.service.ProjectProgressService;
import com.maisprati.hub.domain.enums.PhaseStatus;
import com.maisprati.hub.domain.model.ProjectPhase;
import com.maisprati.hub.domain.model.ProjectProgress;
import com.maisprati.hub.infrastructure.persistence.repository.ProjectProgressRepository;
import com.maisprati.hub.presentation.controller.ProjectProgressController.UpdateProgressRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectProgressServiceTest {
	
	@Mock private ProjectProgressRepository projectProgressRepository;
	@InjectMocks private ProjectProgressService projectProgressService;
	
	private final String teamId = "team-123";
	
	@Test
	void shouldCreateNewProgressWhenNotExists() {
		// Arrange: configurar repository para não encontrar progresso existente
		when(projectProgressRepository.findByTeamId(teamId)).thenReturn(Optional.empty());
		when(projectProgressRepository.save(any(ProjectProgress.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		
		// Act: criar um novo progresso
		ProjectProgress progress = projectProgressService.createProgress(teamId);
		
		// Assert: validar o progresso criado
		assertNotNull(progress);
		assertEquals(teamId, progress.getTeamId());
		assertNotNull(progress.getPhases());
		assertEquals(7, progress.getPhases().size()); // 7 fases iniciais
		assertTrue(progress.getPhases().stream()
			           .allMatch(p -> p.getStatus() == PhaseStatus.TODO)); // todas devem estar em TODO
		verify(projectProgressRepository).save(any(ProjectProgress.class));
	}
	
	@Test
	void shouldReturnExistingProgressIfPresent() {
		// Arrange
		ProjectProgress existing = new ProjectProgress();
		existing.setTeamId(teamId);
		when(projectProgressRepository.findByTeamId(teamId))
			.thenReturn(Optional.of(existing));
		
		// Act
		ProjectProgress result = projectProgressService.createProgress(teamId);
		
		// Assert
		assertSame(existing, result); // deve retornar o mesmo objeto
		verify(projectProgressRepository, never()).save(any()); // não deve salvar nada novo
	}
	
	@Test
	void shouldUpdatePhaseStatusToDone() {
		// Arrange
		ProjectPhase phase = ProjectPhase.builder().title("Frontend").status(PhaseStatus.TODO).build();
		ProjectProgress progress = new ProjectProgress();
		progress.setTeamId(teamId);
		progress.setPhases(List.of(phase));
		
		when(projectProgressRepository.findByTeamId(teamId)).thenReturn(Optional.of(progress));
		when(projectProgressRepository.save(any(ProjectProgress.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		
		// Act
		ProjectProgress result = projectProgressService.updatePhaseStatus(teamId, "Frontend", PhaseStatus.DONE);
		
		// Assert
		assertEquals(PhaseStatus.DONE, result.getPhases().get(0).getStatus()); // status atualizado
		assertNotNull(result.getPhases().get(0).getCompletedAt()); // completedAt preenchido
		verify(projectProgressRepository).save(any(ProjectProgress.class));
	}
	
	@Test
	void shouldThrowExceptionWhenUpdateProgressNotFound() {
		// Arrange: repository não encontra progresso
		when(projectProgressRepository.findById("fake")).thenReturn(Optional.empty());
		
		// Act & Assert: deve lançar RuntimeException
		assertThrows(RuntimeException.class, () -> {
			projectProgressService.updateProgressById("fake", new UpdateProgressRequest());
		});
	}
}
