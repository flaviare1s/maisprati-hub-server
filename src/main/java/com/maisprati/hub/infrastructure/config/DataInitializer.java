package com.maisprati.hub.infrastructure.config;

import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.domain.enums.UserType;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        // Verifica se já existe um admin
        if (userRepository.findByEmail("admin@admin.com").isPresent()) {
            log.info("👤 Usuário admin já existe no banco");
            return;
        }

        // Cria o usuário admin
        User admin = User.builder()
                .name("admin")
                .email("admin@admin.com")
                .password(passwordEncoder.encode("admin123"))
                .type(UserType.ADMIN)
                .codename("Edu da Codifica")
                .avatar("/images/avatar/avatares 54.png")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);
        log.info("Usuário admin criado com sucesso!");
        log.info("Email: admin@admin.com");
        log.info("Senha: admin123");
    }
}
