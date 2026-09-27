package com.voting.online_voting_system.service;

import com.voting.online_voting_system.dto.NominationRequest;
import com.voting.online_voting_system.entity.Candidate;
import com.voting.online_voting_system.entity.Election;
import com.voting.online_voting_system.entity.ElectionOfficerAssignment;
import com.voting.online_voting_system.entity.EligibilityCriteria;
import com.voting.online_voting_system.entity.Nomination;
import com.voting.online_voting_system.entity.Notification;
import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.enums.AssignmentStatus;
import com.voting.online_voting_system.enums.ElectionStatus;
import com.voting.online_voting_system.enums.NominationStatus;
import com.voting.online_voting_system.enums.NotificationType;
import com.voting.online_voting_system.repository.CandidateRepository;
import com.voting.online_voting_system.repository.ElectionOfficerAssignmentRepository;
import com.voting.online_voting_system.repository.ElectionRepository;
import com.voting.online_voting_system.repository.EligibilityCriteriaRepository;
import com.voting.online_voting_system.repository.NominationRepository;
import com.voting.online_voting_system.repository.NotificationRepository;
import com.voting.online_voting_system.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NominationService {

    private final NominationRepository nominationRepository;
    private final CandidateRepository candidateRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ElectionRepository electionRepository;
    private final EligibilityCriteriaRepository eligibilityCriteriaRepository;
    private final ElectionOfficerAssignmentRepository assignmentRepository;

    public NominationService(
            NominationRepository nominationRepository,
            CandidateRepository candidateRepository,
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            ElectionRepository electionRepository,
            EligibilityCriteriaRepository eligibilityCriteriaRepository,
            ElectionOfficerAssignmentRepository assignmentRepository) {

        this.nominationRepository = nominationRepository;
        this.candidateRepository = candidateRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.electionRepository = electionRepository;
        this.eligibilityCriteriaRepository = eligibilityCriteriaRepository;
        this.assignmentRepository = assignmentRepository;
    }

    // ------------------------------------------------
    // VOTER SUBMITS NOMINATION
    // ------------------------------------------------

    public Nomination submitNomination(
            NominationRequest request) {

        // Get currently logged-in user from JWT
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));

        // Find election
        Election election =
                electionRepository.findById(
                        request.getElectionId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Election not found"
                        ));

        // ------------------------------------------------
        // CHECK ELECTION STATUS
        // Nomination is allowed ONLY for upcoming elections
        // ------------------------------------------------

        if (election.getStatus() != ElectionStatus.UPCOMING) {

            throw new RuntimeException(
                    "Nomination is allowed only for upcoming elections"
            );
        }

        // ------------------------------------------------
        // CHECK VOTER ELIGIBILITY
        // ------------------------------------------------

        EligibilityCriteria criteria =
                eligibilityCriteriaRepository
                        .findByElectionId(election.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Eligibility criteria not found"
                                ));

        boolean departmentMatches =
                criteria.getDepartment() == null ||
                        criteria.getDepartment().isBlank() ||
                        (
                                user.getDepartment() != null &&
                                        user.getDepartment()
                                                .equalsIgnoreCase(
                                                        criteria.getDepartment()
                                                )
                        );

        boolean courseMatches =
                criteria.getCourse() == null ||
                        criteria.getCourse().isBlank() ||
                        (
                                user.getCourse() != null &&
                                        user.getCourse()
                                                .equalsIgnoreCase(
                                                        criteria.getCourse()
                                                )
                        );

        boolean yearMatches =
                criteria.getYear() == null ||
                        (
                                user.getYear() != null &&
                                        user.getYear()
                                                .equals(criteria.getYear())
                        );

        if (!departmentMatches ||
                !courseMatches ||
                !yearMatches) {

            throw new RuntimeException(
                    "You are not eligible for this election"
            );
        }

        // ------------------------------------------------
        // CHECK DUPLICATE NOMINATION
        // ------------------------------------------------

        if (nominationRepository
                .findByUserIdAndElectionId(
                        user.getId(),
                        election.getId()
                )
                .isPresent()) {

            throw new RuntimeException(
                    "You have already submitted a nomination for this election"
            );
        }

        // ------------------------------------------------
        // CREATE NOMINATION
        // ------------------------------------------------

        Nomination nomination =
                new Nomination();

        nomination.setUser(user);

        nomination.setElection(election);

        // Position comes from Election → Post
        nomination.setPosition(
                election.getPost().getName()
        );

        nomination.setStatus(
                NominationStatus.PENDING
        );

        nomination.setSubmittedAt(
                LocalDateTime.now()
        );

        return nominationRepository.save(nomination);
    }

    // ------------------------------------------------
    // EO GETS PENDING NOMINATIONS
    // ------------------------------------------------

    public List<Nomination> getPendingNominations(
            Long electionId) {

        // Get currently logged-in EO
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User officer =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Officer not found"
                                ));

        // Check that this EO is assigned to this election
        checkOfficerAssignment(
                officer.getId(),
                electionId
        );

        return nominationRepository
                .findByElectionIdAndStatus(
                        electionId,
                        NominationStatus.PENDING
                );
    }

    // ------------------------------------------------
    // EO APPROVES NOMINATION
    // ------------------------------------------------

    @Transactional
    public Nomination approveNomination(
            Long nominationId) {

        // Get currently logged-in EO
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User officer =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Officer not found"
                                ));

        // Find nomination
        Nomination nomination =
                nominationRepository.findById(
                        nominationId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Nomination not found"
                        ));

        // Check EO is assigned to this election
        checkOfficerAssignment(
                officer.getId(),
                nomination.getElection().getId()
        );

        // Make sure nomination is still pending
        if (nomination.getStatus()
                != NominationStatus.PENDING) {

            throw new RuntimeException(
                    "Nomination has already been reviewed"
            );
        }

        // ------------------------------------------------
        // UPDATE NOMINATION
        // ------------------------------------------------

        nomination.setStatus(
                NominationStatus.APPROVED
        );

        nomination.setReviewedBy(officer);

        nomination.setReviewedAt(
                LocalDateTime.now()
        );

        Nomination savedNomination =
                nominationRepository.save(nomination);

        // ------------------------------------------------
        // CREATE CANDIDATE
        // ------------------------------------------------

        Candidate candidate =
                new Candidate();

        candidate.setUser(
                nomination.getUser()
        );

        candidate.setElection(
                nomination.getElection()
        );

        candidate.setNomination(
                nomination
        );

        candidate.setApprovedAt(
                LocalDateTime.now()
        );

        candidateRepository.save(candidate);

        // ------------------------------------------------
        // CREATE APPROVAL NOTIFICATION
        // ------------------------------------------------

        Notification notification =
                new Notification();

        notification.setUser(
                nomination.getUser()
        );

        notification.setType(
                NotificationType.NOMINATION_APPROVED
        );

        notification.setMessage(
                "Your nomination for "
                        + nomination.getPosition()
                        + " has been approved. "
                        + "You are now a candidate."
        );

        notification.setRead(false);

        notification.setCreatedAt(
                LocalDateTime.now()
        );

        notificationRepository.save(notification);

        return savedNomination;
    }

    // ------------------------------------------------
    // EO REJECTS NOMINATION
    // ------------------------------------------------

    @Transactional
    public Nomination rejectNomination(
            Long nominationId) {

        // Get currently logged-in EO
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User officer =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Officer not found"
                                ));

        // Find nomination
        Nomination nomination =
                nominationRepository.findById(
                        nominationId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Nomination not found"
                        ));

        // Check EO is assigned to this election
        checkOfficerAssignment(
                officer.getId(),
                nomination.getElection().getId()
        );

        // Make sure nomination is still pending
        if (nomination.getStatus()
                != NominationStatus.PENDING) {

            throw new RuntimeException(
                    "Nomination has already been reviewed"
            );
        }

        // ------------------------------------------------
        // UPDATE NOMINATION
        // ------------------------------------------------

        nomination.setStatus(
                NominationStatus.REJECTED
        );

        nomination.setReviewedBy(officer);

        nomination.setReviewedAt(
                LocalDateTime.now()
        );

        Nomination savedNomination =
                nominationRepository.save(nomination);

        // ------------------------------------------------
        // CREATE REJECTION NOTIFICATION
        // ------------------------------------------------

        Notification notification =
                new Notification();

        notification.setUser(
                nomination.getUser()
        );

        notification.setType(
                NotificationType.NOMINATION_REJECTED
        );

        notification.setMessage(
                "Your nomination for "
                        + nomination.getPosition()
                        + " has been rejected."
        );

        notification.setRead(false);

        notification.setCreatedAt(
                LocalDateTime.now()
        );

        notificationRepository.save(notification);

        return savedNomination;
    }

    // ------------------------------------------------
    // CHECK WHETHER EO IS ASSIGNED TO ELECTION
    // ------------------------------------------------

    private void checkOfficerAssignment(
            Long officerId,
            Long electionId) {

        ElectionOfficerAssignment assignment =
                assignmentRepository
                        .findByUserIdAndElectionIdAndStatus(
                                officerId,
                                electionId,
                                AssignmentStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not assigned as Election Officer for this election"
                                )
                        );
    }
}