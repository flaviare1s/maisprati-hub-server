package com.maisprati.hub.service;

import com.maisprati.hub.model.Team;
import com.maisprati.hub.model.TeamMember;
import com.maisprati.hub.model.User;
import com.maisprati.hub.model.enums.TeamMemberRole;
import com.maisprati.hub.model.enums.UserType;
import com.maisprati.hub.repository.TeamRepository;
import com.maisprati.hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private static final int DEFAULT_MAX_MEMBERS = 10;

    /**
     * Buscar todos os times
     */
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    /**
     * Buscar todos os times ativos
     */
    public List<Team> getActiveTeams() {
        return teamRepository.findByIsActiveTrue();
    }

    /**
     * Buscar time pelo ID
     */
    public Optional<Team> getTeamById(String teamId) {
        return teamRepository.findById(teamId);
    }

    /**
     * Validar código de segurança do time
     */
    public Team validateTeamCode(String teamId, String securityCode) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Time não encontrado"));

        if (!team.getSecurityCode().equals(securityCode)) {
            throw new RuntimeException("Código de segurança inválido");
        }

        return team;
    }

    /**
     * Criar um novo time (apenas ADMIN)
     */
    public Team createTeam(Team teamData, String creatorUserId) {
        // Verificar se o usuário é admin
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!UserType.ADMIN.equals(creator.getType())) {
            throw new RuntimeException("Apenas administradores podem criar times");
        }

        // Gerar código de segurança único
        String securityCode = generateUniqueSecurityCode();

        // Criar o time
        Team newTeam = Team.builder()
                .name(teamData.getName())
                .description(teamData.getDescription())
                .securityCode(securityCode)
                .maxMembers(teamData.getMaxMembers() != null ? teamData.getMaxMembers() : DEFAULT_MAX_MEMBERS)
                .currentMembers(0)
                .members(List.of()) // Lista vazia inicialmente
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        Team savedTeam = teamRepository.save(newTeam);
        log.info("Time '{}' criado com sucesso! Código: {}", savedTeam.getName(), savedTeam.getSecurityCode());

        return savedTeam;
    }

    /**
     * Adicionar membro ao time
     */
    @Transactional
    public Team addMemberToTeam(String teamId, String userId, TeamMemberRole role, String subLeaderType) {
        // Buscar time
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Time não encontrado"));

        // Buscar usuário
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verificar se o time está cheio
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            throw new RuntimeException("Time já está cheio");
        }

        // Verificar se o usuário já é membro
        boolean isAlreadyMember = team.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(userId));

        if (isAlreadyMember) {
            throw new RuntimeException("Usuário já é membro deste time");
        }

        // Criar novo membro
        TeamMember newMember = TeamMember.builder()
                .userId(userId)
                .role(role != null ? role : TeamMemberRole.MEMBER)
                .subLeaderType(subLeaderType)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        // Adicionar membro ao time
        team.getMembers().add(newMember);
        team.setCurrentMembers(team.getMembers().size());

        // Salvar time
        Team updatedTeam = teamRepository.save(team);

        // Atualizar usuário (marcar como tendo grupo)
        user.setHasGroup(true);
        user.setIsFirstLogin(false);
        userRepository.save(user);

        log.info("Usuário '{}' adicionado ao time '{}'", user.getName(), team.getName());
        return updatedTeam;
    }

    /**
     * Atualizar role de um membro
     */
    @Transactional
    public Team updateMemberRole(String teamId, String userId, TeamMemberRole newRole, String subLeaderType) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Time não encontrado"));

        // Encontrar o membro
        TeamMember member = team.getMembers().stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Membro não encontrado no time"));

        // Atualizar role
        member.setRole(newRole);
        member.setSubLeaderType(TeamMemberRole.SUBLEADER.equals(newRole) ? subLeaderType : null);

        Team updatedTeam = teamRepository.save(team);
        log.info("Role do usuário '{}' atualizada para '{}' no time '{}'", userId, newRole, team.getName());

        return updatedTeam;
    }

    /**
     * Remover membro do time
     */
    @Transactional
    public Team removeMemberFromTeam(String teamId, String userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Time não encontrado"));

        // Remover membro
        team.getMembers().removeIf(member -> member.getUserId().equals(userId));
        team.setCurrentMembers(team.getMembers().size());

        // Salvar time
        Team updatedTeam = teamRepository.save(team);

        // Atualizar usuário (remover do grupo)
        userRepository.findById(userId).ifPresent(user -> {
            user.setHasGroup(false);
            userRepository.save(user);
        });

        log.info("Usuário '{}' removido do time '{}'", userId, team.getName());
        return updatedTeam;
    }

    /**
     * Alterar status ativo/inativo do time
     */
    public Team toggleTeamStatus(String teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Time não encontrado"));

        team.setIsActive(!team.getIsActive());
        Team updatedTeam = teamRepository.save(team);

        log.info("Status do time '{}' alterado para: {}", team.getName(), updatedTeam.getIsActive() ? "ATIVO" : "INATIVO");
        return updatedTeam;
    }

    /**
     * Verificar se usuário está em algum time ativo
     */
    public boolean isUserInActiveTeam(String userId) {
        List<Team> activeTeams = teamRepository.findByIsActiveTrue();

        return activeTeams.stream()
                .anyMatch(team -> team.getMembers().stream()
                        .anyMatch(member -> member.getUserId().equals(userId) && member.getIsActive()));
    }

    /**
     * Buscar time por código de segurança
     */
    public Optional<Team> getTeamBySecurityCode(String securityCode) {
        return teamRepository.findBySecurityCode(securityCode);
    }

    /**
     * Gerar código de segurança único
     */
    private String generateUniqueSecurityCode() {
        String securityCode;
        do {
            securityCode = "TEAM" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (teamRepository.existsBySecurityCode(securityCode));

        return securityCode;
    }
}
