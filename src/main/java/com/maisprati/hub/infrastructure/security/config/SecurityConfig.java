package com.maisprati.hub.infrastructure.security.config;

import com.maisprati.hub.infrastructure.security.jwt.JwtTokenFilter;
import com.maisprati.hub.infrastructure.security.oauth2.CustomOAuth2UserService;
import com.maisprati.hub.infrastructure.security.oauth2.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

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
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final CorsConfigurationSource corsConfigurationSource;
	
	/**
	 * Configura o HttpSecurity definindo rotas públicas e privadas,
	 * desabilitando CSRF e adicionando o filtro JWT antes do filtro padrão de autenticação.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable) // CSRF desabilitado para APIs REST
			.cors(cors -> cors.configurationSource(corsConfigurationSource)) // habilita CORS
			.authorizeHttpRequests(auth -> auth
				                               // rotas públicas
				                               .requestMatchers(
					                               "/api/auth/login", "/api/auth/register", "/api/auth/refresh",
					                               "/api/auth/forgot-password", "/api/auth/reset-password",
					                               "/oauth2/**", "/login/oauth2/**"
				                               ).permitAll()
				                               // permitir OPTIONS sem autenticação (preflight do navegador)
				                               .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				                               // rotas privadas
				                               .anyRequest().authenticated() // qualquer outra rota requer token válido
			)
			.sessionManagement(session -> session
				                              // API sem sessão — cada requisição deve ser autenticada com token
				                              .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.oauth2Login(oauth -> oauth
				                      .userInfoEndpoint(userInfo -> userInfo.userService(
					                      customOAuth2UserService)
				                      )
				                      .successHandler(oAuth2SuccessHandler) // gera JWT e redireciona
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
}
