package com.maisprati.hub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuração de segurança.
 *
 * <p>Fornece um {@link BCryptPasswordEncoder} como bean
 * para criptografia de senhas em toda a aplicação.</p>
 */
@Configuration
public class SecurityConfig {

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
