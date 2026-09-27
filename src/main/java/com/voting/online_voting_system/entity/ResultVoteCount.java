package com.voting.online_voting_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "result_vote_counts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"result_id", "candidate_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultVoteCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private Result result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(nullable = false)
    private Integer voteCount;
}