package com.voting.online_voting_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "eligibility_criteria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "election_id",
            nullable = false,
            unique = true
    )
    private Election election;

    private String department;

    private String course;

    private Integer year;
}