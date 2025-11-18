package com.maisprati.hub.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "google_tokens")
public class GoogleToken {
	
	@Id
	private String id;
	
	private String userId; // referência ao usuário
	private String accessToken;
	private String refreshToken;
	private LocalDateTime accessTokenExpiry;
}
