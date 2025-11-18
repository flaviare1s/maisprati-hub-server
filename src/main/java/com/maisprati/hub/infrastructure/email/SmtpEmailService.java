package com.maisprati.hub.infrastructure.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
@RequiredArgsConstructor
@Profile("dev")
public class SmtpEmailService implements EmailSender {
	
	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;
	
	@Value("${APP_FRONTEND_URL:http://localhost:5173}")
	private String frontendUrl;
	
	@Override
	public void sendPasswordResetEmail(String to, String name, String token) {
		try {
			String resetUrl = frontendUrl + "/new-password?token=" + token;
			
			Context context = new Context();
			context.setVariable("name", name);
			context.setVariable("resetUrl", resetUrl);
			
			String html = templateEngine.process("password-reset", context);
			
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			
			helper.setTo(to);
			helper.setSubject("Redefinição de senha");
			helper.setText(html, true);
			
			mailSender.send(mimeMessage);
			
		} catch (Exception e) {
			throw new RuntimeException("Erro ao enviar e-mail de redefinição", e);
		}
	}
}
