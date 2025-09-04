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
     * Criar uma nova notificação
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
     * Enviar notificação para o admin
     */
    public void sendNotificationToAdmin(String studentName, String message) {
        // Buscar o admin no banco
        Optional<User> adminOpt = userRepository.findByEmail("admin@admin.com");
        
        if (adminOpt.isEmpty()) {
            log.error("Admin não encontrado no sistema");
            return;
        }

        User admin = adminOpt.get();
        
        Notification notification = Notification.builder()
                .userId(admin.getId())
                .type("student_message")
                .title("Nova mensagem do aluno " + studentName)
                .message(studentName + ": " + message)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Notificação enviada para admin: {}", message);
    }

    /**
     * Enviar mensagem genérica para o admin (solicitações gerais)
     */
    public Notification sendMessageToAdmin(String studentName, String message) {
        Optional<User> adminOpt = userRepository.findByEmail("admin@admin.com");
        
        if (adminOpt.isEmpty()) {
            throw new RuntimeException("Admin não encontrado no sistema");
        }

        User admin = adminOpt.get();
        
        Notification notification = Notification.builder()
                .userId(admin.getId())
                .type("student_request")
                .title("Nova solicitação do aluno " + studentName)
                .message(studentName + ": " + message)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Solicitação enviada para admin de {}: {}", studentName, message);
        
        return savedNotification;
    }

    /**
     * Notificar admin sobre saída de membro do time
     */
    public void notifyAdminTeamExit(String studentName, String teamName, String reason) {
        Optional<User> adminOpt = userRepository.findByEmail("admin@admin.com");
        
        if (adminOpt.isEmpty()) {
            log.error("Admin não encontrado no sistema");
            return;
        }

        User admin = adminOpt.get();
        
        Notification notification = Notification.builder()
                .userId(admin.getId())
                .type("team_exit")
                .title("Membro saiu do time")
                .message(studentName + " saiu do time " + teamName + ". Motivo: " + reason)
                .data(Map.of(
                    "studentName", studentName,
                    "teamName", teamName,
                    "reason", reason
                ))
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Admin notificado sobre saída de {} do time {}", studentName, teamName);
    }

    /**
     * Notificar admin sobre entrada de membro no time
     */
    public void notifyAdminTeamJoin(String studentName, String teamName) {
        Optional<User> adminOpt = userRepository.findByEmail("admin@admin.com");
        
        if (adminOpt.isEmpty()) {
            log.error("Admin não encontrado no sistema");
            return;
        }

        User admin = adminOpt.get();
        
        Notification notification = Notification.builder()
                .userId(admin.getId())
                .type("team_join")
                .title("Novo membro no time")
                .message(studentName + " entrou no time " + teamName)
                .data(Map.of(
                    "studentName", studentName,
                    "teamName", teamName
                ))
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Admin notificado sobre entrada de {} no time {}", studentName, teamName);
    }
}
