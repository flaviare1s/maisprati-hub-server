package com.maisprati.hub.service;

import com.maisprati.hub.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {
	
	/** Expiração padrão do token em segundos (ajustável) */
	private static final long DEFAULT_EXPIRATION_SECONDS = 10;
	
	private Key signingKey;
	
	/** Chave secreta JWT definida no .env ou application.properties */
	@Value("${jwt.secret}")
	private String secretKey;
	
	/**
	 * Inicializa o serviço JWT decodificando a chave Base64.
	 * <p>
	 * Lança {@code RuntimeException} se a chave for inválida.
	 */
	@PostConstruct
	public void init() {
		try {
			byte[] keyBytes = Base64.getDecoder().decode(secretKey);
			signingKey = Keys.hmacShaKeyFor(keyBytes);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("JWT Secret inválido. Verifique se está em Base64.", e);
		}
	}
	
	/**
	 * Gera token JWT para o usuário com expiração customizada.
	 *
	 * @param user usuário que terá o token gerado
	 * @param expirationSeconds tempo em segundos até o token expirar
	 * @return token JWT como {@code String}
	 * <p>
	 * Exemplo:
	 * <pre>
	 * {@code
	 * String token = jwtService.generateToken(user, 60); // expira em 60 segundos
	 * }
	 * </pre>
	 */
	public String generateToken(User user, long expirationSeconds) {
		Instant now = Instant.now();
		return Jwts.builder()
			       .setSubject(user.getEmail())
			       .claim("role", user.getType().getName())
			       .setIssuedAt(Date.from(now))
			       .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
			       .signWith(signingKey, SignatureAlgorithm.HS512)
			       .compact();
	}
	
	/**
	 * Gera token JWT com expiração padrão definida em {@link #DEFAULT_EXPIRATION_SECONDS}.
	 *
	 * @param user usuário que terá o token gerado
	 * @return token JWT como {@code String}
	 */
	public String generateToken(User user) {
		return generateToken(user, DEFAULT_EXPIRATION_SECONDS);
	}
	
	/**
	 * Extrai o username (email) do token JWT.
	 *
	 * @param token token JWT
	 * @return username ou {@code null} se token expirou ou for inválido
	 */
	public String extractUsername(String token) {
		try {
			Claims claims = Jwts.parserBuilder()
				                .setSigningKey(signingKey)
				                .build()
				                .parseClaimsJws(token)
				                .getBody();
			return claims.getSubject();
		} catch (ExpiredJwtException e) {
			return null;
		} catch (Exception e) {
			return null; // Token inválido
		}
	}
	
	/**
	 * Valida se o token é válido:
	 * <ol>
	 *   <li>Username do token bate com o usuário fornecido</li>
	 *   <li>Token ainda não expirou</li>
	 * </ol>
	 *
	 * @param token token JWT
	 * @param userDetails objeto {@code UserDetails} do usuário
	 * @return {@code true} se token válido, {@code false} caso contrário
	 * <p>
	 * Exemplo:
	 * <pre>
	 * {@code
	 * boolean valid = jwtService.validateToken(token, userDetails);
	 * }
	 * </pre>
	 */
	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			Claims claims = Jwts.parserBuilder()
				                .setSigningKey(signingKey)
				                .build()
				                .parseClaimsJws(token)
				                .getBody();
			
			String username = claims.getSubject();
			Date expiration = claims.getExpiration();
			
			return username.equals(userDetails.getUsername()) && expiration.after(new Date());
		} catch (ExpiredJwtException e) {
			return false;
		} catch (Exception e) {
			return false; // Token inválido
		}
	}
}
