package com.voting.online_voting_system.service;

import com.voting.online_voting_system.entity.Election;
import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.enums.AuditAction;
import com.voting.online_voting_system.enums.Role;
import com.voting.online_voting_system.repository.ElectionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ElectionManagementService {

    private final ElectionRepository electionRepository;
    private final AuditLogService auditLogService;

    public ElectionManagementService(
            ElectionRepository electionRepository,
            AuditLogService auditLogService
    ) {
        this.electionRepository = electionRepository;
        this.auditLogService = auditLogService;
    }

    public Election updateElectionTime(
            Long electionId,
            LocalDateTime newStartDate,
            LocalDateTime newEndDate,
            User user
    ) {

        Election election = electionRepository.findById(electionId)
                .orElseThrow(() ->
                        new RuntimeException("Election not found"));

        // Only the assigned EO should modify election time.
        // This method will need the assignment check from the controller/service
        // that calls it.

        if (user.getRole() == Role.ADMIN) {

            auditLogService.log(
                    user,
                    AuditAction.ELECTION_TIME_MODIFICATION_ATTEMPT,
                    "Admin attempted to modify election date/time"
            );

            throw new RuntimeException(
                    "Admin cannot modify election date/time"
            );
        }

        // Basic date validation
        if (newStartDate == null || newEndDate == null) {
            throw new RuntimeException(
                    "Start date and end date are required"
            );
        }

        if (!newEndDate.isAfter(newStartDate)) {

            throw new RuntimeException(
                    "End date must be after start date"
            );
        }

        // Log the modification attempt
        auditLogService.log(
                user,
                AuditAction.ELECTION_TIME_MODIFICATION_ATTEMPT,
                "Election date/time modification attempted for election ID "
                        + electionId
        );

        election.setStartDate(newStartDate);
        election.setEndDate(newEndDate);

        return electionRepository.save(election);
    }
}