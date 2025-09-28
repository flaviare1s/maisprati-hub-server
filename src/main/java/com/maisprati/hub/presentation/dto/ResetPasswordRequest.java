package com.maisprati.hub.presentation.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
	private String token;
	private String newPassword;
}
