package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.domain.model.Team;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.domain.enums.TeamMemberRole;
import com.maisprati.hub.application.service.TeamService;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamService teamService;
    private final UserRepository userRepository;

    /**
     * GET /api/teams - Buscar todos os times
     */
    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    /**
     * GET /api/teams/active - Buscar times ativos
     */
    @GetMapping("/active")
    public ResponseEntity<List<Team>> getActiveTeams() {
        List<Team> activeTeams = teamService.getActiveTeams();
        return ResponseEntity.ok(activeTeams);
    }

    /**
     * GET /api/teams/{id} - Buscar time pelo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable String id) {
        Optional<Team> team = teamService.getTeamByIdWithUserData(id);
        return team.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/teams - Criar novo time (apenas ADMIN)
     */
    @PostMapping
    public ResponseEntity<Team> createTeam(
            @RequestBody Team teamData,
            @RequestParam String creatorUserId) {
        try {
            log.info("Recebendo requisição para criar time. Dados: {}, Criador: {}", teamData, creatorUserId);
            Team newTeam = teamService.createTeam(teamData, creatorUserId);
            return ResponseEntity.ok(newTeam);
        } catch (RuntimeException e) {
            log.error("Erro ao criar time: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/teams/{teamId}/validate - Validar código de segurança
     */
    @PostMapping("/{teamId}/validate")
    public ResponseEntity<?> validateTeamCode(
            @PathVariable String teamId,
            @RequestBody Map<String, String> request) {
        try {
            String securityCode = request.get("securityCode");
            Team team = teamService.validateTeamCode(teamId, securityCode);
            return ResponseEntity.ok(team);
        } catch (RuntimeException e) {
            log.error("Erro na validação: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/teams/{teamId}/members - Adicionar membro ao time
     */
    @PostMapping("/{teamId}/members")
    public ResponseEntity<Map<String, Object>> addMemberToTeam(
            @PathVariable String teamId,
            @RequestBody Map<String, Object> memberData) {
        try {
            log.info("Adicionando membro ao time. TeamId: {}, MemberData: {}", teamId, memberData);
            
            String userId = (String) memberData.get("userId");
            String roleStr = (String) memberData.getOrDefault("role", "MEMBER");
            String subLeaderType = (String) memberData.get("subLeaderType");

            log.info("Dados extraídos: userId={}, role={}, subLeaderType={}", userId, roleStr, subLeaderType);

            TeamMemberRole role = TeamMemberRole.valueOf(roleStr.toUpperCase());

            Team updatedTeam = teamService.addMemberToTeam(teamId, userId, role, subLeaderType);
            
            // Buscar dados atualizados do usuário
            User updatedUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            // Criar resposta com team e dados do usuário
            Map<String, Object> response = new HashMap<>();
            response.put("updatedTeam", updatedTeam);
            response.put("updatedUserData", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao adicionar membro: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/teams/{teamId}/members/{userId}/role - Atualizar role do membro
     */
    @PutMapping("/{teamId}/members/{userId}/role")
    public ResponseEntity<Team> updateMemberRole(
            @PathVariable String teamId,
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        try {
            String roleStr = request.get("role");
            String subLeaderType = request.get("subLeaderType");

            TeamMemberRole role = TeamMemberRole.valueOf(roleStr.toUpperCase());

            Team updatedTeam = teamService.updateMemberRole(teamId, userId, role, subLeaderType);
            
            // Retornar o team com dados completos dos usuários
            Optional<Team> teamWithUserData = teamService.getTeamByIdWithUserData(teamId);
            return teamWithUserData.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.ok(updatedTeam));
        } catch (Exception e) {
            log.error("Erro ao atualizar role: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<Team> updateTeam(
            @PathVariable String teamId,
            @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");

            Team updatedTeam = teamService.updateTeamBasicData(teamId, name, description);

            Optional<Team> teamWithUserData = teamService.getTeamByIdWithUserData(teamId);
            return teamWithUserData.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.ok(updatedTeam));
        } catch (Exception e) {
            log.error("Erro ao atualizar dados do time: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/teams/{teamId}/members/{userId} - Remover membro do time
     */
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Team> removeMemberFromTeam(
            @PathVariable String teamId,
            @PathVariable String userId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            String reason = null;
            if (requestBody != null) {
                reason = requestBody.get("reason");
            }
            
            Team updatedTeam = teamService.removeMemberFromTeam(teamId, userId, reason);
            return ResponseEntity.ok(updatedTeam);
        } catch (RuntimeException e) {
            log.error("Erro ao remover membro: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PATCH /api/teams/{teamId}/status - Alterar status do time
     */
    @PatchMapping("/{teamId}/status")
    public ResponseEntity<Team> toggleTeamStatus(@PathVariable String teamId) {
        try {
            Team updatedTeam = teamService.toggleTeamStatus(teamId);
            return ResponseEntity.ok(updatedTeam);
        } catch (RuntimeException e) {
            log.error("Erro ao alterar status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/teams/user/{userId}/status - Verificar se usuário está em time ativo
     */
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<Map<String, Boolean>> checkUserTeamStatus(@PathVariable String userId) {
        boolean isInActiveTeam = teamService.isUserInActiveTeam(userId);
        return ResponseEntity.ok(Map.of("isInActiveTeam", isInActiveTeam));
    }

    /**
     * GET /api/teams/code/{securityCode} - Buscar time por código de segurança
     */
    @GetMapping("/code/{securityCode}")
    public ResponseEntity<Team> getTeamByCode(@PathVariable String securityCode) {
        Optional<Team> team = teamService.getTeamBySecurityCode(securityCode);
        return team.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/roles - Retornar roles disponíveis (para o frontend)
     */
    @GetMapping("/roles")
    public ResponseEntity<TeamMemberRole[]> getRoles() {
        return ResponseEntity.ok(TeamMemberRole.values());
    }

    /**
     * GET /api/subLeaderTypes - Retornar tipos de subliderança
     */
    @GetMapping("/subLeaderTypes")
    public ResponseEntity<List<String>> getSubLeaderTypes() {
        List<String> types = List.of("Frontend", "Backend", "Design", "DevOps", "Mobile", "QA");
        return ResponseEntity.ok(types);
    }
}
