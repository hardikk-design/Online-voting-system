package com.voting.online_voting_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false, unique = true)
    private Election election;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Candidate winner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "released_by", nullable = false)
    private User releasedBy;

    @Column(nullable = false)
    private LocalDateTime releasedAt;
}