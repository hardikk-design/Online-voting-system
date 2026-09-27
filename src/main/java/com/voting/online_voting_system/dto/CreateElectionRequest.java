package com.voting.online_voting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;



import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateElectionRequest {

    private String title;

    private String description;

    private Long postId;

    private String department;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Valid
    @NotNull(message = "Eligibility criteria is required")
    private EligibilityCriteriaRequest eligibility;
}
