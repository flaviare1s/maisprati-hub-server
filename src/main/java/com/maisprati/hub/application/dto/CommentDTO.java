package com.maisprati.hub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
    private String id;
    private String postId;
    private String content;
    private LocalDateTime createdAt;
    private UserDTO author;
}
