package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.PasswordResetToken;
import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.email.EmailSender;
import com.maisprati.hub.infrastructure.persistence.repository.PasswordResetTokenRepository;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import com.maisprati.hub.infrastructure.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {
	
	private final UserRepository userRepository;
	private final EmailSender emailSender;
	private final PasswordEncoder passwordEncoder;
	private final PasswordResetTokenRepository resetTokenRepository;
	
	public void generateAndSendToken(String email) {
		log.info("Gerando token de reset para email={}", email);
		
		User user = userRepository.findByEmail(email)
			            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
		log.info("Usuário encontrado: id={} email={}", user.getId(), user.getEmail());
		
		String rawToken = TokenGenerator.generateToken(32); // 32 bytes ≈ 43 chars
		String tokenHash = DigestUtils.md5DigestAsHex(rawToken.getBytes());
		
		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setUserId(user.getId());
		resetToken.setTokenHash(tokenHash);
		resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
		resetToken.setUsed(false);
		
		resetTokenRepository.save(resetToken);
		log.info("Reset token salvo no banco: tokenHash={} expiresAt={}", tokenHash, resetToken.getExpiresAt());
		
		emailSender.sendPasswordResetEmail(email, rawToken);
		log.info("Reset token DEV (para debug): {}", rawToken);
	}
	
	public void resetPassword(String rawToken, String newPassword) {
		String tokenHash = DigestUtils.md5DigestAsHex(rawToken.getBytes());
		
		PasswordResetToken resetToken = resetTokenRepository.findByTokenHash(tokenHash)
			                                .orElseThrow(() -> new RuntimeException("Token inválido"));
		
		if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new RuntimeException("Token expirado ou já usado");
		}
		
		User user = userRepository.findById(resetToken.getUserId())
			            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
		
		// atualiza a senha
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		
		// conferindo reset de senha
		log.info("Nova senha criptografada: {}", user.getPassword());
		boolean confere = passwordEncoder.matches(newPassword, user.getPassword());
		log.info("Senha confere? {}", confere);
		
		// invalida token
		resetToken.setUsed(true);
		resetTokenRepository.save(resetToken);
	}
}
