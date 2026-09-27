package com.voting.online_voting_system.controller;

import com.voting.online_voting_system.entity.ResultVoteCount;
import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.repository.UserRepository;
import com.voting.online_voting_system.service.ResultService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService resultService;
    private final UserRepository userRepository;

    public ResultController(
            ResultService resultService,
            UserRepository userRepository
    ) {
        this.resultService = resultService;
        this.userRepository = userRepository;
    }

    // =========================================================
    // RELEASE RESULT
    // =========================================================

    @PostMapping("/{electionId}/release")
    public ResponseEntity<?> releaseResult(
            @PathVariable Long electionId,
            Authentication authentication
    ) {

        try {

            String email =
                    authentication.getName();

            User eo =
                    userRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User not found"
                                    )
                            );

            resultService.releaseResult(
                    electionId,
                    eo
            );

            return ResponseEntity.ok(
                    "Result announced successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================================================
    // VIEW RESULT
    // =========================================================

    @GetMapping("/{electionId}")
    public ResponseEntity<?> getResult(
            @PathVariable Long electionId
    ) {

        try {

            List<ResultVoteCount> results =
                    resultService.getResult(
                            electionId
                    );

            return ResponseEntity.ok(
                    results
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}