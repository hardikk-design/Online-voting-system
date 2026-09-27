package com.voting.online_voting_system.controller;

import com.voting.online_voting_system.entity.ElectionOfficerAssignment;
import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.repository.UserRepository;
import com.voting.online_voting_system.service.ElectionOfficerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/election-officers")
public class ElectionOfficerController {

    private final ElectionOfficerService electionOfficerService;
    private final UserRepository userRepository;

    public ElectionOfficerController(
            ElectionOfficerService electionOfficerService,
            UserRepository userRepository
    ) {
        this.electionOfficerService = electionOfficerService;
        this.userRepository = userRepository;
    }


    // ---------------------------------------------------------
    // ADMIN ASSIGNS USER AS ELECTION OFFICER
    // ---------------------------------------------------------

    @PostMapping("/assign/{userId}")
    public ResponseEntity<?> assignOfficer(
            @PathVariable Long userId,
            Authentication authentication
    ) {

        try {

            // Get email of currently logged-in user
            String adminEmail = authentication.getName();

            // Find that user in database
            User admin = userRepository
                    .findByEmail(adminEmail)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Admin user not found"
                            ));

            // Assign selected user as EO
            ElectionOfficerAssignment assignment =
                    electionOfficerService.assignOfficer(
                            userId,
                            admin
                    );

            return ResponseEntity.ok(assignment);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // ---------------------------------------------------------
    // ADMIN ENDS ELECTION OFFICER ASSIGNMENT
    // ---------------------------------------------------------

    @PutMapping("/remove/{userId}")
    public ResponseEntity<?> removeOfficer(
            @PathVariable Long userId,
            Authentication authentication
    ) {

        try {

            // Get email of currently logged-in user
            String adminEmail = authentication.getName();

            // Find admin in database
            User admin = userRepository
                    .findByEmail(adminEmail)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Admin user not found"
                            ));

            // End EO assignment
            electionOfficerService.removeOfficer(
                    userId,
                    admin
            );

            return ResponseEntity.ok(
                    "Election Officer assignment ended successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}