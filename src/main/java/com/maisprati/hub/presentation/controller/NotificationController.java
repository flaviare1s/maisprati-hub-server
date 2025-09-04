package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.domain.model.Notification;
import com.maisprati.hub.application.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications - Buscar notificações do usuário
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(@RequestParam String userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * POST /api/notifications - Criar nova notificação
     */
    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notificationData) {
        try {
            Notification notification = notificationService.createNotification(notificationData);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            log.error("Erro ao criar notificação: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/notifications/admin - Buscar notificações do admin (para testes)
     */
    @GetMapping("/admin")
    public ResponseEntity<List<Notification>> getAdminNotifications() {
        try {
            // Buscar ID do admin
            List<Notification> notifications = notificationService.getAdminNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Erro ao buscar notificações do admin: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/notifications/{id} - Deletar notificação
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao deletar notificação: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
