package com.voting.online_voting_system.service;

import com.voting.online_voting_system.entity.ElectionOfficerAssignment;
import com.voting.online_voting_system.entity.Notification;
import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.enums.AssignmentStatus;
import com.voting.online_voting_system.enums.AuditAction;
import com.voting.online_voting_system.enums.NotificationType;
import com.voting.online_voting_system.enums.Role;
import com.voting.online_voting_system.repository.ElectionOfficerAssignmentRepository;
import com.voting.online_voting_system.repository.NotificationRepository;
import com.voting.online_voting_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ElectionOfficerService {

    private final ElectionOfficerAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;

    public ElectionOfficerService(
            ElectionOfficerAssignmentRepository assignmentRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            AuditLogService auditLogService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogService = auditLogService;
    }

    // ---------------------------------------------------------
    // ADMIN ASSIGNS A USER AS ELECTION OFFICER
    // ---------------------------------------------------------

    public ElectionOfficerAssignment assignOfficer(
            Long userId,
            User admin
    ) {

        // Only ADMIN can assign an Election Officer
        if (admin.getRole() != Role.ADMIN) {

            auditLogService.log(
                    admin,
                    AuditAction.UNAUTHORIZED_ELECTION_ACTION,
                    "Unauthorized attempt to assign an Election Officer"
            );

            throw new RuntimeException(
                    "Only Admin can assign Election Officers"
            );
        }

        // Find the user who is being assigned
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Admin cannot assign another Admin as EO
        if (user.getRole() == Role.ADMIN) {

            throw new RuntimeException(
                    "Admin cannot be assigned as Election Officer"
            );
        }

        // Check whether the user is already an ACTIVE EO
        if (assignmentRepository
                .findByUserIdAndStatus(
                        userId,
                        AssignmentStatus.ACTIVE
                )
                .isPresent()) {

            throw new RuntimeException(
                    "User is already an active Election Officer"
            );
        }

        // Create EO assignment
        ElectionOfficerAssignment assignment =
                new ElectionOfficerAssignment();

        assignment.setUser(user);

        // No election yet.
        // EO will create the election later.
        assignment.setElection(null);

        assignment.setAssignedBy(admin);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStatus(AssignmentStatus.ACTIVE);

        ElectionOfficerAssignment savedAssignment =
                assignmentRepository.save(assignment);

        // ---------------------------------------------------------
        // CREATE EO ASSIGNMENT NOTIFICATION
        // ---------------------------------------------------------

        Notification notification =
                new Notification();

        notification.setUser(user);
        notification.setType(NotificationType.EO_ASSIGNED);
        notification.setMessage(
                "You have been assigned as an Election Officer."
        );
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);

        return savedAssignment;
    }


    // ---------------------------------------------------------
    // ADMIN ENDS AN EO ASSIGNMENT
    // ---------------------------------------------------------

    public void removeOfficer(
            Long userId,
            User admin
    ) {

        // Only ADMIN can remove/end an EO assignment
        if (admin.getRole() != Role.ADMIN) {

            auditLogService.log(
                    admin,
                    AuditAction.UNAUTHORIZED_ELECTION_ACTION,
                    "Unauthorized attempt to remove an Election Officer"
            );

            throw new RuntimeException(
                    "Only Admin can remove Election Officers"
            );
        }

        // Find active EO assignment
        ElectionOfficerAssignment assignment =
                assignmentRepository
                        .findByUserIdAndStatus(
                                userId,
                                AssignmentStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Active Election Officer assignment not found"
                                ));

        // End the assignment
        assignment.setStatus(AssignmentStatus.ENDED);

        assignmentRepository.save(assignment);

        // Audit log
        auditLogService.log(
                admin,
                AuditAction.EO_REMOVED,
                "Election Officer assignment ended for user ID "
                        + userId
        );
    }
}