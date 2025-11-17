package com.maisprati.hub.presentation.dto;

import com.maisprati.hub.domain.enums.EmotionalStatus;
import com.maisprati.hub.domain.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.lang.Nullable; // Adicionado para indicar campos opcionais

/**
 * DTO expandido para encapsular todos os dados de entrada necessários para o cadastro de um aluno.
 * Expõe nome, email, senha e todos os campos adicionais de perfil.
 */
@Data
public class RegisterStudentRequest {

    // Campos essenciais
    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @Email(message = "O email deve ser válido")
    @NotBlank(message = "O email é obrigatório")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    private String password;

    @Schema(defaultValue = "STUDENT", example = "STUDENT", description = "Tipo de usuário. O valor padrão, se não enviado, é 'STUDENT'.")
    @Nullable
    private UserType type;

    @Nullable
    private String whatsapp;

    @Nullable
    private String groupClass;

    @Nullable
    private Boolean hasGroup;

    @Nullable
    private Boolean wantsGroup;

    @Nullable
    private String codename;

    @Nullable
    private String avatar;

    @Schema(defaultValue = "null", example = "null", description = "Status emocional do usuário, default é null.")
    @Nullable
    private EmotionalStatus emotionalStatus;

    @Nullable
    private Boolean isActive;
}
