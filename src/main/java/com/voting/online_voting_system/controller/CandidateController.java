package com.voting.online_voting_system.controller;

import com.voting.online_voting_system.entity.Candidate;
import com.voting.online_voting_system.service.CandidateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping("/election/{electionId}")
    public List<Candidate> getCandidatesByElection(
            @PathVariable Long electionId
    ) {
        return candidateService.getCandidatesByElection(electionId);
    }
}