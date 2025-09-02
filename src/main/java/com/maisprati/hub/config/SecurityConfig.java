package com.maisprati.hub.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração de segurança da aplicação.
 *
 * <p>Define o encoder de senhas, as rotas públicas e privadas,
 * e integra o filtro JWT para validação de autenticação.</p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final JwtTokenFilter jwtTokenFilter;
	
	/**
	 * Bean para criptografia de senhas usando BCrypt.
	 */
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	/**
	 * Bean que expõe o AuthenticationManager, necessário para autenticação.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
	
	/**
	 * Configura o HttpSecurity definindo rotas públicas e privadas,
	 * desabilitando CSRF e adicionando o filtro JWT antes do filtro padrão de autenticação.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable) // CSRF desabilitado para APIs REST
			.authorizeHttpRequests(auth -> auth
				                               .requestMatchers("api/auth/login", "api/auth/register").permitAll() // rotas públicas
				                               .anyRequest().authenticated() // qualquer outra rota requer token válido
			)
			.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class); // integra o filtro JWT
		return http.build();
	}
}
