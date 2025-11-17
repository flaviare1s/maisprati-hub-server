package com.maisprati.hub.infrastructure.email;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@Profile("prod")
public class GmailApiEmailSender implements EmailSender {
	
	@Value("${GOOGLE_CLIENT_ID}")
	private String clientId;
	
	@Value("${GOOGLE_CLIENT_SECRET}")
	private String clientSecret;
	
	@Value("${GMAIL_API_REFRESH_TOKEN}")
	private String refreshToken;
	
	@Value("${SPRING_MAIL_USERNAME}")
	private String from;
	
	@Value("${APP_FRONTEND_URL:http://localhost:5173}")
	private String frontendUrl;
	
	@PostConstruct
	public void init() {
		log.info("GmailApiEmailSender iniciado! from={} frontendUrl={} refreshToken={} clientId={}",
			from, frontendUrl, refreshToken != null, clientId != null);
	}
	
	private Gmail buildGmailService() throws Exception {
		log.info("Construindo Gmail service com clientId={} refreshTokenSet={}", clientId, refreshToken != null);
		
		GoogleCredential credential = new GoogleCredential.Builder()
			                              .setClientSecrets(clientId, clientSecret)
			                              .setTransport(GoogleNetHttpTransport.newTrustedTransport())
			                              .setJsonFactory(JacksonFactory.getDefaultInstance())
			                              .build();
		credential.setRefreshToken(refreshToken);
		boolean refreshed = credential.refreshToken(); // garante access token válido
		log.info("Access token gerado? {}", credential.getAccessToken() != null);
		
		return new Gmail.Builder(
			GoogleNetHttpTransport.newTrustedTransport(),
			JacksonFactory.getDefaultInstance(),
			credential
		).setApplicationName("MaisPraTi Hub")
			       .build();
	}
	
	@Override
	public void sendPasswordResetEmail(String to, String token) {
		log.info("Preparando envio de email para {} com token={}", to, token);
		try {
			Gmail service = buildGmailService();
			
			String link = frontendUrl + "/new-password?token=" + token;
			
			String rawEmail = "From: " + from + "\r\n" +
				                  "To: " + to + "\r\n" +
				                  "Subject: Password Reset Request\r\n" +
				                  "Content-Type: text/html; charset=UTF-8\r\n\r\n" +
				                  "<p>Clique no link para redefinir sua senha:</p>" +
				                  "<a href=\"" + link + "\">Redefinir Senha</a>";
			
			Message message = new Message();
			message.setRaw(Base64.getEncoder().encodeToString(rawEmail.getBytes(StandardCharsets.UTF_8)));
			
			service.users().messages().send("me", message).execute();
			log.info("Email enviado via Gmail API para {}", to);
			
		} catch (Exception e) {
			log.error("Erro ao enviar email via Gmail API", e); // stack trace completa
			throw new RuntimeException("Falha ao enviar email de redefinição de senha. Contate o suporte.");
		}
	}
}
