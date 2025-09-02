package com.maisprati.hub.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
	
	/** Chave secreta do JWT */
	private String secret;
	
	/** Expiração do token em segundos */
	private long expirationSeconds;
}
