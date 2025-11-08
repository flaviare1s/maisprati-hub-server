package com.maisprati.hub.application.service;

import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Serviço que carrega dados do usuário para autenticação.
 *
 * O Spring Security chama automaticamente o método loadUserByUsername()
 * durante o login. O método isEnabled() do User será verificado aqui.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        // O Spring Security automaticamente verifica user.isEnabled()
        // Se retornar false, lança DisabledException
        return user;
    }
}
