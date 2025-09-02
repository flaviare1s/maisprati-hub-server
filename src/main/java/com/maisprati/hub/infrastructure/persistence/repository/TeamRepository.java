package com.maisprati.hub.infrastructure.persistence.repository;

import com.maisprati.hub.domain.model.Team;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TeamRepository extends MongoRepository<Team, String> {

    Optional<Team> findBySecurityCode(String securityCode);
    List<Team> findByIsActiveTrue();
    boolean existsBySecurityCode(String securityCode);
}
