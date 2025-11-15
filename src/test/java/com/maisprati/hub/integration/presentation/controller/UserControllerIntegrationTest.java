package com.maisprati.hub.integration.presentation.controller;

import com.maisprati.hub.domain.enums.UserType;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class UserControllerIntegrationTest {
	
	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");
	
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}
	
	@Autowired private MockMvc mockMvc;
	@Autowired private UserRepository userRepository;
	@Autowired private JwtService jwtService;
	
	private User testUser;
	private User adminUser;
	
	@BeforeEach
	void setup() {
		userRepository.deleteAll();
		
		// Usuário estudante
		testUser = new User();
		testUser.setEmail("user@example.com");
		testUser.setPassword("123456");
		testUser.setType(UserType.STUDENT);
		userRepository.save(testUser);
		
		// Usuário admin
		adminUser = new User();
		adminUser.setEmail("admin@example.com");
		adminUser.setPassword("admin123");
		adminUser.setType(UserType.ADMIN);
		userRepository.save(adminUser);
	}

	@Test
	void testReadUsers() throws Exception {
		mockMvc.perform(get("/api/users")
			                .cookie(new Cookie("access_token", jwtService.generateAccessToken(adminUser))))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	void testUpdateUser() throws Exception {
		User newUser = new User();
		newUser.setEmail("updateuser@example.com");
		newUser.setPassword("123456");
		newUser.setType(UserType.STUDENT);
		userRepository.save(newUser);
		
		mockMvc.perform(put("/api/users/" + newUser.getId())
			                .cookie(new Cookie("access_token", jwtService.generateAccessToken(adminUser)))
			                .contentType(MediaType.APPLICATION_JSON)
			                .content("""
                                {"email":"updateuser@example.com","password":"654321","type":"STUDENT"}
                                """))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.password").value("654321"));
	}
	
	@Test
	void testDeleteUser() throws Exception {
		User newUser = new User();
		newUser.setEmail("deleteuser@example.com");
		newUser.setPassword("123456");
		newUser.setType(UserType.STUDENT);
		userRepository.save(newUser);
		
		mockMvc.perform(delete("/api/users/" + newUser.getId())
			                .cookie(new Cookie("access_token", jwtService.generateAccessToken(adminUser))))
			.andExpect(status().isNoContent());
	}
}
