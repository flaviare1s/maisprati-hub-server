package com.maisprati.hub.infrastructure.security.jwt;

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
	private long accessTokenExpiration;
	
	/** Expiração do token em segundos */
	private long refreshTokenExpiration;
	
	/** Uso de cookies seguros definido por profile */
	private boolean secureCookie;
}
