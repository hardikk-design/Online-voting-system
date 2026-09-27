package com.voting.online_voting_system.controller;

import com.voting.online_voting_system.dto.OtpRequest;
import com.voting.online_voting_system.dto.OtpVerificationRequest;
import com.voting.online_voting_system.dto.VoteRequest;
import com.voting.online_voting_system.service.VotingService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
public class VotingController {

    private final VotingService votingService;

    public VotingController(
            VotingService votingService
    ) {
        this.votingService = votingService;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(
            @Valid @RequestBody OtpRequest request
    ) {

        try {

            String otp =
                    votingService.requestOtp(
                            request.getElectionId()
                    );

            return ResponseEntity.ok(
                    new OtpResponse(
                            "OTP generated successfully",
                            otp
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @Valid @RequestBody
            OtpVerificationRequest request
    ) {

        try {

            votingService.verifyOtp(request);

            return ResponseEntity.ok(
                    "OTP verified successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> castVote(
            @Valid @RequestBody VoteRequest request
    ) {

        try {

            votingService.castVote(request);

            return ResponseEntity.ok(
                    "Vote cast successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // Temporary response class for development testing
    public static class OtpResponse {

        private String message;
        private String otp;

        public OtpResponse(
                String message,
                String otp
        ) {
            this.message = message;
            this.otp = otp;
        }

        public String getMessage() {
            return message;
        }

        public String getOtp() {
            return otp;
        }
    }
}