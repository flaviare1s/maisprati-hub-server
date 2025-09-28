package com.maisprati.hub.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
	
	private final JavaMailSender mailSender;
	
	public void sendPasswordResetEmail(String to, String token) {
		// TODO: URL do front que vai renderizar o formulário
		String resetUrl = "http://localhost:5173/reset-password?token=" + token;
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject("Password Reset Request");
		message.setText("Clique no link para redefinir sua senha: " + resetUrl);
		mailSender.send(message);
	}
}
