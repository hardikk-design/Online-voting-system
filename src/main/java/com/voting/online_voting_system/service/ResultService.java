package com.voting.online_voting_system.service;

import com.voting.online_voting_system.entity.*;
import com.voting.online_voting_system.enums.AuditAction;
import com.voting.online_voting_system.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResultService {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final BallotRepository ballotRepository;
    private final ResultRepository resultRepository;
    private final ResultVoteCountRepository resultVoteCountRepository;
    private final ElectionOfficerAssignmentRepository assignmentRepository;
    private final VoteEncryptionService voteEncryptionService;
    private final AuditLogService auditLogService;

    public ResultService(
            ElectionRepository electionRepository,
            CandidateRepository candidateRepository,
            BallotRepository ballotRepository,
            ResultRepository resultRepository,
            ResultVoteCountRepository resultVoteCountRepository,
            ElectionOfficerAssignmentRepository assignmentRepository,
            VoteEncryptionService voteEncryptionService,
            AuditLogService auditLogService
    ) {
        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
        this.ballotRepository = ballotRepository;
        this.resultRepository = resultRepository;
        this.resultVoteCountRepository = resultVoteCountRepository;
        this.assignmentRepository = assignmentRepository;
        this.voteEncryptionService = voteEncryptionService;
        this.auditLogService = auditLogService;
    }

    // =========================================================
    // RELEASE RESULT
    // =========================================================

    @Transactional
    public Result releaseResult(
            Long electionId,
            User eo
    ) {

        // 1. Find election
        Election election =
                electionRepository.findById(electionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Election not found"
                                )
                        );

        // 2. Election must have ended
        if (LocalDateTime.now()
                .isBefore(election.getEndDate())) {

            throw new RuntimeException(
                    "Election is still active. Result can be announced only after the election ends."
            );
        }

        // 3. Result must not already exist
        if (resultRepository.existsByElectionId(electionId)) {

            throw new RuntimeException(
                    "Result has already been announced for this election."
            );
        }

        // 4. Check whether this user was assigned
        // as Election Officer for this election.
        //
        // We do NOT check ACTIVE here because the scheduler
        // changes the assignment to ENDED after the election.

        boolean wasAssigned =
                assignmentRepository
                        .findByUserIdAndElectionId(
                                eo.getId(),
                                electionId
                        )
                        .isPresent();

        if (!wasAssigned) {

            auditLogService.log(
                    eo,
                    AuditAction.UNAUTHORIZED_RESULT_RELEASE,
                    "User attempted to announce the result of election ID: "
                            + electionId
                            + " without being the assigned Election Officer."
            );

            throw new RuntimeException(
                    "You were not the assigned Election Officer for this election."
            );
        }

        // 5. Get candidates
        List<Candidate> candidates =
                candidateRepository.findByElectionId(
                        electionId
                );

        if (candidates.isEmpty()) {

            throw new RuntimeException(
                    "No candidates found for this election."
            );
        }

        // 6. Get all ballots
        List<Ballot> ballots =
                ballotRepository.findByElectionId(
                        electionId
                );

        // 7. Start every candidate with 0 votes
        Map<Long, Integer> voteCounts =
                new HashMap<>();

        for (Candidate candidate : candidates) {

            voteCounts.put(
                    candidate.getId(),
                    0
            );
        }

        // 8. Decrypt ballots and count votes
        for (Ballot ballot : ballots) {

            Long candidateId =
                    voteEncryptionService.decrypt(
                            ballot.getEncryptedVote()
                    );

            if (voteCounts.containsKey(candidateId)) {

                voteCounts.put(
                        candidateId,
                        voteCounts.get(candidateId) + 1
                );
            }
        }

        // 9. Find highest vote count
        int highestVotes = -1;

        for (Candidate candidate : candidates) {

            int votes =
                    voteCounts.get(
                            candidate.getId()
                    );

            if (votes > highestVotes) {

                highestVotes = votes;
            }
        }

        // 10. Find winner / detect tie
        Candidate winner = null;

        int candidatesWithHighestVotes = 0;

        for (Candidate candidate : candidates) {

            int votes =
                    voteCounts.get(
                            candidate.getId()
                    );

            if (votes == highestVotes) {

                candidatesWithHighestVotes++;

                winner = candidate;
            }
        }

        // If multiple candidates have the same highest
        // number of votes, there is no single winner.
        if (candidatesWithHighestVotes > 1) {

            winner = null;
        }

        // 11. Create Result
        Result result = new Result();

        result.setElection(election);
        result.setWinner(winner);
        result.setReleasedBy(eo);
        result.setReleasedAt(
                LocalDateTime.now()
        );

        // 12. Save Result
        result =
                resultRepository.save(result);

        // 13. Save vote counts
        for (Candidate candidate : candidates) {

            ResultVoteCount resultVoteCount =
                    new ResultVoteCount();

            resultVoteCount.setResult(result);

            resultVoteCount.setCandidate(
                    candidate
            );

            resultVoteCount.setVoteCount(
                    voteCounts.get(
                            candidate.getId()
                    )
            );

            resultVoteCountRepository.save(
                    resultVoteCount
            );
        }

        return result;
    }

    // =========================================================
    // GET RESULT
    // =========================================================

    public List<ResultVoteCount> getResult(
            Long electionId
    ) {

        Result result =
                resultRepository
                        .findByElectionId(electionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Result has not been announced yet."
                                )
                        );

        return resultVoteCountRepository
                .findByResultId(
                        result.getId()
                );
    }
}