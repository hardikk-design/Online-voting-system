package com.voting.online_voting_system.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EOAssignmentResponse {

    private Long assignmentId;
    private String status;
    private Long electionId;
    private String electionTitle;

    public EOAssignmentResponse() {
    }

    public EOAssignmentResponse(
            Long assignmentId,
            String status,
            Long electionId,
            String electionTitle
    ) {
        this.assignmentId = assignmentId;
        this.status = status;
        this.electionId = electionId;
        this.electionTitle = electionTitle;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getElectionId() {
        return electionId;
    }

    public void setElectionId(Long electionId) {
        this.electionId = electionId;
    }

    public String getElectionTitle() {
        return electionTitle;
    }

    public void setElectionTitle(String electionTitle) {
        this.electionTitle = electionTitle;
    }
}