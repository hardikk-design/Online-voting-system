package com.voting.online_voting_system.service;

import com.voting.online_voting_system.dto.CreateElectionRequest;
import com.voting.online_voting_system.entity.Election;
import com.voting.online_voting_system.entity.ElectionOfficerAssignment;
import com.voting.online_voting_system.entity.EligibilityCriteria;
import com.voting.online_voting_system.entity.Post;
import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.enums.AssignmentStatus;
import com.voting.online_voting_system.enums.ElectionStatus;
import com.voting.online_voting_system.repository.ElectionOfficerAssignmentRepository;
import com.voting.online_voting_system.repository.ElectionRepository;
import com.voting.online_voting_system.repository.EligibilityCriteriaRepository;
import com.voting.online_voting_system.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final PostRepository postRepository;
    private final ElectionOfficerAssignmentRepository assignmentRepository;
    private final EligibilityCriteriaRepository eligibilityCriteriaRepository;

    public ElectionService(
            ElectionRepository electionRepository,
            PostRepository postRepository,
            ElectionOfficerAssignmentRepository assignmentRepository,
            EligibilityCriteriaRepository eligibilityCriteriaRepository
    ) {
        this.electionRepository = electionRepository;
        this.postRepository = postRepository;
        this.assignmentRepository = assignmentRepository;
        this.eligibilityCriteriaRepository = eligibilityCriteriaRepository;
    }

    @Transactional
    public Election createElection(
            CreateElectionRequest request,
            User eo
    ) {

        // Find the active Election Officer assignment
        ElectionOfficerAssignment assignment =
                assignmentRepository
                        .findByUserIdAndStatus(
                                eo.getId(),
                                AssignmentStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not an active Election Officer"
                                )
                        );

        // One active EO assignment can create only ONE election
        if (assignment.getElection() != null) {
            throw new RuntimeException(
                    "You already have an election assigned to you. " +
                            "You cannot create another election until this assignment ends."
            );
        }

        // Find the selected post
        Post post =
                postRepository.findById(request.getPostId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Post not found"
                                )
                        );

        // Validate dates
        if (request.getStartDate() == null ||
                request.getEndDate() == null) {

            throw new RuntimeException(
                    "Start date and end date are required"
            );
        }

        if (!request.getEndDate()
                .isAfter(request.getStartDate())) {

            throw new RuntimeException(
                    "End date must be after start date"
            );
        }

        // Eligibility criteria is required
        if (request.getEligibility() == null) {

            throw new RuntimeException(
                    "Eligibility criteria is required"
            );
        }

        // Create election
        Election election = new Election();

        election.setTitle(request.getTitle());
        election.setDescription(request.getDescription());
        election.setPost(post);
        election.setDepartment(request.getDepartment());
        election.setCreatedBy(eo);
        election.setStartDate(request.getStartDate());
        election.setEndDate(request.getEndDate());
        election.setStatus(ElectionStatus.UPCOMING);
        election.setCreatedAt(LocalDateTime.now());

        Election savedElection =
                electionRepository.save(election);

        // Create eligibility criteria
        EligibilityCriteria criteria =
                new EligibilityCriteria();

        criteria.setElection(savedElection);

        criteria.setDepartment(
                request.getEligibility().getDepartment()
        );

        criteria.setCourse(
                request.getEligibility().getCourse()
        );

        criteria.setYear(
                request.getEligibility().getYear()
        );

        eligibilityCriteriaRepository.save(criteria);

        // Connect EO assignment with this election
        assignment.setElection(savedElection);
        assignmentRepository.save(assignment);

        return savedElection;
    }

    public List<Election> getEligibleElections(User user) {

        List<Election> allElections =
                electionRepository.findAll();

        List<Election> eligibleElections =
                new ArrayList<>();

        for (Election election : allElections) {

            /*
             * IMPORTANT:
             * Do NOT filter based on election status here.
             *
             * Eligible users should be able to see:
             * UPCOMING
             * ONGOING
             * COMPLETED
             *
             * Status will determine what action
             * the user can perform.
             */

            EligibilityCriteria criteria =
                    eligibilityCriteriaRepository
                            .findByElectionId(election.getId())
                            .orElse(null);

            if (criteria == null) {
                continue;
            }

            // Department eligibility
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

            // Course eligibility
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

            // Year eligibility
            boolean yearMatches =
                    criteria.getYear() == null ||
                            (
                                    user.getYear() != null &&
                                            user.getYear()
                                                    .equals(criteria.getYear())
                            );

            // Add election if ALL specified criteria match
            if (departmentMatches &&
                    courseMatches &&
                    yearMatches) {

                eligibleElections.add(election);
            }
        }

        return eligibleElections;
    }
}