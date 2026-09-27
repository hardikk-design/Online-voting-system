package com.voting.online_voting_system.service;

import com.voting.online_voting_system.dto.OtpVerificationRequest;
import com.voting.online_voting_system.dto.VoteRequest;
import com.voting.online_voting_system.entity.*;
import com.voting.online_voting_system.enums.AuditAction;
import com.voting.online_voting_system.enums.ElectionStatus;
import com.voting.online_voting_system.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VotingService {

    private final UserRepository userRepository;
    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final VoteParticipationRepository participationRepository;
    private final BallotRepository ballotRepository;
    private final VotingChallengeRepository challengeRepository;
    private final EligibilityCriteriaRepository eligibilityCriteriaRepository;

    private final OtpService otpService;
    private final VoteEncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public VotingService(
            UserRepository userRepository,
            ElectionRepository electionRepository,
            CandidateRepository candidateRepository,
            VoteParticipationRepository participationRepository,
            BallotRepository ballotRepository,
            VotingChallengeRepository challengeRepository,
            EligibilityCriteriaRepository eligibilityCriteriaRepository,
            OtpService otpService,
            VoteEncryptionService encryptionService,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
        this.participationRepository = participationRepository;
        this.ballotRepository = ballotRepository;
        this.challengeRepository = challengeRepository;
        this.eligibilityCriteriaRepository = eligibilityCriteriaRepository;
        this.otpService = otpService;
        this.encryptionService = encryptionService;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));
    }

    // =========================================================
    // REQUEST OTP
    // =========================================================

    public String requestOtp(Long electionId) {

        User user = getCurrentUser();

        Election election =
                electionRepository.findById(electionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Election not found"
                                ));

        validateVotingEligibility(
                user,
                election
        );

        if (participationRepository
                .existsByVoterIdAndElectionId(
                        user.getId(),
                        electionId
                )) {

            auditLogService.log(
                    user,
                    AuditAction.DUPLICATE_VOTE_ATTEMPT,
                    "User attempted to vote more than once in election ID: "
                            + electionId
            );

            throw new RuntimeException(
                    "You have already voted in this election"
            );
        }

        // TEMPORARY DEVELOPMENT MODE
        // OTP is generated and stored,
        // but NOT sent through email.
        return otpService.generateAndSendOtp(
                user,
                election
        );
    }

    // =========================================================
    // VERIFY OTP
    // =========================================================

    public void verifyOtp(
            OtpVerificationRequest request
    ) {

        User user = getCurrentUser();

        VotingChallenge challenge =
                challengeRepository
                        .findTopByUserIdAndElectionIdOrderByCreatedAtDesc(
                                user.getId(),
                                request.getElectionId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "OTP request not found"
                                ));

        if (challenge.isUsed()) {

            throw new RuntimeException(
                    "This OTP has already been used"
            );
        }

        if (challenge.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "OTP has expired"
            );
        }

        if (challenge.getAttempts() >= 5) {

            throw new RuntimeException(
                    "Maximum OTP attempts exceeded"
            );
        }

        challenge.setAttempts(
                challenge.getAttempts() + 1
        );

        if (!passwordEncoder.matches(
                request.getOtp(),
                challenge.getOtpHash()
        )) {

            challengeRepository.save(challenge);

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        challenge.setVerified(true);

        challengeRepository.save(challenge);
    }

    // =========================================================
    // CAST VOTE
    // =========================================================

    @Transactional
    public void castVote(
            VoteRequest request
    ) {

        User user = getCurrentUser();

        Election election =
                electionRepository.findById(
                                request.getElectionId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Election not found"
                                ));

        validateVotingEligibility(
                user,
                election
        );

        if (participationRepository
                .existsByVoterIdAndElectionId(
                        user.getId(),
                        election.getId()
                )) {

            auditLogService.log(
                    user,
                    AuditAction.DUPLICATE_VOTE_ATTEMPT,
                    "User attempted to vote more than once in election ID: "
                            + election.getId()
            );

            throw new RuntimeException(
                    "You have already voted in this election"
            );
        }

        VotingChallenge challenge =
                challengeRepository
                        .findTopByUserIdAndElectionIdOrderByCreatedAtDesc(
                                user.getId(),
                                election.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "OTP verification required"
                                ));

        if (!challenge.isVerified()) {

            throw new RuntimeException(
                    "Please verify OTP first"
            );
        }

        if (challenge.isUsed()) {

            throw new RuntimeException(
                    "This voting verification has already been used"
            );
        }

        if (challenge.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "OTP has expired"
            );
        }

        Candidate candidate =
                candidateRepository.findById(
                                request.getCandidateId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Candidate not found"
                                ));

        if (!candidate.getElection()
                .getId()
                .equals(election.getId())) {

            throw new RuntimeException(
                    "Candidate does not belong to this election"
            );
        }

        String encryptedVote =
                encryptionService.encrypt(
                        candidate.getId()
                );

        Ballot ballot = new Ballot();

        ballot.setElection(election);
        ballot.setEncryptedVote(encryptedVote);
        ballot.setCastAt(LocalDateTime.now());

        ballotRepository.save(ballot);

        VoteParticipation participation =
                new VoteParticipation();

        participation.setVoter(user);
        participation.setElection(election);
        participation.setVotedAt(
                LocalDateTime.now()
        );

        try {

            participationRepository.save(
                    participation
            );

        } catch (DataIntegrityViolationException e) {

            auditLogService.log(
                    user,
                    AuditAction.DUPLICATE_VOTE_ATTEMPT,
                    "User attempted to vote more than once in election ID: "
                            + election.getId()
            );

            throw new RuntimeException(
                    "You have already voted in this election"
            );
        }

        challenge.setUsed(true);

        challengeRepository.save(challenge);
    }

    // =========================================================
    // VOTING ELIGIBILITY
    // =========================================================

    private void validateVotingEligibility(
            User user,
            Election election
    ) {

        if (election.getStatus()
                != ElectionStatus.ONGOING) {

            throw new RuntimeException(
                    "Election is not currently active"
            );
        }

        EligibilityCriteria criteria =
                eligibilityCriteriaRepository
                        .findByElectionId(election.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Eligibility criteria not found for this election"
                                ));

        if (criteria.getDepartment() != null
                && !criteria.getDepartment()
                .equalsIgnoreCase(user.getDepartment())) {

            throw new RuntimeException(
                    "You are not eligible for this election"
            );
        }

        if (criteria.getCourse() != null
                && !criteria.getCourse()
                .equalsIgnoreCase(user.getCourse())) {

            throw new RuntimeException(
                    "You are not eligible for this election"
            );
        }

        if (criteria.getYear() != null
                && !criteria.getYear()
                .equals(user.getYear())) {

            throw new RuntimeException(
                    "You are not eligible for this election"
            );
        }
    }
}