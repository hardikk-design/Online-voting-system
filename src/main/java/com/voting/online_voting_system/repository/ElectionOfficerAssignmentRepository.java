package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.ElectionOfficerAssignment;
import com.voting.online_voting_system.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ElectionOfficerAssignmentRepository
        extends JpaRepository<ElectionOfficerAssignment, Long> {

    List<ElectionOfficerAssignment> findByUserId(Long userId);

    Optional<ElectionOfficerAssignment> findByUserIdAndStatus(
            Long userId,
            AssignmentStatus status
    );

    Optional<ElectionOfficerAssignment> findByUserIdAndElectionIdAndStatus(
            Long userId,
            Long electionId,
            AssignmentStatus status
    );

    Optional<ElectionOfficerAssignment> findByElectionIdAndStatus(
            Long electionId,
            AssignmentStatus status
    );

    // Used to verify that the EO was assigned to this election
    // even after the assignment becomes ENDED.
    Optional<ElectionOfficerAssignment> findByUserIdAndElectionId(
            Long userId,
            Long electionId
    );
}