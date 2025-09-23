package com.maisprati.hub.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "password_reset_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {
	
	@Id
	private String id;
	
	@NotBlank
	private String userId;
	
	@NotBlank
	private String tokenHash;
	
	@NotNull
	private LocalDateTime expiresAt;
	
	private boolean used = false;
	
}