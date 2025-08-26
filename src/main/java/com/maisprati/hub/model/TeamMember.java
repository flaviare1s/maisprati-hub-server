package com.maisprati.hub.model;

import com.maisprati.hub.model.enums.TeamMemberRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

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
}
