package com.maisprati.hub.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    private String id;

    @NotBlank
    private String name;

    @Indexed(unique = true)
    private String securityCode;

    private String description;

    @Min(1)
    @Max(20)
    private Integer maxMembers;

    private Integer currentMembers;
    private List<TeamMember> members;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
