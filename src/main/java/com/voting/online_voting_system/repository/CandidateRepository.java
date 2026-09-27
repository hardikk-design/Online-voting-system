package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateRepository
        extends JpaRepository<Candidate, Long> {

    List<Candidate> findByElectionId(
            Long electionId
    );

    List<Candidate> findByUserId(
            Long userId
    );
}