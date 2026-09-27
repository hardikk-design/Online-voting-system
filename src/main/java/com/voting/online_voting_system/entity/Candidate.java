package com.voting.online_voting_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "candidates",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "election_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "election_id",
            nullable = false
    )
    private Election election;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "nomination_id",
            nullable = false,
            unique = true
    )
    private Nomination nomination;

    @Column(nullable = false)
    private LocalDateTime approvedAt;
}