package com.voting.online_voting_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NominationRequest {

    @NotNull(message = "Election ID is required")
    private Long electionId;

    private String position;
}