package com.maisprati.hub.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Document(collection = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    private String id;

    @NotBlank
    private String authorId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
