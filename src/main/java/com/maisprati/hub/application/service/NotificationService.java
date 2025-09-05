package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.Notification;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.NotificationRepository;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

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
}
