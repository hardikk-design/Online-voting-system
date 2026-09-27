package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.ResultVoteCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultVoteCountRepository
        extends JpaRepository<ResultVoteCount, Long> {

    List<ResultVoteCount> findByResultId(Long resultId);
}