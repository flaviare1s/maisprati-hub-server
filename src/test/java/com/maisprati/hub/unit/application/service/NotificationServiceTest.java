package com.maisprati.hub.unit.application.service;

import com.maisprati.hub.application.service.NotificationService;
import com.maisprati.hub.domain.model.*;
import com.maisprati.hub.infrastructure.persistence.repository.NotificationRepository;
import com.maisprati.hub.infrastructure.persistence.repository.TeamRepository;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class NotificationServiceTest {
	
	@Mock private NotificationRepository notificationRepository;
	@Mock private UserRepository userRepository;
	@Mock private TeamRepository teamRepository;
	@InjectMocks private NotificationService notificationService;
	
	@BeforeEach
	void setUp() {
		// Inicializa os mocks antes de cada teste
		MockitoAnnotations.openMocks(this);
	}
	
	// TEST 1 — Obter notificações de um usuário existente
	@Test
	void shouldReturnUserNotifications_WhenUserExists() {
		// Arrange
		List<Notification> expected = List.of(new Notification());
		when(notificationRepository.findByUserIdOrderByCreatedAtDesc("user123"))
			.thenReturn(expected);
		
		// Act
		List<Notification> result = notificationService.getUserNotifications("user123");
		
		// Assert
		assertEquals(1, result.size());
		verify(notificationRepository).findByUserIdOrderByCreatedAtDesc("user123");
	}
	
	// TEST 2 — Obter notificações de admin inexistente retorna lista vazia
	@Test
	void shouldReturnEmptyList_WhenAdminDoesNotExist() {
		// Arrange
		when(userRepository.findByEmail("admin@admin.com")).thenReturn(Optional.empty());
		
		// Act
		List<Notification> result = notificationService.getAdminNotifications();
		
		// Assert
		assertTrue(result.isEmpty());
		verify(notificationRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
	}
	
	// TEST 3 — Obter notificações de admin existente
	@Test
	void shouldReturnAdminNotifications_WhenAdminExists() {
		// Arrange
		User admin = new User();
		admin.setId("admin123");
		when(userRepository.findByEmail("admin@admin.com")).thenReturn(Optional.of(admin));
		when(notificationRepository.findByUserIdOrderByCreatedAtDesc("admin123"))
			.thenReturn(List.of(new Notification()));
		
		// Act
		List<Notification> result = notificationService.getAdminNotifications();
		
		// Assert
		assertEquals(1, result.size());
		verify(notificationRepository).findByUserIdOrderByCreatedAtDesc("admin123");
	}
	
	// TEST 4 — Criar notificação com sucesso
	@Test
	void shouldCreateNotificationSuccessfully() {
		// Arrange
		Notification input = Notification.builder()
			                     .userId("u1")
			                     .type("info")
			                     .title("Test Title")
			                     .message("Test Message")
			                     .build();
		when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		// Act
		Notification result = notificationService.createNotification(input);
		
		// Assert
		assertNotNull(result);
		assertEquals("u1", result.getUserId());
		verify(notificationRepository).save(any(Notification.class));
	}
	
	// TEST 5 — Deletar notificação por ID
	@Test
	void shouldDeleteNotificationById() {
		// Arrange
		String notificationId = "n1";
		
		// Act
		notificationService.deleteNotification(notificationId);
		
		// Assert
		verify(notificationRepository).deleteById(notificationId);
	}
	
	// TEST 6 — Enviar notificação para admin existente
	@Test
	void shouldSendMessageToAdmin_WhenAdminExists() {
		// Arrange
		User admin = new User();
		admin.setId("admin123");
		when(userRepository.findByEmail("admin@admin.com")).thenReturn(Optional.of(admin));
		when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		// Act
		Notification result = notificationService.sendNotificationToAdmin("Alice", "Hello Admin!");
		
		// Assert
		assertNotNull(result);
		assertEquals("admin123", result.getUserId());
		verify(notificationRepository).save(any(Notification.class));
	}
	
	// TEST 7 — Enviar notificação quando admin não encontrado retorna null
	@Test
	void shouldReturnNull_WhenSendingMessageAndAdminNotFound() {
		// Arrange
		when(userRepository.findByEmail("admin@admin.com")).thenReturn(Optional.empty());
		
		// Act
		Notification result = notificationService.sendNotificationToAdmin("Bob", "Hi!");
		
		// Assert
		assertNull(result);
		verify(notificationRepository, never()).save(any());
	}
	
	// TEST 8 — Criar notificações para agendamento individual
	@Test
	void shouldCreateNotificationsForIndividualAppointment_WhenScheduled() {
		// Arrange
		Appointment appointment = new Appointment();
		appointment.setId("app1");
		appointment.setDate(LocalDate.now());
		appointment.setTime(LocalTime.now());
		appointment.setStudentId("student1");
		appointment.setAdminId("admin1");
		
		User student = new User();
		student.setId("student1");
		student.setName("Alice");
		when(userRepository.findById("student1")).thenReturn(Optional.of(student));
		when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		// Act
		notificationService.createNotificationForAppointment(appointment, "SCHEDULED");
		
		// Assert
		verify(notificationRepository, atLeast(2)).save(any(Notification.class));
	}
	
	// TEST 9 — Criar notificações para agendamento de time
	@Test
	void shouldHandleTeamAppointmentNotifications_WhenScheduled() {
		// Arrange
		Appointment appointment = new Appointment();
		appointment.setId("app2");
		appointment.setDate(LocalDate.now());
		appointment.setTime(LocalTime.now());
		appointment.setTeamId("team1");
		appointment.setStudentId("student1");
		appointment.setAdminId("admin1");
		
		Team team = new Team();
		team.setId("team1");
		team.setName("Dev Team");
		
		TeamMember member1 = new TeamMember();
		member1.setUserId("student1");
		TeamMember member2 = new TeamMember();
		member2.setUserId("student2");
		
		team.setMembers(List.of(member1, member2));
		
		when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
		when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		// Act
		notificationService.createNotificationForAppointment(appointment, "SCHEDULED");
		
		// Assert
		verify(notificationRepository, atLeast(3)).save(any(Notification.class));
	}
	
	// TEST 10 — Notificar admin quando usuário entra no time
	@Test
	void shouldNotifyAdminWhenUserJoinsTeam() {
		// Arrange
		User admin = User.builder().id("admin123").name("Admin").build();
		when(userRepository.findByEmail("admin@admin.com")).thenReturn(Optional.of(admin));
		when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		// Act
		notificationService.notifyAdminTeamJoin("Alice", "Dev Team");
		
		// Assert
		verify(notificationRepository).save(argThat(notification ->
			                                            notification.getTitle().contains("Novo membro no time") &&
				                                            notification.getMessage().contains("Alice") &&
				                                            notification.getMessage().contains("Dev Team")
		));
	}
	
	// TEST 11 — Notificar admin quando usuário sai do time
	@Test
	void shouldNotifyAdminWhenUserExitsTeam() {
		// Arrange
		User admin = User.builder().id("admin123").name("Admin").build();
		when(userRepository.findByEmail("admin@admin.com")).thenReturn(Optional.of(admin));
		when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		
		// Act
		notificationService.notifyAdminTeamExit("Bob", "Dev Team", "Motivo pessoal");
		
		// Assert
		verify(notificationRepository).save(argThat(notification ->
			                                            notification.getTitle().contains("Membro saiu do time") &&
				                                            notification.getMessage().contains("Bob") &&
				                                            notification.getMessage().contains("Dev Team") &&
				                                            notification.getMessage().contains("Motivo pessoal")
		));
	}
	
	// TEST 12 — Notificações de agendamento individual cancelado
	@Test
	void shouldCreateNotificationsForCancelledIndividualAppointmentViaPublicMethod() {
		// Arrange
		Appointment appointment = new Appointment();
		appointment.setId("appCancelled");
		appointment.setDate(LocalDate.now());
		appointment.setTime(LocalTime.now());
		appointment.setStudentId("student1");
		appointment.setAdminId("admin1");
		
		User admin = User.builder().id("admin1").name("Admin").build();
		User student = User.builder().id("student1").name("Student").build();
		when(userRepository.findById("admin1")).thenReturn(Optional.of(admin));
		when(userRepository.findById("student1")).thenReturn(Optional.of(student));
		
		// Act
		notificationService.createNotificationForAppointment(appointment, "CANCELLED");
		
		// Assert
		verify(notificationRepository, times(2)).save(any(Notification.class));
	}
	
	// TEST 13 — Notificações de agendamento individual concluído
	@Test
	void shouldCreateNotificationsForCompletedIndividualAppointmentViaPublicMethod() {
		// Arrange
		Appointment appointment = new Appointment();
		appointment.setId("appCompleted");
		appointment.setDate(LocalDate.now());
		appointment.setTime(LocalTime.now());
		appointment.setStudentId("student1");
		appointment.setAdminId("admin1");
		
		User admin = User.builder().id("admin1").name("Admin").build();
		User student = User.builder().id("student1").name("Student").build();
		when(userRepository.findById("admin1")).thenReturn(Optional.of(admin));
		when(userRepository.findById("student1")).thenReturn(Optional.of(student));
		
		// Act
		notificationService.createNotificationForAppointment(appointment, "COMPLETED");
		
		// Assert
		verify(notificationRepository, times(2)).save(any(Notification.class));
	}
	
	// TEST 14 — Notificações de agendamento de time cancelado
	@Test
	void shouldCreateNotificationsForCancelledTeamAppointmentViaPublicMethod() {
		// Arrange
		Appointment appointment = new Appointment();
		appointment.setId("teamAppCancelled");
		appointment.setDate(LocalDate.now());
		appointment.setTime(LocalTime.now());
		appointment.setTeamId("team1");
		appointment.setAdminId("admin1");
		
		Team team = new Team();
		team.setId("team1");
		team.setName("Dev Team");
		TeamMember member1 = new TeamMember(); member1.setUserId("user1");
		TeamMember member2 = new TeamMember(); member2.setUserId("user2");
		team.setMembers(List.of(member1, member2));
		
		when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
		
		// Act
		notificationService.createNotificationForAppointment(appointment, "CANCELLED");
		
		// Assert
		verify(notificationRepository, times(3)).save(any(Notification.class)); // Admin + 2 membros
	}
	
	// TEST 15 — Notificações de agendamento de time concluído
	@Test
	void shouldCreateNotificationsForCompletedTeamAppointmentViaPublicMethod() {
		// Arrange
		Appointment appointment = new Appointment();
		appointment.setId("teamAppCompleted");
		appointment.setDate(LocalDate.now());
		appointment.setTime(LocalTime.now());
		appointment.setTeamId("team1");
		appointment.setAdminId("admin1");
		
		Team team = new Team();
		team.setId("team1");
		team.setName("Dev Team");
		TeamMember member1 = new TeamMember(); member1.setUserId("user1");
		TeamMember member2 = new TeamMember(); member2.setUserId("user2");
		team.setMembers(List.of(member1, member2));
		
		when(teamRepository.findById("team1")).thenReturn(Optional.of(team));
		
		// Act
		notificationService.createNotificationForAppointment(appointment, "COMPLETED");
		
		// Assert
		verify(notificationRepository, times(3)).save(any(Notification.class)); // Admin + 2 membros
	}
}
