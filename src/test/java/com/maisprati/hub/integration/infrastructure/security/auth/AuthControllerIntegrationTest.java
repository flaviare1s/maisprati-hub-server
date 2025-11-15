package com.maisprati.hub.integration.infrastructure.security.auth;

import com.maisprati.hub.application.service.UserService;
import com.maisprati.hub.domain.enums.UserType;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import com.maisprati.hub.infrastructure.security.jwt.JwtService;
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
public class AuthControllerIntegrationTest {
	
	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");
	
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}
	
	@Autowired private MockMvc mockMvc;
	@Autowired private UserService userService;
	@Autowired private JwtService jwtService;
	@Autowired private UserRepository userRepository;
	
	private User testUser;
	
	@BeforeEach
	void setup() {
		userRepository.deleteAll();
		
		testUser = new User();
		testUser.setEmail("test@example.com");
		testUser.setPassword("123456");
		testUser.setType(UserType.STUDENT);
		userService.registerStudent(testUser);
	}
	
	@Test
	void testLogin() throws Exception {
		mockMvc.perform(post("/api/auth/login")
			                .contentType(MediaType.APPLICATION_JSON)
			                .content("""
                                {"email": "test@example.com", "password": "123456"}
                                """))
			.andExpect(status().isOk())
			.andExpect(cookie().exists("access_token"))
			.andExpect(cookie().exists("refresh_token"));
	}
	
	@Test
	void testRegisterStudent() throws Exception {
		mockMvc.perform(post("/api/auth/register")
			                .contentType(MediaType.APPLICATION_JSON)
			                .content("""
                                {"email":"newuser@example.com","password":"123456","type":"STUDENT"}
                                """))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.message").value("Cadastro realizado com sucesso!"));
	}
}
