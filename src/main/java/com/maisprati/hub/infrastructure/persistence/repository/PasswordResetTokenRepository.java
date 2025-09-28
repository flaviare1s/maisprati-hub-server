package com.maisprati.hub.infrastructure.persistence.repository;

import com.maisprati.hub.domain.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
	Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}