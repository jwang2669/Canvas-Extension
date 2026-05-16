package com.oiloncanvas.backend.dto;

import java.util.List;

/**
 * Response wrapper for GET /api/canvas/assignments.
 * Contains a list of assignments and the date range queried.
 */
public class AssignmentsResponse {

    private List<AssignmentResponse> assignments;
    private String startDate;
    private String endDate;
    private int count;

    public AssignmentsResponse() {}

    public AssignmentsResponse(List<AssignmentResponse> assignments, String startDate, String endDate) {
        this.assignments = assignments;
        this.startDate = startDate;
        this.endDate = endDate;
        this.count = assignments != null ? assignments.size() : 0;
    }

    public List<AssignmentResponse> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<AssignmentResponse> assignments) {
        this.assignments = assignments;
        this.count = assignments != null ? assignments.size() : 0;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
