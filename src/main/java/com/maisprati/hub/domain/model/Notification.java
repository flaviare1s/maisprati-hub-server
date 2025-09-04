package com.maisprati.hub.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    private String id;

    @NotBlank
    private String userId;

    @NotBlank
    private String type; // team_invitation, etc.

    @NotBlank
    private String title;

    private String message;
    private Map<String, Object> data; // dados extras flexíveis
    private LocalDateTime createdAt;
}
