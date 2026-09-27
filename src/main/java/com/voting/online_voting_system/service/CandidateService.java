package com.voting.online_voting_system.service;

import com.voting.online_voting_system.entity.Candidate;
import com.voting.online_voting_system.repository.CandidateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    public List<Candidate> getCandidatesByElection(Long electionId) {

        return candidateRepository.findByElectionId(electionId);
    }
}
