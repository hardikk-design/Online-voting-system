package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.Nomination;
import com.voting.online_voting_system.enums.NominationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NominationRepository
        extends JpaRepository<Nomination, Long> {

    List<Nomination> findByElectionId(
            Long electionId
    );

    List<Nomination> findByElectionIdAndStatus(
            Long electionId,
            NominationStatus status
    );

    List<Nomination> findByUserId(
            Long userId
    );

    Optional<Nomination> findByUserIdAndElectionId(
            Long userId,
            Long electionId
    );
}
