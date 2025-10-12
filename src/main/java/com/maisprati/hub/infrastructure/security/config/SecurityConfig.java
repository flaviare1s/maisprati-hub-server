package com.maisprati.hub.infrastructure.security.config;

import com.maisprati.hub.infrastructure.security.jwt.JwtTokenFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração de segurança da aplicação.
 *
 * <p>Define o encoder de senhas, as rotas públicas e privadas,
 * e integra o filtro JWT para validação de autenticação.</p>
 */

@Configuration
@EnableMethodSecurity
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
			.cors(cors -> cors.configurationSource(corsConfigurationSource())) // habilita CORS
			.sessionManagement(session -> session
				                              // API sem sessão — cada requisição deve ser autenticada com token
				                              .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				                               // rotas públicas
				                               .requestMatchers(
																				 "/api/auth/login", "/api/auth/register",
					                               "/api/auth/forgot-password", "/api/auth/reset-password"
				                               ).permitAll()
				                               // rotas privadas
				                               .anyRequest().authenticated() // qualquer outra rota requer token válido
			)
			// configuração de erro para requisições sem autenticação
			.exceptionHandling(exception -> exception
				                                .authenticationEntryPoint(
					                                (request,
					                                 response,
					                                 authException
					                                ) -> response.sendError(
						                                HttpServletResponse.SC_UNAUTHORIZED, "Usuário não autenticado")
				                                )
			)
			.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class); // integra o filtro JWT
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:5173", "https://maisprati-hub.vercel.app"));
		configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
