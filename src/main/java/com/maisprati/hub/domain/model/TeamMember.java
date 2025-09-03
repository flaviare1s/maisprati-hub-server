package com.maisprati.hub.domain.model;

import com.maisprati.hub.domain.enums.TeamMemberRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

    private String userId;
    private TeamMemberRole role;
    private String subLeaderType; // Frontend, Backend, etc.
    private LocalDateTime joinedAt;
    private Boolean isActive;
    
    @Transient // Não será persistido no banco, apenas usado para transferência de dados
    private User user;
}
