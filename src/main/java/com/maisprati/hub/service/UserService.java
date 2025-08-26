package com.maisprati.hub.service;

import com.maisprati.hub.model.User;
import com.maisprati.hub.model.enums.UserType;
import com.maisprati.hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Serviço responsável pela lógica de negócios relacionada a usuários.
 *
 * <p>Principais responsabilidades:</p>
 * <ul>
 *     <li>Criar usuários com senha criptografada</li>
 *     <li>Registrar alunos e professores com os papéis corretos</li>
 *     <li>Validar duplicidade de e-mail antes de salvar</li>
 *     <li>Buscar usuários pelo ID</li>
 * </ul>
 *
 * <p>Depende de {@link UserRepository} para acesso ao banco e de
 * {@link BCryptPasswordEncoder} para criptografia de senhas.</p>
 */

@Service
@RequiredArgsConstructor // construtor automático para os campos final
public class UserService {
	
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	/** Cria um usuário genérico no sistema */
	public User createUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}
	
	/** Registra um aluno no sistema */
	public User registerStudent(User user) {
		checkEmail(user.getEmail());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setType(UserType.STUDENT);
		return userRepository.save(user);
	}

	/** Busca um usuário pelo ID */
	public Optional<User> getUserById(String id) {
		return userRepository.findById(id);
	}
	
	/** Valida duplicidade de e-mail */
	private void checkEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new RuntimeException("E-mail já está em uso");
		}
	}
}
