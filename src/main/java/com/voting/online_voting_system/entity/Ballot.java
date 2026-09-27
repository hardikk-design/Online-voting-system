package com.voting.online_voting_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ballots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ballot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @Lob
    @Column(nullable = false)
    private String encryptedVote;

    @Column(nullable = false)
    private LocalDateTime castAt;

    @PreUpdate
    public void preventUpdate() {
        throw new IllegalStateException("Ballots cannot be updated");
    }

    @PreRemove
    public void preventDelete() {
        throw new IllegalStateException("Ballots cannot be deleted");
    }
}