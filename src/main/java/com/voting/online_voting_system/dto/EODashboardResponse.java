
        package com.voting.online_voting_system.dto;

import java.util.List;

public class EODashboardResponse {

    private EOAssignmentResponse currentAssignment;

    private List<EOAssignmentResponse> pendingResults;

    public EODashboardResponse() {
    }

    public EODashboardResponse(
            EOAssignmentResponse currentAssignment,
            List<EOAssignmentResponse> pendingResults
    ) {
        this.currentAssignment = currentAssignment;
        this.pendingResults = pendingResults;
    }

    public EOAssignmentResponse getCurrentAssignment() {
        return currentAssignment;
    }

    public void setCurrentAssignment(
            EOAssignmentResponse currentAssignment
    ) {
        this.currentAssignment = currentAssignment;
    }

    public List<EOAssignmentResponse> getPendingResults() {
        return pendingResults;
    }

    public void setPendingResults(
            List<EOAssignmentResponse> pendingResults
    ) {
        this.pendingResults = pendingResults;
    }
}
