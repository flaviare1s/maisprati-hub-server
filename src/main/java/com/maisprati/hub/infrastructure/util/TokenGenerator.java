package com.maisprati.hub.infrastructure.util;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenGenerator {
	private static final SecureRandom secureRandom = new SecureRandom();
	
	public static String generateToken(int lengthBytes) {
		byte[] tokenBytes = new byte[lengthBytes];
		secureRandom.nextBytes(tokenBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
	}
}
