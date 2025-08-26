package com.maisprati.hub.repository;

import com.maisprati.hub.model.Team;
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
