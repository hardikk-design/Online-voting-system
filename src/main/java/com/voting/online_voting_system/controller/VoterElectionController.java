package com.voting.online_voting_system.controller;

import com.voting.online_voting_system.entity.Election;
import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.repository.UserRepository;
import com.voting.online_voting_system.service.ElectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/elections")
public class VoterElectionController {

    private final ElectionService electionService;
    private final UserRepository userRepository;

    public VoterElectionController(
            ElectionService electionService,
            UserRepository userRepository
    ) {
        this.electionService = electionService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getEligibleElections(
            Authentication authentication
    ) {

        try {

            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found"
                            )
                    );

            List<Election> elections =
                    electionService.getEligibleElections(user);

            return ResponseEntity.ok(elections);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}