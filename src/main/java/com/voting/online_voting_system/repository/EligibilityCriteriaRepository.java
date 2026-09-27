package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.EligibilityCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EligibilityCriteriaRepository
        extends JpaRepository<EligibilityCriteria, Long> {

    Optional<EligibilityCriteria> findByElectionId(Long electionId);
}
