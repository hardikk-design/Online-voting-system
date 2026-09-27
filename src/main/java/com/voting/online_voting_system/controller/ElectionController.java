package com.voting.online_voting_system.controller;

import com.voting.online_voting_system.dto.CreateElectionRequest;
import com.voting.online_voting_system.entity.Election;
import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.service.ElectionService;
import com.voting.online_voting_system.repository.UserRepository;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/election-officer/elections")
public class ElectionController {

    private final ElectionService electionService;
    private final UserRepository userRepository;

    public ElectionController(
            ElectionService electionService,
            UserRepository userRepository) {

        this.electionService = electionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createElection(
            @Valid @RequestBody CreateElectionRequest request,
            Authentication authentication) {

        // DEBUG: Check who is authenticated for this request
        System.out.println(
                "========== CREATE ELECTION REQUEST =========="
        );

        System.out.println(
                "Authenticated user = "
                        + authentication.getName()
        );

        System.out.println(
                "Authorities = "
                        + authentication.getAuthorities()
        );

        System.out.println(
                "============================================="
        );

        try {

            String email = authentication.getName();

            User eo = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found"
                            )
                    );

            Election election =
                    electionService.createElection(
                            request,
                            eo
                    );

            return ResponseEntity.ok(election);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}