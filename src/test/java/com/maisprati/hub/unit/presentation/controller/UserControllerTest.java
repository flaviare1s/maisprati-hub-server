package com.maisprati.hub.unit.presentation.controller;

import com.maisprati.hub.application.service.UserService;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.presentation.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {
	
	@Mock private UserService userService;
	@InjectMocks private UserController userController;
	
	private User user;
	
	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		
		user = new User();
		user.setId("1");
		user.setEmail("user@test.com");
		user.setPassword("pass");
	}
	
	// ==================== /all ====================
	@Test
	void all_ShouldReturnListOfUsers() {
		List<User> users = List.of(user);
		when(userService.getAllUsers()).thenReturn(users);
		
		ResponseEntity<List<User>> response = userController.all();
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().size());
		assertEquals(user, response.getBody().get(0));
	}
	
	// ==================== /{id} GET ====================
	@Test
	void get_ShouldReturnUserWhenFound() {
		when(userService.getUserById("1")).thenReturn(Optional.of(user));
		
		ResponseEntity<User> response = userController.get("1");
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(user, response.getBody());
	}
	
	@Test
	void get_ShouldReturnNotFoundWhenUserNotFound() {
		when(userService.getUserById("2")).thenReturn(Optional.empty());
		
		ResponseEntity<User> response = userController.get("2");
		
		assertEquals(404, response.getStatusCodeValue());
		assertNull(response.getBody());
	}
	
	// ==================== /{id} PUT ====================
	@Test
	void update_ShouldReturnUpdatedUser() {
		User updatedUser = new User();
		updatedUser.setId("1");
		updatedUser.setEmail("updated@test.com");
		
		when(userService.getUserById("1")).thenReturn(Optional.of(user));
		when(userService.updateUser(any(User.class))).thenReturn(updatedUser);
		
		ResponseEntity<User> response = userController.update("1", updatedUser);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(updatedUser, response.getBody());
		verify(userService).updateUser(updatedUser);
	}
	
	@Test
	void update_ShouldReturnNotFoundWhenUserNotFound() {
		when(userService.getUserById("2")).thenReturn(Optional.empty());
		
		ResponseEntity<User> response = userController.update("2", user);
		
		assertEquals(404, response.getStatusCodeValue());
		assertNull(response.getBody());
	}
	
	// ==================== /admin PUT ====================
	@Test
	void updateAdmin_ShouldReturnUpdatedAdmin() {
		User admin = new User();
		admin.setId("adminId");
		admin.setEmail("admin@admin.com");
		
		when(userService.getUserByEmail("admin@admin.com")).thenReturn(Optional.of(admin));
		when(userService.updateUser(any(User.class))).thenReturn(admin);
		
		ResponseEntity<User> response = userController.updateAdmin(admin);
		
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(admin, response.getBody());
		verify(userService).updateUser(admin);
	}
	
	@Test
	void updateAdmin_ShouldThrowWhenAdminNotFound() {
		when(userService.getUserByEmail("admin@admin.com")).thenReturn(Optional.empty());
		
		RuntimeException exception = assertThrows(RuntimeException.class, () -> userController.updateAdmin(user));
		assertEquals("Admin não encontrado", exception.getMessage());
	}
	
	// ==================== /{id} DELETE ====================
	@Test
	void delete_ShouldReturnNoContentWhenUserExists() {
		when(userService.getUserById("1")).thenReturn(Optional.of(user));
		doNothing().when(userService).deleteUser("1");
		
		ResponseEntity<Void> response = userController.delete("1");
		
		assertEquals(204, response.getStatusCodeValue());
		verify(userService).deleteUser("1");
	}
	
	@Test
	void delete_ShouldReturnNotFoundWhenUserDoesNotExist() {
		when(userService.getUserById("2")).thenReturn(Optional.empty());
		
		ResponseEntity<Void> response = userController.delete("2");
		
		assertEquals(404, response.getStatusCodeValue());
		verify(userService, never()).deleteUser("2");
	}
}
