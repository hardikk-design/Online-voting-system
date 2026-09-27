package com.voting.online_voting_system.entity;

import com.voting.online_voting_system.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "election_officer_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectionOfficerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who has been assigned as Election Officer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    // Election can be NULL initially.
    // The EO is assigned before creating an election.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id")
    private Election election;

    // Admin who assigned this user as EO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_by",
            nullable = false
    )
    private User assignedBy;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    // ACTIVE = currently an EO
    // ENDED = EO assignment has ended
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;
}