package com.voting.online_voting_system.controller;

import com.voting.online_voting_system.dto.NominationRequest;
import com.voting.online_voting_system.entity.Nomination;
import com.voting.online_voting_system.service.NominationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nominations")
public class NominationController {

    private final NominationService nominationService;

    public NominationController(
            NominationService nominationService) {

        this.nominationService = nominationService;
    }

    // Voter submits nomination

    @PostMapping
    public ResponseEntity<?> submitNomination(
            @Valid @RequestBody NominationRequest request) {

        try {

            Nomination nomination =
                    nominationService.submitNomination(request);

            return ResponseEntity.ok(nomination);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
    // Get current user's nomination for an election

    @GetMapping("/my/{electionId}")
    public ResponseEntity<?> getMyNomination(
            @PathVariable Long electionId) {

        try {

            return nominationService
                    .getMyNomination(electionId)
                    .map(ResponseEntity::ok)
                    .orElseGet(() ->
                            ResponseEntity
                                    .notFound()
                                    .build()
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // EO gets pending nominations for an election

    @GetMapping("/election/{electionId}/pending")
    public ResponseEntity<?> getPendingNominations(
            @PathVariable Long electionId) {

        try {

            List<Nomination> nominations =
                    nominationService
                            .getPendingNominations(electionId);

            return ResponseEntity.ok(nominations);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // EO approves nomination

    @PutMapping("/{nominationId}/approve")
    public ResponseEntity<?> approveNomination(
            @PathVariable Long nominationId) {

        try {

            Nomination nomination =
                    nominationService
                            .approveNomination(nominationId);

            return ResponseEntity.ok(nomination);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // EO rejects nomination

    @PutMapping("/{nominationId}/reject")
    public ResponseEntity<?> rejectNomination(
            @PathVariable Long nominationId) {

        try {

            Nomination nomination =
                    nominationService
                            .rejectNomination(nominationId);

            return ResponseEntity.ok(nomination);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}