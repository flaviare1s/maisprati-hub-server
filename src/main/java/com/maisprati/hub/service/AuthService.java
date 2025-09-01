package com.maisprati.hub.service;

import com.maisprati.hub.model.User;
import com.maisprati.hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
	
	private final UserRepository userRepository;
	/**
	 * {@link PasswordEncoder} é a interface do Spring Security para codificar e validar senhas.
	 * O bean {@link BCryptPasswordEncoder} é injetado automaticamente onde
	 * {@link PasswordEncoder} é usado, garantindo flexibilidade e segurança.
	 */
	private final PasswordEncoder passwordEncoder;
//	private final JwtService jwtService; // TODO: classe que gera/valida JWT
	
	/**
	 * Carrega um usuário pelo e-mail para autenticação no Spring Security.
	 *
	 * <p>Busca o usuário no banco de dados usando {@link UserRepository#findByEmail},
	 * que retorna um {@link Optional}. Desembrulha o Optional com {@link Optional#orElseThrow()}
	 * para fornecer o {@link User} como {@link UserDetails},
	 * ou lançar {@link UsernameNotFoundException}
	 * se não encontrado.</p>
	 *
	 * @param username e-mail do usuário
	 * @return {@link UserDetails} do usuário
	 * @throws UsernameNotFoundException se o usuário não existir
	 */
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmail(username)
			       .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
	}
	
	public void login(String email, String password) {
		User user = userRepository.findByEmail(email)
			            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
		
		// compara a senha digitada com a criptografada no banco
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new RuntimeException("Credenciais inválidas");
		}
		
		// se bateu, gera o token JWT
//		return jwtService.generateToken(user);
	}
}
