package com.maisprati.hub.model;

import com.maisprati.hub.model.enums.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	private String id;

	@NotBlank
	@Indexed(unique = true)
	private String name;

	@Email
	@NotBlank
	@Indexed(unique = true)
	private String email;

	@NotBlank
	private String password;

	@NotNull
	private UserType type;

	private String whatsapp;
	private String groupClass;
	private Boolean hasGroup;
	private Boolean wantsGroup;
	private Boolean isFirstLogin;
	private String codename;
	private String avatar;

	// Para controle de data
	private java.time.LocalDateTime createdAt;
	private java.time.LocalDateTime updatedAt;

	@Override
	public String toString() {
		return "User{id='" + id + "', username='" + name + "', email='" + email + "'}";
	}
}
