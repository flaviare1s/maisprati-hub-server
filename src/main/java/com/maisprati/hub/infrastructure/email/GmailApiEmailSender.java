package com.maisprati.hub.infrastructure.email;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@Profile("prod")
public class GmailApiEmailSender implements EmailSender {
	
	private final TemplateEngine templateEngine;
	
	@Value("${APP_FRONTEND_URL}")
	private String frontendUrl;
	
	@Value("${SPRING_MAIL_USERNAME}")
	private String from;
	
	@Value("${GOOGLE_CLIENT_ID}")
	private String clientId;
	
	@Value("${GOOGLE_CLIENT_SECRET}")
	private String clientSecret;
	
	@Value("${GMAIL_API_REFRESH_TOKEN}")
	private String refreshToken;
	
	public GmailApiEmailSender(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}
	
	@Override
	public void sendPasswordResetEmail(String to, String name, String token) {
		try {
			Gmail service = buildGmailService();
			
			String resetUrl = frontendUrl + "/new-password?token=" + token;
			
			Context ctx = new Context();
			ctx.setVariable("name", name);
			ctx.setVariable("resetUrl", resetUrl);
			
			String html = templateEngine.process("password-reset", ctx);
			
			String raw = "From: " + from + "\r\n" +
				             "To: " + to + "\r\n" +
				             "Subject: Redefinição de senha\r\n" +
				             "Content-Type: text/html; charset=UTF-8\r\n\r\n" +
				             html;
			
			Message message = new Message();
			message.setRaw(Base64.getEncoder()
				               .encodeToString(raw.getBytes(StandardCharsets.UTF_8)));
			
			service.users().messages().send("me", message).execute();
			
		} catch (Exception e) {
			log.error("Erro no envio Gmail API", e);
			throw new RuntimeException("Falha ao enviar e-mail.");
		}
	}
	
	private Gmail buildGmailService() throws Exception {
		log.info("Construindo Gmail service com clientId={} refreshTokenSet={}", clientId, refreshToken != null);
		
		GoogleCredential credential = new GoogleCredential.Builder()
			                              .setClientSecrets(clientId, clientSecret)
			                              .setTransport(GoogleNetHttpTransport.newTrustedTransport())
			                              .setJsonFactory(JacksonFactory.getDefaultInstance())
			                              .build();
		
		credential.setRefreshToken(refreshToken);
		credential.refreshToken();
		
		return new Gmail.Builder(
			GoogleNetHttpTransport.newTrustedTransport(),
			JacksonFactory.getDefaultInstance(),
			credential
		).setApplicationName("MaisPraTi Hub").build();
	}
}
