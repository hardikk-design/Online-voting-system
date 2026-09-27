package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.VotingChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VotingChallengeRepository
        extends JpaRepository<VotingChallenge, Long> {

    Optional<VotingChallenge>
    findTopByUserIdAndElectionIdOrderByCreatedAtDesc(
            Long userId,
            Long electionId
    );
}
