package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.Team;
import com.maisprati.hub.domain.model.TeamMember;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.domain.enums.TeamMemberRole;
import com.maisprati.hub.domain.enums.UserType;
import com.maisprati.hub.infrastructure.persistence.repository.TeamRepository;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
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
     * Buscar time pelo ID com dados completos dos usuários
     */
    public Optional<Team> getTeamByIdWithUserData(String teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isPresent()) {
            Team team = teamOpt.get();
            // Enriquecer os dados dos membros com informações dos usuários
            if (team.getMembers() != null) {
                team.getMembers().forEach(member -> {
                    Optional<User> userOpt = userRepository.findById(member.getUserId());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        // Criar um objeto simplificado para evitar exposição de dados sensíveis
                        User publicUser = User.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .type(user.getType())
                                .build();
                        member.setUser(publicUser);
                    }
                });
            }
        }
        return teamOpt;
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

        // Usar o código de segurança fornecido pelo frontend, ou gerar um novo se não fornecido
        String securityCode = teamData.getSecurityCode();
        if (securityCode == null || securityCode.trim().isEmpty()) {
            securityCode = generateUniqueSecurityCode();
        } else {
            // Verificar se o código fornecido já existe
            if (teamRepository.existsBySecurityCode(securityCode)) {
                throw new RuntimeException("Código de segurança já existe. Gere um novo código.");
            }
        }

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

        log.info("👤 Usuário: {} (hasGroup: {})", user.getName(), user.getHasGroup());

        // Verificar se o time está cheio
        if (team.getCurrentMembers() >= team.getMaxMembers()) {
            throw new RuntimeException("Time já está cheio");
        }

        List<Team> allTeams = teamRepository.findAll();

        for (Team t : allTeams) {
            boolean removed = t.getMembers().removeIf(member -> member.getUserId().equals(userId));
            if (removed) {
                t.setCurrentMembers(t.getMembers().size());
                teamRepository.save(t);
            }
        }

        // Verificar se ainda é membro do time de destino (após limpeza)
        team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Time não encontrado"));

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
        userRepository.save(user);

        // Notificar admin sobre entrada no time
        notificationService.notifyAdminTeamJoin(user.getName(), team.getName());

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
        return removeMemberFromTeam(teamId, userId, null);
    }

    /**
     * Remover membro do time com motivo para notificação
     */
    @Transactional
    public Team removeMemberFromTeam(String teamId, String userId, String reason) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Time não encontrado"));

        // Buscar dados do usuário antes de remover
        User user = userRepository.findById(userId)
                .orElse(null);

        // Remover membro
        team.getMembers().removeIf(member -> member.getUserId().equals(userId));
        team.setCurrentMembers(team.getMembers().size());

        // Salvar time
        Team updatedTeam = teamRepository.save(team);

        // Atualizar usuário (remover do grupo)
        if (user != null) {
            user.setHasGroup(false);
            userRepository.save(user);

            // Notificar admin sobre saída do time
            String finalReason = reason != null ? reason : "Motivo não informado";
            notificationService.notifyAdminTeamExit(user.getName(), team.getName(), finalReason);
        }

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

    public Team updateTeamBasicData(String teamId, String name, String description) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Time não encontrado"));

        if (name != null && !name.trim().isEmpty()) {
            team.setName(name.trim());
        }

        if (description != null) {
            team.setDescription(description.trim());
        }

        return teamRepository.save(team);
    }

    /**
     * Remove um usuário de todos os times ativos em que ele participa.
     * <p>
     * Este método é chamado automaticamente quando um usuário é inativado.
     *
     * @param userId ID do usuário a ser removido
     * @param reason Motivo da remoção (ex: "Usuário inativado")
     * @throws RuntimeException se houver erro ao remover o usuário de algum time
     */
    @Transactional
    public void removeUserFromAllActiveTeams(String userId, String reason) {
        // Busca todos os times ativos
        List<Team> activeTeams = getActiveTeams();

        // Remove o usuário de cada time em que ele esteja
        for (Team team : activeTeams) {
            boolean isMember = team.getMembers().stream()
                    .anyMatch(member -> member.getUserId().equals(userId));

            if (isMember) {
                try {
                    removeMemberFromTeam(team.getId(), userId, reason);
                    log.info("Usuário {} removido do time {} devido a: {}", userId, team.getName(), reason);
                } catch (Exception e) {
                    log.error("Erro ao remover usuário {} do time {}: {}", userId, team.getName(), e.getMessage());
                    // Continua removendo dos outros times mesmo se falhar em um
                }
            }
        }
    }
}
