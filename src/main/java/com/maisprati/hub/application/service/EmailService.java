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
		String resetUrl = "http://localhost:8080/reset-password?token=" + token;
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject("Password Reset Request");
		message.setText("Clique no link para resetar sua senha: " + resetUrl);
		mailSender.send(message);
	}
}
