package com.maisprati.hub.infrastructure.email;

public interface EmailSender {
	void sendPasswordResetEmail(String to, String token);
}
