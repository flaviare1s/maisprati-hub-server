package com.maisprati.hub.infrastructure.persistence.repository;

import com.maisprati.hub.domain.model.User;
import com.maisprati.hub.domain.enums.UserType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

	Optional<User> findByEmail(String email);
	Optional<User> findByName(String name);
	List<User> findByType(UserType type);
	boolean existsByEmail(String email);
	boolean existsByName(String name);
}
