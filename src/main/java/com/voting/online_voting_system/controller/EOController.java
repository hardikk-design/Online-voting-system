package com.voting.online_voting_system.controller;

import com.voting.online_voting_system.dto.EOAssignmentResponse;
import com.voting.online_voting_system.dto.EODashboardResponse;
import com.voting.online_voting_system.entity.ElectionOfficerAssignment;
import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.enums.AssignmentStatus;
import com.voting.online_voting_system.repository.ElectionOfficerAssignmentRepository;
import com.voting.online_voting_system.repository.ResultRepository;
import com.voting.online_voting_system.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/election-officer")
public class EOController {

    private final ElectionOfficerAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ResultRepository resultRepository;

    public EOController(
            ElectionOfficerAssignmentRepository assignmentRepository,
            UserRepository userRepository,
            ResultRepository resultRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.resultRepository = resultRepository;
    }

    @GetMapping("/my-assignment")
    public ResponseEntity<?> getMyAssignment(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        /*
         * Current active EO assignment.
         *
         * This represents the election that the EO
         * is currently responsible for.
         */
        ElectionOfficerAssignment activeAssignment =
                assignmentRepository
                        .findByUserIdAndStatus(
                                user.getId(),
                                AssignmentStatus.ACTIVE
                        )
                        .orElse(null);

        EOAssignmentResponse currentAssignment = null;

        if (activeAssignment != null) {

            currentAssignment =
                    createAssignmentResponse(
                            activeAssignment
                    );
        }


        /*
         * Find ALL previous assignments for this EO.
         *
         * Some of these may have ended but still
         * have an unreleased result.
         */
        List<ElectionOfficerAssignment> assignments =
                assignmentRepository
                        .findByUserId(user.getId());

        List<EOAssignmentResponse> pendingResults =
                new ArrayList<>();


        for (ElectionOfficerAssignment assignment
                : assignments) {

            /*
             * We only want assignments that:
             *
             * 1. Have ENDED
             * 2. Are linked to an election
             * 3. Do not already have a released result
             */
            if (
                    assignment.getStatus()
                            == AssignmentStatus.ENDED
                            &&
                            assignment.getElection() != null
            ) {

                Long electionId =
                        assignment
                                .getElection()
                                .getId();

                boolean resultReleased =
                        resultRepository
                                .existsByElectionId(
                                        electionId
                                );

                if (!resultReleased) {

                    pendingResults.add(
                            createAssignmentResponse(
                                    assignment
                            )
                    );
                }
            }
        }


        /*
         * Return both:
         *
         * currentAssignment
         * pendingResults
         */
        EODashboardResponse response =
                new EODashboardResponse(
                        currentAssignment,
                        pendingResults
                );

        return ResponseEntity.ok(response);
    }


    /*
     * Converts an ElectionOfficerAssignment
     * entity into the DTO already used by
     * the frontend.
     */
    private EOAssignmentResponse
    createAssignmentResponse(
            ElectionOfficerAssignment assignment
    ) {

        Long electionId = null;
        String electionTitle = null;

        if (assignment.getElection() != null) {

            electionId =
                    assignment
                            .getElection()
                            .getId();

            electionTitle =
                    assignment
                            .getElection()
                            .getTitle();
        }

        return new EOAssignmentResponse(
                assignment.getId(),
                assignment.getStatus().name(),
                electionId,
                electionTitle
        );
    }
}

