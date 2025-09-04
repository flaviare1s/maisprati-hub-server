package com.maisprati.hub.infrastructure.persistence.repository;

import com.maisprati.hub.domain.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    void deleteByUserId(String userId);
}
