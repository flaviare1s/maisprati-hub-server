package com.maisprati.hub.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Requisição de login")
public class LoginRequest {

    @Schema(description = "Email do usuário", example = "usuario@example.com")
    private String email;

    @Schema(description = "Senha do usuário", example = "senha123")
    private String password;
}
