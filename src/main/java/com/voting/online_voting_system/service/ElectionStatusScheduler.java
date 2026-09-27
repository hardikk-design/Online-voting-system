package com.voting.online_voting_system.service;

import com.voting.online_voting_system.entity.Election;
import com.voting.online_voting_system.entity.ElectionOfficerAssignment;
import com.voting.online_voting_system.enums.AssignmentStatus;
import com.voting.online_voting_system.enums.ElectionStatus;
import com.voting.online_voting_system.repository.ElectionOfficerAssignmentRepository;
import com.voting.online_voting_system.repository.ElectionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ElectionStatusScheduler {

    private final ElectionRepository electionRepository;
    private final ElectionOfficerAssignmentRepository assignmentRepository;

    public ElectionStatusScheduler(
            ElectionRepository electionRepository,
            ElectionOfficerAssignmentRepository assignmentRepository) {

        this.electionRepository = electionRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateElectionStatuses() {

        LocalDateTime now = LocalDateTime.now();

        System.out.println("----------------------------------------");
        System.out.println("SCHEDULER RUNNING");
        System.out.println("JAVA TIMEZONE: " + ZoneId.systemDefault());
        System.out.println("JAVA TIME: " + now);
        System.out.println("----------------------------------------");

        // --------------------------------------------------
        // 1. Check UPCOMING elections
        // --------------------------------------------------

        List<Election> upcomingElections =
                electionRepository.findByStatus(
                        ElectionStatus.UPCOMING
                );

        System.out.println(
                "UPCOMING ELECTIONS FOUND: "
                        + upcomingElections.size()
        );

        for (Election election : upcomingElections) {

            System.out.println("----------------------------------------");
            System.out.println(
                    "Election ID: " + election.getId()
            );
            System.out.println(
                    "Election title: " + election.getTitle()
            );
            System.out.println(
                    "Start date: " + election.getStartDate()
            );
            System.out.println(
                    "End date: " + election.getEndDate()
            );
            System.out.println(
                    "Current time: " + now
            );

            if (!now.isBefore(election.getStartDate())) {

                election.setStatus(ElectionStatus.ONGOING);

                electionRepository.save(election);

                System.out.println(
                        ">>> Election changed UPCOMING -> ONGOING"
                );

            } else {

                System.out.println(
                        ">>> Election has not started yet"
                );
            }
        }

        // --------------------------------------------------
        // 2. Check ONGOING elections
        // --------------------------------------------------

        List<Election> ongoingElections =
                electionRepository.findByStatus(
                        ElectionStatus.ONGOING
                );

        System.out.println(
                "ONGOING ELECTIONS FOUND: "
                        + ongoingElections.size()
        );

        for (Election election : ongoingElections) {

            System.out.println("----------------------------------------");
            System.out.println(
                    "Checking ONGOING election ID: "
                            + election.getId()
            );
            System.out.println(
                    "End date: " + election.getEndDate()
            );
            System.out.println(
                    "Current time: " + now
            );

            if (!now.isBefore(election.getEndDate())) {

                election.setStatus(ElectionStatus.COMPLETED);

                electionRepository.save(election);

                System.out.println(
                        ">>> Election changed ONGOING -> COMPLETED"
                );

                // --------------------------------------------------
                // 3. End EO assignment
                // --------------------------------------------------

                ElectionOfficerAssignment assignment =
                        assignmentRepository
                                .findByElectionIdAndStatus(
                                        election.getId(),
                                        AssignmentStatus.ACTIVE
                                )
                                .orElse(null);

                if (assignment != null) {

                    assignment.setStatus(
                            AssignmentStatus.ENDED
                    );

                    assignmentRepository.save(assignment);

                    System.out.println(
                            ">>> EO assignment ID "
                                    + assignment.getId()
                                    + " changed to ENDED"
                    );
                }
            }
        }

        System.out.println("----------------------------------------");
        System.out.println("SCHEDULER FINISHED");
        System.out.println("----------------------------------------");
    }
}