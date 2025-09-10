package com.maisprati.hub.infrastructure.persistence.repository;

import com.maisprati.hub.domain.model.ProjectProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectProgressRepository extends MongoRepository<ProjectProgress, String> {
    Optional<ProjectProgress> findByTeamId(String teamId);
    List<ProjectProgress> findAllByTeamId(String teamId);
}
