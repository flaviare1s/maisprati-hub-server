package com.maisprati.hub.infrastructure.persistence.repository;

import com.maisprati.hub.domain.model.GoogleToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface GoogleTokenRepository extends MongoRepository<GoogleToken, String> {
	Optional<GoogleToken> findByUserId(String userId);
}
