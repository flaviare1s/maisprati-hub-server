package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.domain.enums.UserType;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela lógica de negócios relacionada a usuários.
 *
 * <p>Principais responsabilidades:</p>
 * <ul>
 *     <li>Criar usuários com senha criptografada</li>
 *     <li>Registrar alunos e professores com os papéis corretos</li>
 *     <li>Validar duplicidade de e-mail antes de salvar</li>
 *     <li>Buscar usuários pelo ID e pelo E-mail</li>
 *     <li>Valida email e senha para login</li>
 * </ul>
 *
 * <p>Depende de {@link UserRepository} para acesso ao banco e de
 * {@link BCryptPasswordEncoder} para criptografia de senhas.</p>
 */

@Service
@RequiredArgsConstructor
public class UserService {
	
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	/**
	 * Registra um aluno no sistema
	 */
	@Transactional
	public User registerStudent(User user) {
		checkEmail(user.getEmail());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setType(UserType.STUDENT);
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());
		return userRepository.save(user);
	}
	
	/**
	 * Atualiza dados do usuário
	 */
	@Transactional
	public User updateUser(User user) {
		User existing = userRepository.findById(user.getId())
			                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
		
		if (user.getName() != null) existing.setName(user.getName());
		if (user.getEmail() != null) existing.setEmail(user.getEmail());
		if (user.getPassword() != null) {
			existing.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		existing.setUpdatedAt(LocalDateTime.now());
		
		return userRepository.save(existing);
	}

	/**
	 * Deleta usuário (somente admin pode usar ou permitir ao usuário apagar sua conta)
	 */
	@Transactional
	public void deleteUser(String id) {
		userRepository.deleteById(id);
	}
	
	/**
	 * Lista todos os usuários (somente admin pode usar)
	 */
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}
	
	/**
	 * Busca um usuário pelo ID
	 */
	public Optional<User> getUserById(String id) {
		return userRepository.findById(id);
	}
	
	/**
	 * Busca um usuário pelo E-mail
	 */
	public Optional<User> getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	/**
	 * Valida duplicidade de e-mail
	 * <p>
	 * TODO: garantir atomicidade para evitar operações concorrentes (race condition)
	 */
	private void checkEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new RuntimeException("E-mail já está em uso");
		}
	}
}
