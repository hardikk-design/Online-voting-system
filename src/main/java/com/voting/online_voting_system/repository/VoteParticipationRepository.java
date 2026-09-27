package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.VoteParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteParticipationRepository
        extends JpaRepository<VoteParticipation, Long> {

    boolean existsByVoterIdAndElectionId(
            Long voterId,
            Long electionId
    );

    Optional<VoteParticipation> findByVoterIdAndElectionId(
            Long voterId,
            Long electionId
    );
}