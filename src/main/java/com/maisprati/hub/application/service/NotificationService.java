package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.Appointment;
import com.maisprati.hub.domain.model.Notification;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.domain.model.Team;
import com.maisprati.hub.infrastructure.persistence.repository.NotificationRepository;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import com.maisprati.hub.infrastructure.persistence.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    /**
     * Buscar todas as notificações de um usuário
     */
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Buscar notificações do admin
     */
    public List<Notification> getAdminNotifications() {
        Optional<User> adminOpt = userRepository.findByEmail("admin@admin.com");
        if (adminOpt.isEmpty()) {
            log.error("Admin não encontrado no sistema");
            return List.of();
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(adminOpt.get().getId());
    }

    /**
     * Criar uma nova notificação genérica
     */
    public Notification createNotification(Notification notificationData) {
        Notification notification = Notification.builder()
                .userId(notificationData.getUserId())
                .type(notificationData.getType())
                .title(notificationData.getTitle())
                .message(notificationData.getMessage())
                .data(notificationData.getData())
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notificação criada para usuário: {}, tipo: {}", notification.getUserId(), notification.getType());

        return savedNotification;
    }

    /**
     * Deletar uma notificação
     */
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
        log.info("Notificação deletada: {}", notificationId);
    }

    /**
     * Enviar mensagem do aluno para o admin
     */
    public Notification sendNotificationToAdmin(String studentName, String message) {
        return saveAdminNotification(
                "student_message",
                "Nova mensagem do aluno " + studentName,
                studentName + ": " + message,
                null
        );
    }

    /**
     * Enviar solicitação genérica do aluno para o admin
     */
    public Notification sendMessageToAdmin(String studentName, String message) {
        return saveAdminNotification(
                "student_request",
                "Nova solicitação do aluno " + studentName,
                studentName + ": " + message,
                null
        );
    }

    /**
     * Notificar admin sobre entrada de membro no time
     */
    public Notification notifyAdminTeamJoin(String studentName, String teamName) {
        return saveAdminNotification(
                "team_join",
                "Novo membro no time",
                studentName + " entrou no time " + teamName,
                Map.of("studentName", studentName, "teamName", teamName)
        );
    }

    /**
     * Notificar admin sobre saída de membro do time
     */
    public Notification notifyAdminTeamExit(String studentName, String teamName, String reason) {
        return saveAdminNotification(
                "team_exit",
                "Membro saiu do time",
                studentName + " saiu do time " + teamName + ". Motivo: " + reason,
                Map.of("studentName", studentName, "teamName", teamName, "reason", reason)
        );
    }

    /**
     * Monta a notificação para o admin e salva no banco
     */
    private Notification saveAdminNotification(String type, String title, String message, Map<String, Object> data) {
        Notification notification = buildAdminNotification(type, title, message, data);
        if (notification != null) {
            notificationRepository.save(notification);
            log.info("Admin notificado: {} - {}", type, message);
        }
        return notification;
    }

    /**
     * Monta o objeto Notification para o admin (não salva)
     */
    private Notification buildAdminNotification(String type, String title, String message, Map<String, Object> data) {
        Optional<User> adminOpt = userRepository.findByEmail("admin@admin.com");
        if (adminOpt.isEmpty()) {
            log.error("Admin não encontrado no sistema");
            return null;
        }

        User admin = adminOpt.get();

        return Notification.builder()
                .userId(admin.getId())
                .type(type)
                .title(title)
                .message(message)
                .data(data)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Cria notificações para agendamentos (para todos os envolvidos)
     */
    public void createNotificationForAppointment(Appointment appointment, String eventType) {
        try {
            // Formatadores para data brasileira
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            String formattedDate = appointment.getDate().format(dateFormatter);
            String formattedTime = appointment.getTime().format(timeFormatter);

            // Buscar dados do time se existir
            String teamName = null;
            List<String> teamMemberIds = new ArrayList<>();

            if (appointment.getTeamId() != null) {
                Optional<Team> teamOpt = teamRepository.findById(appointment.getTeamId());
                if (teamOpt.isPresent()) {
                    Team team = teamOpt.get();
                    teamName = team.getName();
                    teamMemberIds = team.getMembers().stream()
                            .map(member -> member.getUserId())
                            .collect(Collectors.toList());
                }
            }

            // Se tem time, tratar como reunião do time
            if (appointment.getTeamId() != null && teamName != null) {
                handleTeamAppointmentNotifications(
                        appointment, eventType, formattedDate, formattedTime,
                        teamName, teamMemberIds
                );
            } else {
                // Reunião individual
                handleIndividualAppointmentNotifications(
                        appointment, eventType, formattedDate, formattedTime
                );
            }

        } catch (Exception e) {
            log.error("Erro ao criar notificações para appointment: {}", appointment.getId(), e);
        }
    }

    /**
     * Trata notificações para reuniões de time
     */
    private void handleTeamAppointmentNotifications(
            Appointment appointment, String eventType, String formattedDate, String formattedTime,
            String teamName, List<String> teamMemberIds) {

        String studentId = appointment.getStudentId();
        String adminId = appointment.getAdminId();

        switch (eventType) {
            case "SCHEDULED":
                // Notificar admin sobre reunião do time
                createNotification(Notification.builder()
                        .userId(adminId)
                        .type("team_appointment_scheduled")
                        .title("Nova reunião do time")
                        .message("O time " + teamName + " agendou uma reunião para " + formattedDate + " às " + formattedTime)
                        .data(Map.of("appointmentId", appointment.getId(), "teamName", teamName))
                        .createdAt(LocalDateTime.now())
                        .build());

                // Notificar outros membros do time (exceto quem agendou)
                teamMemberIds.stream()
                        .filter(memberId -> !memberId.equals(studentId))
                        .forEach(memberId -> createNotification(Notification.builder()
                                .userId(memberId)
                                .type("team_appointment_scheduled")
                                .title("Nova reunião do time")
                                .message("O time " + teamName + " agendou uma reunião para " + formattedDate + " às " + formattedTime)
                                .data(Map.of("appointmentId", appointment.getId(), "teamName", teamName))
                                .createdAt(LocalDateTime.now())
                                .build()));

                // Notificar quem agendou (mensagem específica)
                createNotification(Notification.builder()
                        .userId(studentId)
                        .type("appointment_scheduled")
                        .title("Reunião agendada")
                        .message("Você agendou uma reunião para o time " + teamName + " em " + formattedDate + " às " + formattedTime)
                        .data(Map.of("appointmentId", appointment.getId(), "teamName", teamName))
                        .createdAt(LocalDateTime.now())
                        .build());
                break;

            case "CANCELLED":
                // Mensagem padrão para todos
                String cancelMessage = "A reunião do time " + teamName + " marcada para " + formattedDate + " às " + formattedTime + " foi cancelada";

                // Notificar admin
                createNotification(Notification.builder()
                        .userId(adminId)
                        .type("team_appointment_cancelled")
                        .title("Reunião do time cancelada")
                        .message(cancelMessage)
                        .data(Map.of("appointmentId", appointment.getId(), "teamName", teamName))
                        .createdAt(LocalDateTime.now())
                        .build());

                // Notificar todos os membros do time
                teamMemberIds.forEach(memberId -> createNotification(Notification.builder()
                        .userId(memberId)
                        .type("team_appointment_cancelled")
                        .title("Reunião do time cancelada")
                        .message(cancelMessage)
                        .data(Map.of("appointmentId", appointment.getId(), "teamName", teamName))
                        .createdAt(LocalDateTime.now())
                        .build()));
                break;

            case "COMPLETED":
                // Notificar admin
                createNotification(Notification.builder()
                        .userId(adminId)
                        .type("team_appointment_completed")
                        .title("Reunião do time concluída")
                        .message("A reunião do time " + teamName + " do dia " + formattedDate + " às " + formattedTime + " foi concluída")
                        .data(Map.of("appointmentId", appointment.getId(), "teamName", teamName))
                        .createdAt(LocalDateTime.now())
                        .build());

                // Notificar todos os membros do time
                teamMemberIds.forEach(memberId -> createNotification(Notification.builder()
                        .userId(memberId)
                        .type("team_appointment_completed")
                        .title("Reunião do time concluída")
                        .message("A reunião do time " + teamName + " do dia " + formattedDate + " às " + formattedTime + " foi concluída")
                        .data(Map.of("appointmentId", appointment.getId(), "teamName", teamName))
                        .createdAt(LocalDateTime.now())
                        .build()));
                break;
        }
    }

    /**
     * Trata notificações para reuniões individuais
     */
    private void handleIndividualAppointmentNotifications(
            Appointment appointment, String eventType, String formattedDate, String formattedTime) {

        String studentId = appointment.getStudentId();
        String adminId = appointment.getAdminId();

        String studentName = "Um aluno";
        Optional<User> studentOpt = userRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            studentName = studentOpt.get().getName();
        }

        switch (eventType) {
            case "SCHEDULED":
                // Notificar admin
                createNotification(Notification.builder()
                        .userId(adminId)
                        .type("appointment_scheduled")
                        .title("Nova reunião agendada")
                        .message(studentName + " agendou uma reunião para " + formattedDate + " às " + formattedTime)
                        .data(Map.of("appointmentId", appointment.getId()))
                        .createdAt(LocalDateTime.now())
                        .build());

                // Notificar estudante
                createNotification(Notification.builder()
                        .userId(studentId)
                        .type("appointment_scheduled")
                        .title("Nova reunião marcada")
                        .message("Sua reunião foi marcada para " + formattedDate + " às " + formattedTime)
                        .data(Map.of("appointmentId", appointment.getId()))
                        .createdAt(LocalDateTime.now())
                        .build());
                break;

            case "CANCELLED":
                // Mensagem padrão para reunião individual
                String cancelMessage = "A reunião marcada para " + formattedDate + " às " + formattedTime + " foi cancelada";

                // Notificar admin
                createNotification(Notification.builder()
                        .userId(adminId)
                        .type("appointment_cancelled")
                        .title("Reunião cancelada")
                        .message(cancelMessage)
                        .data(Map.of("appointmentId", appointment.getId()))
                        .createdAt(LocalDateTime.now())
                        .build());

                // Notificar estudante
                createNotification(Notification.builder()
                        .userId(studentId)
                        .type("appointment_cancelled")
                        .title("Reunião cancelada")
                        .message(cancelMessage)
                        .data(Map.of("appointmentId", appointment.getId()))
                        .createdAt(LocalDateTime.now())
                        .build());
                break;

            case "COMPLETED":
                // Notificar admin
                createNotification(Notification.builder()
                        .userId(adminId)
                        .type("appointment_completed")
                        .title("Reunião concluída")
                        .message("A reunião do dia " + formattedDate + " às " + formattedTime + " foi concluída")
                        .data(Map.of("appointmentId", appointment.getId()))
                        .createdAt(LocalDateTime.now())
                        .build());

                // Notificar estudante
                createNotification(Notification.builder()
                        .userId(studentId)
                        .type("appointment_completed")
                        .title("Reunião concluída")
                        .message("Sua reunião do dia " + formattedDate + " às " + formattedTime + " foi concluída.")
                        .data(Map.of("appointmentId", appointment.getId()))
                        .createdAt(LocalDateTime.now())
                        .build());
                break;
        }
    }
}
