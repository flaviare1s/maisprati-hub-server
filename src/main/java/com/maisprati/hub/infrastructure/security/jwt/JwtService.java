package com.maisprati.hub.infrastructure.security.jwt;

import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
	
	private final UserRepository userRepository;
	private final JwtProperties jwtProperties;
	private Key signingKey;
	
	/**
	 * Inicializa o serviço JWT decodificando a chave Base64.
	 * <p>
	 * Lança {@code RuntimeException} se a chave for inválida.
	 */
	@PostConstruct
	public void init() {
		try {
			byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
			signingKey = Keys.hmacShaKeyFor(keyBytes);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("JWT Secret inválido. Verifique se está em Base64.", e);
		}
	}
	
	public String generateAccessToken(User user, long expirationSeconds) {
		Instant now = Instant.now();
		return Jwts.builder()
			       .setSubject(user.getEmail())
			       .claim("id", user.getId())
			       .claim("type", "access")
			       .claim("role", user.getType().getName())
			       .setIssuedAt(Date.from(now))
			       .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
			       .signWith(signingKey, SignatureAlgorithm.HS512)
			       .compact();
	}
	
	public String generateAccessToken(User user) {
		return generateAccessToken(user, jwtProperties.getAccessTokenExpiration());
	}
	
	public String generateRefreshToken(User user) {
		Instant now = Instant.now();
		return Jwts.builder()
			       .setSubject(user.getEmail())
			       .claim("id", user.getId())
			       .claim("type", "refresh")
			       .claim("role", user.getType().getName())
			       .setIssuedAt(Date.from(now))
			       .setExpiration(Date.from(now.plusSeconds(jwtProperties.getRefreshTokenExpiration())))
			       .signWith(signingKey, SignatureAlgorithm.HS512)
			       .compact();
	}
	
	public String extractUsernameFromAccessToken(String token) {
		try {
			Claims claims = Jwts.parserBuilder()
				                .setSigningKey(signingKey)
				                .build()
				                .parseClaimsJws(token)
				                .getBody();
			return "access".equals(claims.get("type")) ? claims.getSubject() : null;
		} catch (Exception e) {
			return null;
		}
	}
	
	public String extractUsernameFromRefreshToken(String token) {
		try {
			Claims claims = Jwts.parserBuilder()
				                .setSigningKey(signingKey)
				                .build()
				                .parseClaimsJws(token)
				                .getBody();
			return "refresh".equals(claims.get("type")) ? claims.getSubject() : null;
		} catch (Exception e) {
			return null;
		}
	}
	
	public boolean isAccessTokenValid(String token) {
		try {
			Claims claims = Jwts.parserBuilder()
				                .setSigningKey(signingKey)
				                .build()
				                .parseClaimsJws(token)
				                .getBody();
			return "access".equals(claims.get("type")) && claims.getExpiration().after(new Date());
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean isRefreshTokenValid(String token) {
		try {
			Claims claims = Jwts.parserBuilder()
				                .setSigningKey(signingKey)
				                .build()
				                .parseClaimsJws(token)
				                .getBody();
			return "refresh".equals(claims.get("type")) && claims.getExpiration().after(new Date());
		} catch (Exception e) {
			return false;
		}
	}
	
	public String refreshAccessToken(String refreshToken) {
		String username = extractUsernameFromRefreshToken(refreshToken);
		if (username == null) return null;
		
		var user = userRepository.findByEmail(username).orElse(null);
		if (user == null) return null;
		
		return generateAccessToken(user);
	}
	
	public void addAccessTokenToResponse(HttpServletResponse response, String accessToken) {
		Cookie cookie = new Cookie("access_token", accessToken);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge((int) jwtProperties.getAccessTokenExpiration());
		response.addCookie(cookie);
	}
}
