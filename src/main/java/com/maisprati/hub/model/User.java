package com.maisprati.hub.model;

import com.maisprati.hub.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "users")
public class User {
	
	@Id
	private String id;
	
	@NotNull
	private String name;
	
	@Email
	@NotBlank
	private String email;
	
	@NotBlank
	private String password;
	
	@NotNull
	private Role role;
	
	private String whatsapp;
	private String cohort;
	
	private Boolean hasGroup = false;
	private Boolean wantsGroup = true;
	private Boolean isFirstLogin = true;
	
	private String codename;
	private String avatar;
	
	@CreatedDate
	private LocalDateTime createdAt;
	
	@LastModifiedDate
	private LocalDateTime updatedAt;
}
