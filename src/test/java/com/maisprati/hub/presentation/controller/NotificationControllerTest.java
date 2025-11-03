package com.maisprati.hub.presentation.controller;

import com.maisprati.hub.application.service.NotificationService;
import com.maisprati.hub.domain.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {
	
	@Mock private NotificationService notificationService;
	@InjectMocks private NotificationController notificationController;
	
	private Notification notification;
	
	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		
		notification = new Notification();
		notification.setId("notif1");
		notification.setUserId("user1");
		notification.setMessage("Mensagem de teste");
	}
	
	// ==================== GET /api/notifications ====================
	@Test
	void getUserNotifications_ShouldReturnList() {
		when(notificationService.getUserNotifications("user1")).thenReturn(List.of(notification));
		
		ResponseEntity<List<Notification>> response = notificationController.getUserNotifications("user1");
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
		assertEquals(notification, response.getBody().get(0));
	}
	
	// ==================== POST /api/notifications ====================
	@Test
	void createNotification_ShouldReturnNotification() {
		when(notificationService.createNotification(notification)).thenReturn(notification);
		
		ResponseEntity<Notification> response = notificationController.createNotification(notification);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(notification, response.getBody());
	}
	
	@Test
	void createNotification_ShouldReturnBadRequestOnError() {
		when(notificationService.createNotification(notification)).thenThrow(new RuntimeException("Erro"));
		
		ResponseEntity<Notification> response = notificationController.createNotification(notification);
		
		assertEquals(400, response.getStatusCodeValue());
		assertNull(response.getBody());
	}
	
	// ==================== POST /api/notifications/send-to-admin ====================
	@Test
	void sendMessageToAdmin_ShouldReturnNotification() {
		Map<String, String> request = Map.of("studentName", "João", "message", "Teste");
		when(notificationService.sendMessageToAdmin("João", "Teste")).thenReturn(notification);
		
		ResponseEntity<Notification> response = notificationController.sendMessageToAdmin(request);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(notification, response.getBody());
	}
	
	@Test
	void sendMessageToAdmin_ShouldReturnBadRequestIfMissingData() {
		Map<String, String> request = Map.of("studentName", "João"); // sem mensagem
		
		ResponseEntity<Notification> response = notificationController.sendMessageToAdmin(request);
		
		assertEquals(400, response.getStatusCodeValue());
		assertNull(response.getBody());
	}
	
	@Test
	void sendMessageToAdmin_ShouldReturnBadRequestOnException() {
		Map<String, String> request = Map.of("studentName", "João", "message", "Teste");
		when(notificationService.sendMessageToAdmin("João", "Teste")).thenThrow(new RuntimeException("Erro"));
		
		ResponseEntity<Notification> response = notificationController.sendMessageToAdmin(request);
		
		assertEquals(400, response.getStatusCodeValue());
		assertNull(response.getBody());
	}
	
	// ==================== GET /api/notifications/admin ====================
	@Test
	void getAdminNotifications_ShouldReturnList() {
		when(notificationService.getAdminNotifications()).thenReturn(List.of(notification));
		
		ResponseEntity<List<Notification>> response = notificationController.getAdminNotifications();
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
	}
	
	@Test
	void getAdminNotifications_ShouldReturnBadRequestOnError() {
		when(notificationService.getAdminNotifications()).thenThrow(new RuntimeException("Erro"));
		
		ResponseEntity<List<Notification>> response = notificationController.getAdminNotifications();
		
		assertEquals(400, response.getStatusCodeValue());
		assertNull(response.getBody());
	}
	
	// ==================== DELETE /api/notifications/{id} ====================
	@Test
	void deleteNotification_ShouldReturnOk() {
		doNothing().when(notificationService).deleteNotification("notif1");
		
		ResponseEntity<Void> response = notificationController.deleteNotification("notif1");
		
		assertEquals(200, response.getStatusCodeValue());
	}
	
	@Test
	void deleteNotification_ShouldReturnBadRequestOnError() {
		doThrow(new RuntimeException("Erro")).when(notificationService).deleteNotification("notif1");
		
		ResponseEntity<Void> response = notificationController.deleteNotification("notif1");
		
		assertEquals(400, response.getStatusCodeValue());
	}
}
