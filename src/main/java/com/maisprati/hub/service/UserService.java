package com.maisprati.hub.service;

import com.maisprati.hub.model.User;
import com.maisprati.hub.model.enums.UserType;
import com.maisprati.hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
	 * Função genérica para criar usuários com tipo definido
	 */
	@Transactional
	public User registerUser(User user, UserType type) {
		checkEmail(user.getEmail());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setType(type);
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());
		return userRepository.save(user);
	}
	
	/**
	 * Registra um aluno no sistema
	 */
	@Transactional
	public User registerStudent(User user) {
		return registerUser(user, UserType.STUDENT);
	}
	
	/**
	 * Registra o professor no sistema
	 */
	@Transactional
	public User registerProfessor(User user) {
		return registerUser(user, UserType.ADMIN);
	}
	
	/**
	 * Valida email e senha para login
	 */
	public User login(String email, String rawPassword) {
		return userRepository.findByEmail(email)
			       .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
			       .orElseThrow(() -> new RuntimeException("Usuário ou senha inválidos!"));
	}
	
	/**
	 * Lista todos os usuário
	 * <br><br>
	 * TODO: rota deve ser restrita a ADMIN
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
	 * <br><br>
	 * TODO: garantir atomicidade para evitar operações concorrentes (race condition)
	 */
	private void checkEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new RuntimeException("E-mail já está em uso");
		}
	}
}
