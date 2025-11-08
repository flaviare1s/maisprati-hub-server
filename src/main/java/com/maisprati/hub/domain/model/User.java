package com.maisprati.hub.domain.model;

import com.maisprati.hub.domain.enums.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Representa um usuário do sistema, armazenado na coleção "users" do MongoDB.
 *
 * <p>Esta classe implementa {@link UserDetails} do Spring Security, permitindo
 * integração com autenticação e autorização baseada em roles.</p>
 *
 * <p><b>Anotações principais usadas:</b></p>
 * <ul>
 *     <li>{@code @Data} (Lombok) – Gera automaticamente getters, setters, toString, equals e hashCode.</li>
 *     <li>{@code @Builder} (Lombok) – Permite criar instâncias da classe usando o padrão Builder, facilitando testes e inicialização.</li>
 *     <li>{@code @NoArgsConstructor} e {@code @AllArgsConstructor} (Lombok) – Geram construtor vazio e construtor com todos os campos, respectivamente.</li>
 *     <li>{@code @Document(collection = "users")} (Spring Data MongoDB) – Indica que a classe será persistida na coleção "users".</li>
 *     <li>{@code @Id} (Spring Data MongoDB) – Marca o campo que representa o identificador único do documento.</li>
 *     <li>{@code @Indexed(unique = true)} (Spring Data MongoDB) – Cria índices únicos nos campos anotados, garantindo unicidade no banco.</li>
 *     <li>{@code @NotBlank}, {@code @NotNull}, {@code @Email} (Jakarta Validation) – Valida os campos para garantir que não sejam nulos, vazios ou tenham formato inválido.</li>
 * </ul>
 *
 * <p>Essa combinação de anotações permite um código enxuto, seguro e consistente,
 * integrando persistência, validação e autenticação de forma clara e eficiente.</p>
 */

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

	@Id
	private String id;

	@NotBlank
	@Indexed(unique = true)
	private String name;

	@Email
	@NotBlank
	@Indexed(unique = true)
	private String email;

	@NotBlank
	private String password;

	@NotNull
	private UserType type;

	private String whatsapp;
	private String groupClass;
	private Boolean hasGroup;
	private Boolean wantsGroup;
	private String codename;
	private String avatar;

	/**
	 * Indica se o usuário está ativo no sistema.
	 * Usuários inativos não podem fazer login e são removidos automaticamente de seus times.
	 * Default: true
	 */
	@Builder.Default
	private Boolean isActive = true;

	// Para controle de data
	private java.time.LocalDateTime createdAt;
	private java.time.LocalDateTime updatedAt;

	@Override
	public String toString() {
		return "User{id='" + id + "', username='" + name + "', email='" + email + "', isActive=" + isActive + "}";
	}

	/**
	 * Retorna as autoridades (roles) do usuário com base no {@link UserType}.
	 *
	 * @return lista contendo a role associada ao usuário no formato "ROLE_{TYPE}".
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + getType().getName().toUpperCase()));
	}

	/**
	 * Retorna o identificador de login do usuário.
	 * Neste caso, o login é feito através do e-mail.
	 *
	 * @return e-mail do usuário.
	 */
	@Override
	public String getUsername() {
		return email;
	}

	/**
	 * Indica se a conta do usuário está expirada.
	 * Por padrão, sempre retorna o valor definido em {@link UserDetails}.
	 *
	 * @return {@code true} se a conta não estiver expirada.
	 */
	@Override
	public boolean isAccountNonExpired() {
		return UserDetails.super.isAccountNonExpired();
	}

	/**
	 * Indica se a conta do usuário está bloqueada.
	 * Por padrão, sempre retorna o valor definido em {@link UserDetails}.
	 *
	 * @return {@code true} se a conta não estiver bloqueada.
	 */
	@Override
	public boolean isAccountNonLocked() {
		return UserDetails.super.isAccountNonLocked();
	}

	/**
	 * Indica se as credenciais do usuário estão expiradas.
	 * Por padrão, sempre retorna o valor definido em {@link UserDetails}.
	 *
	 * @return {@code true} se as credenciais estiverem válidas.
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return UserDetails.super.isCredentialsNonExpired();
	}

	/**
	 * Indica se o usuário está habilitado.
	 * Retorna o valor de {@code isActive}.
	 *
	 * @return {@code true} se o usuário estiver ativo.
	 */
	@Override
	public boolean isEnabled() {
		return isActive != null && isActive;
	}
}
