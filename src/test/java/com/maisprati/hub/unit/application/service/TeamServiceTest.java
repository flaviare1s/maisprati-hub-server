package com.maisprati.hub.unit.application.service;

import com.maisprati.hub.application.service.NotificationService;
import com.maisprati.hub.application.service.TeamService;
import com.maisprati.hub.domain.enums.TeamMemberRole;
import com.maisprati.hub.domain.enums.UserType;
import com.maisprati.hub.domain.model.Team;
import com.maisprati.hub.domain.model.TeamMember;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.TeamRepository;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {
	
	@Mock private TeamRepository teamRepository;
	@Mock private UserRepository userRepository;
	@Mock private NotificationService notificationService;
	@InjectMocks private TeamService teamService;
	
	private User adminUser;
	private User normalUser;
	private Team team;
	
	@BeforeEach
	void setUp() {
		// Arrange geral: cria usuário admin, usuário normal e time inicial
		adminUser = User.builder().id("admin1").name("Admin User").type(UserType.ADMIN).build();
		normalUser = User.builder().id("user1").name("Student One").type(UserType.STUDENT).build();
		
		team = Team.builder()
			       .id("team1").name("Dev Team").maxMembers(5).currentMembers(0)
			       .members(new ArrayList<>()).isActive(true)
			       .build();
	}
	
	// TEST 1 — Criar time com sucesso (usuário admin)
	@Test
	void shouldCreateTeamSuccessfully_WhenUserIsAdmin() {
		// Arrange: mockar usuário admin e salvar do time
		Team inputTeam = Team.builder()
			                 .name("Test Team")
			                 .description("New project team")
			                 .build();
		
		when(userRepository.findById("admin1")).thenReturn(Optional.of(adminUser));
		when(teamRepository.existsBySecurityCode(anyString())).thenReturn(false);
		when(teamRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		// Act: criar time
		Team result = teamService.createTeam(inputTeam, "admin1");
		
		// Assert: verificar se o time foi criado corretamente
		assertNotNull(result);
		assertTrue(result.getIsActive());
		assertNotNull(result.getSecurityCode());
		verify(teamRepository).save(any(Team.class));
	}
	
	// TEST 2 — Criar time com usuário não admin (lança exceção)
	@Test
	void shouldThrowException_WhenUserIsNotAdmin() {
		// Arrange: usuário normal
		when(userRepository.findById("user1")).thenReturn(Optional.of(normalUser));
		Team teamData = Team.builder().name("Team X").build();
		
		// Act & Assert: exceção esperada
		assertThrows(RuntimeException.class,
			() -> teamService.createTeam(teamData, "user1"),
			"Only admins can create teams");
	}
	
	// TEST 3 — Adicionar membro ao time com sucesso
	@Test
	void shouldAddMemberToTeam_WhenUserAndTeamExist() {
		// Arrange
		when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
		when(userRepository.findById("user1")).thenReturn(Optional.of(normalUser));
		when(teamRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		// Act
		Team result = teamService.addMemberToTeam("team1", "user1", TeamMemberRole.MEMBER, null);
		
		// Assert
		assertEquals(1, result.getMembers().size());
		assertTrue(result.getMembers().get(0).getIsActive());
		verify(notificationService).notifyAdminTeamJoin("Student One", "Dev Team");
	}
	
	// TEST 4 — Adicionar membro duplicado (lança exceção)
	@Test
	void shouldThrowException_WhenAddingDuplicateMember() {
		// Arrange: já existe um membro
		TeamMember existingMember = TeamMember.builder().userId("user1").isActive(true).build();
		team.getMembers().add(existingMember);
		team.setCurrentMembers(1);
		
		when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
		when(userRepository.findById("user1")).thenReturn(Optional.of(normalUser));
		
		// Act & Assert
		assertThrows(RuntimeException.class,
			() -> teamService.addMemberToTeam("team1", "user1", TeamMemberRole.MEMBER, null),
			"User is already a member of this team");
	}
	
	// TEST 5 — Alternar status do time (ativo/inativo)
	@Test
	void shouldToggleTeamStatus() {
		// Arrange
		when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
		when(teamRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		// Act
		Team result = teamService.toggleTeamStatus("team1");
		
		// Assert
		assertFalse(result.getIsActive()); // status inativo
		verify(teamRepository).save(any(Team.class));
	}
	
	// TESTES ADICIONAIS
	@Nested
	class TeamServiceAdditionalTest {
		
		// TEST 6 — Remover membro com motivo
		@Test
		void shouldRemoveMemberWithReason() {
			// Arrange
			TeamMember member = TeamMember.builder().userId("user1").isActive(true).build();
			team.getMembers().add(member);
			team.setCurrentMembers(1);
			
			when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
			when(teamRepository.save(any())).thenAnswer(i -> i.getArgument(0));
			when(userRepository.findById("user1")).thenReturn(Optional.of(normalUser));
			when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
			
			// Act
			Team result = teamService.removeMemberFromTeam("team1", "user1", "Personal reasons");
			
			// Assert
			assertEquals(0, result.getMembers().size());
			assertFalse(normalUser.getHasGroup());
			verify(notificationService).notifyAdminTeamExit("Student One", "Dev Team", "Personal reasons");
		}
		
		// TEST 7 — Remover membro sem motivo
		@Test
		void shouldRemoveMemberWithoutReason() {
			// Arrange
			TeamMember member = TeamMember.builder().userId("user1").isActive(true).build();
			team.getMembers().add(member);
			team.setCurrentMembers(1);
			
			when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
			when(teamRepository.save(any())).thenAnswer(i -> i.getArgument(0));
			when(userRepository.findById("user1")).thenReturn(Optional.of(normalUser));
			when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
			
			// Act
			Team result = teamService.removeMemberFromTeam("team1", "user1");
			
			// Assert
			assertEquals(0, result.getMembers().size());
			verify(notificationService).notifyAdminTeamExit("Student One", "Dev Team", "Motivo não informado");
		}
		
		// TEST 8 — Atualizar papel de membro
		@Test
		void shouldUpdateMemberRoleSuccessfully() {
			// Arrange
			TeamMember member = TeamMember.builder().userId("user1").role(TeamMemberRole.MEMBER).isActive(true).build();
			team.getMembers().add(member);
			
			when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
			when(teamRepository.save(any())).thenAnswer(i -> i.getArgument(0));
			
			// Act
			Team result = teamService.updateMemberRole("team1", "user1", TeamMemberRole.SUBLEADER, "Technical");
			
			// Assert
			TeamMember updatedMember = result.getMembers().get(0);
			assertEquals(TeamMemberRole.SUBLEADER, updatedMember.getRole());
			assertEquals("Technical", updatedMember.getSubLeaderType());
		}
		
		// TEST 9 — Obter time com dados públicos do usuário
		@Test
		void shouldReturnTeamWithPublicUserData() {
			// Arrange
			TeamMember member = TeamMember.builder().userId("user1").isActive(true).build();
			team.getMembers().add(member);
			
			when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
			when(userRepository.findById("user1")).thenReturn(Optional.of(normalUser));
			
			// Act
			Optional<Team> resultOpt = teamService.getTeamByIdWithUserData("team1");
			
			// Assert
			assertTrue(resultOpt.isPresent());
			TeamMember resultMember = resultOpt.get().getMembers().get(0);
			assertNotNull(resultMember.getUser());
			assertEquals("Student One", resultMember.getUser().getName());
			assertEquals("user1", resultMember.getUser().getId());
		}
		
		// TEST 10 — Atualizar nome e descrição do time
		@Test
		void shouldUpdateTeamNameAndDescription() {
			// Arrange
			when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
			when(teamRepository.save(any())).thenAnswer(i -> i.getArgument(0));
			
			// Act
			Team updated = teamService.updateTeamBasicData("team1", "New Team Name", "Updated description");
			
			// Assert
			assertEquals("New Team Name", updated.getName());
			assertEquals("Updated description", updated.getDescription());
		}
		
		// TEST 11 — Validar código de segurança do time
		@Test
		void shouldValidateTeamCodeSuccessfully() {
			// Arrange
			team.setSecurityCode("SEC123");
			when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
			
			// Act
			Team validated = teamService.validateTeamCode("team1", "SEC123");
			
			// Assert
			assertEquals("SEC123", validated.getSecurityCode());
		}
		
		@Test
		void shouldThrowExceptionForInvalidTeamCode() {
			// Arrange
			team.setSecurityCode("SEC123");
			when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
			
			// Act & Assert
			assertThrows(RuntimeException.class,
				() -> teamService.validateTeamCode("team1", "WRONGCODE"),
				"Código de segurança inválido");
		}
		
		@Test
		void shouldThrowExceptionWhenTeamNotFoundForValidate() {
			// Arrange
			when(teamRepository.findById("team1")).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThrows(RuntimeException.class,
				() -> teamService.validateTeamCode("team1", "ANYCODE"),
				"Time não encontrado");
		}
	}
}
