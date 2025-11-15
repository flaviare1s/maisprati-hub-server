package com.maisprati.hub.unit.presentation.controller;

import com.maisprati.hub.application.service.TeamService;
import com.maisprati.hub.domain.enums.TeamMemberRole;
import com.maisprati.hub.domain.model.Team;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import com.maisprati.hub.presentation.controller.TeamController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamControllerTest {
	
	@Mock private TeamService teamService;
	@Mock private UserRepository userRepository;
	@InjectMocks private TeamController teamController;
	
	private Team team;
	private User user;
	
	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		
		user = new User();
		user.setId("user1");
		user.setEmail("user@test.com");
		
		team = new Team();
		team.setId("team1");
		team.setName("Team Test");
	}
	
	// ==================== GET /api/teams ====================
	@Test
	void getAllTeams_ShouldReturnList() {
		when(teamService.getAllTeams()).thenReturn(List.of(team));
		
		ResponseEntity<List<Team>> response = teamController.getAllTeams();
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
		assertEquals(team, response.getBody().get(0));
	}
	
	@Test
	void getActiveTeams_ShouldReturnList() {
		when(teamService.getActiveTeams()).thenReturn(List.of(team));
		
		ResponseEntity<List<Team>> response = teamController.getActiveTeams();
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
	}
	
	@Test
	void getTeamById_ShouldReturnTeam() {
		when(teamService.getTeamByIdWithUserData("team1")).thenReturn(Optional.of(team));
		
		ResponseEntity<Team> response = teamController.getTeamById("team1");
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(team, response.getBody());
	}
	
	@Test
	void getTeamById_ShouldReturnNotFound() {
		when(teamService.getTeamByIdWithUserData("team2")).thenReturn(Optional.empty());
		
		ResponseEntity<Team> response = teamController.getTeamById("team2");
		
		assertEquals(404, response.getStatusCodeValue());
		assertNull(response.getBody());
	}
	
	// ==================== POST /api/teams ====================
	@Test
	void createTeam_ShouldReturnCreatedTeam() {
		when(teamService.createTeam(team, "user1")).thenReturn(team);
		
		ResponseEntity<Team> response = teamController.createTeam(team, "user1");
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(team, response.getBody());
	}
	
	@Test
	void createTeam_ShouldReturnBadRequestOnError() {
		when(teamService.createTeam(team, "user1")).thenThrow(new RuntimeException("Erro"));
		
		ResponseEntity<Team> response = teamController.createTeam(team, "user1");
		
		assertEquals(400, response.getStatusCodeValue());
		assertNull(response.getBody());
	}
	
	// ==================== POST /api/teams/{teamId}/validate ====================
	@Test
	void validateTeamCode_ShouldReturnTeam() {
		Map<String, String> request = Map.of("securityCode", "1234");
		when(teamService.validateTeamCode("team1", "1234")).thenReturn(team);
		
		ResponseEntity<?> response = teamController.validateTeamCode("team1", request);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(team, response.getBody());
	}
	
	@Test
	void validateTeamCode_ShouldReturnBadRequestOnError() {
		Map<String, String> request = Map.of("securityCode", "wrong");
		when(teamService.validateTeamCode("team1", "wrong")).thenThrow(new RuntimeException("Erro"));
		
		ResponseEntity<?> response = teamController.validateTeamCode("team1", request);
		
		assertEquals(400, response.getStatusCodeValue());
	}
	
	// ==================== POST /api/teams/{teamId}/members ====================
	@Test
	void addMemberToTeam_ShouldReturnUpdatedTeamAndUser() {
		Map<String, Object> memberData = new HashMap<>();
		memberData.put("userId", "user1");
		memberData.put("role", "MEMBER");
		
		when(teamService.addMemberToTeam("team1", "user1", TeamMemberRole.MEMBER, null)).thenReturn(team);
		when(userRepository.findById("user1")).thenReturn(Optional.of(user));
		
		ResponseEntity<Map<String, Object>> response = teamController.addMemberToTeam("team1", memberData);
		
		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getBody().containsKey("updatedTeam"));
		assertTrue(response.getBody().containsKey("updatedUserData"));
	}
	
	@Test
	void addMemberToTeam_ShouldReturnBadRequestOnError() {
		Map<String, Object> memberData = Map.of("userId", "user1", "role", "MEMBER");
		
		when(teamService.addMemberToTeam(any(), any(), any(), any())).thenThrow(new RuntimeException("Erro"));
		
		ResponseEntity<Map<String, Object>> response = teamController.addMemberToTeam("team1", memberData);
		
		assertEquals(400, response.getStatusCodeValue());
	}
	
	// ==================== GET /api/teams/user/{userId}/status ====================
	@Test
	void checkUserTeamStatus_ShouldReturnStatus() {
		when(teamService.isUserInActiveTeam("user1")).thenReturn(true);
		
		ResponseEntity<Map<String, Boolean>> response = teamController.checkUserTeamStatus("user1");
		
		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getBody().get("isInActiveTeam"));
	}
	
	// ==================== GET /api/teams/roles ====================
	@Test
	void getRoles_ShouldReturnAllRoles() {
		ResponseEntity<TeamMemberRole[]> response = teamController.getRoles();
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(TeamMemberRole.values().length, response.getBody().length);
	}
	
	// ==================== GET /api/subLeaderTypes ====================
	@Test
	void getSubLeaderTypes_ShouldReturnList() {
		ResponseEntity<List<String>> response = teamController.getSubLeaderTypes();
		
		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getBody().contains("Frontend"));
		assertTrue(response.getBody().contains("Backend"));
	}
	
	@Test
	void updateMemberRole_ShouldReturnUpdatedTeam() {
		String teamId = "team1";
		String userId = "user1";
		Map<String, String> request = Map.of("role", "LEADER", "subLeaderType", "Backend");
		
		when(teamService.updateMemberRole(teamId, userId, TeamMemberRole.LEADER, "Backend"))
			.thenReturn(new Team());
		when(teamService.getTeamByIdWithUserData(teamId)).thenReturn(Optional.of(new Team()));
		
		ResponseEntity<Team> response = teamController.updateMemberRole(teamId, userId, request);
		
		assertEquals(200, response.getStatusCodeValue());
		assertNotNull(response.getBody());
	}
	
	@Test
	void updateTeam_ShouldReturnUpdatedTeam() {
		String teamId = "team1";
		Map<String, String> request = Map.of("name", "Novo Time", "description", "Descrição");
		
		Team updatedTeam = new Team();
		when(teamService.updateTeamBasicData(teamId, "Novo Time", "Descrição")).thenReturn(updatedTeam);
		when(teamService.getTeamByIdWithUserData(teamId)).thenReturn(Optional.of(updatedTeam));
		
		ResponseEntity<Team> response = teamController.updateTeam(teamId, request);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(updatedTeam, response.getBody());
	}
	
	@Test
	void removeMemberFromTeam_ShouldReturnUpdatedTeam() {
		String teamId = "team1";
		String userId = "user1";
		Map<String, String> request = Map.of("reason", "Motivo");
		
		Team updatedTeam = new Team();
		when(teamService.removeMemberFromTeam(teamId, userId, "Motivo")).thenReturn(updatedTeam);
		
		ResponseEntity<Team> response = teamController.removeMemberFromTeam(teamId, userId, request);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(updatedTeam, response.getBody());
	}
	
	@Test
	void toggleTeamStatus_ShouldReturnUpdatedTeam() {
		String teamId = "team1";
		Team updatedTeam = new Team();
		when(teamService.toggleTeamStatus(teamId)).thenReturn(updatedTeam);
		
		ResponseEntity<Team> response = teamController.toggleTeamStatus(teamId);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(updatedTeam, response.getBody());
	}
	
	@Test
	void getTeamByCode_ShouldReturnTeam() {
		String code = "ABC123";
		Team team = new Team();
		when(teamService.getTeamBySecurityCode(code)).thenReturn(Optional.of(team));
		
		ResponseEntity<Team> response = teamController.getTeamByCode(code);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(team, response.getBody());
	}
	
	@Test
	void getTeamByCode_ShouldReturnNotFoundIfAbsent() {
		String code = "ABC123";
		when(teamService.getTeamBySecurityCode(code)).thenReturn(Optional.empty());
		
		ResponseEntity<Team> response = teamController.getTeamByCode(code);
		
		assertEquals(404, response.getStatusCodeValue());
	}
	
}
