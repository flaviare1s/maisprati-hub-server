package com.maisprati.hub.infrastructure.integration.email;

public interface EmailSender {
	void sendPasswordResetEmail(String to, String token);
}
