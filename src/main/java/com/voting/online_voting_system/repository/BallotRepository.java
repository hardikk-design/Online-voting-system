package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.Ballot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BallotRepository extends JpaRepository<Ballot, Long> {

    List<Ballot> findByElectionId(Long electionId);
}
