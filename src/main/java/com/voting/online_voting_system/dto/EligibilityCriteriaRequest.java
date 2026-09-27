package com.voting.online_voting_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityCriteriaRequest {

    private String department;

    private String course;

    private Integer year;
}