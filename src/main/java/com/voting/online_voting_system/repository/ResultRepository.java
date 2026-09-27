package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    Optional<Result> findByElectionId(Long electionId);

    boolean existsByElectionId(Long electionId);
}