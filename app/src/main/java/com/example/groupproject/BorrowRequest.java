package com.example.groupproject;

import java.util.List;

public class BorrowRequest {
    // Main form fields (sent by client for new requests) - Standardized to camelCase
    public String dateSubmitted;
    public String department;
    public String borrowerName;
    public String gender;
    public String borrowerId; // Using deviceId as a placeholder for borrowerId
    public String projectName;
    public String dateOfProject;
    public String timeOfProject;
    public String venue;

    // Fields usually managed by the server or updated after submission
    public String status;      // e.g., "Pending", "Approved", "Rejected"
    public String approvedBy; // New field for the person who approved it
    public int requestId;     // New field for the ID from the database

    private List<Item> items;

    // Constructor for creating a NEW request (client-side submission)
    // This matches the 10 String + 1 List<Item> expected by BorrowActivity
    public BorrowRequest(String dateSubmitted, String department, String borrowerName,
                         String gender, String borrowerId, String projectName,
                         String dateOfProject, String timeOfProject, String venue,
                         String status, List<Item> items) {
        this.dateSubmitted = dateSubmitted;
        this.department = department;
        this.borrowerName = borrowerName;
        this.gender = gender;
        this.borrowerId = borrowerId;
        this.projectName = projectName;
        this.dateOfProject = dateOfProject;
        this.timeOfProject = timeOfProject;
        this.venue = venue;
        this.status = status;
        this.items = items;
        // requestId and approvedBy are not set here as they come from the server response
    }

    // Constructor for receiving a request (e.g., from server API, where ID and approval might be present)
    // This constructor should be used by Gson when deserializing JSON from the server.
    // Ensure all fields that might come from the server are included here.
    public BorrowRequest(int requestId, String dateSubmitted, String department, String borrowerName,
                         String gender, String borrowerId, String projectName,
                         String dateOfProject, String timeOfProject, String venue,
                         String status, String approvedBy, List<Item> items) {
        // Call the other constructor for common fields
        this(dateSubmitted, department, borrowerName, gender, borrowerId, projectName,
                dateOfProject, timeOfProject, venue, status, items);
        this.requestId = requestId;
        this.approvedBy = approvedBy;
    }


    // Inner class for items (fields remain camelCase)
    public static class Item {
        public String qty;
        public String description;
        public String dateOfTransfer;
        public String locationFrom;
        public String locationTo;

        public Item(String qty, String description, String dateOfTransfer,
                    String locationFrom, String locationTo) {
            this.qty = qty;
            this.description = description;
            this.dateOfTransfer = dateOfTransfer;
            this.locationFrom = locationFrom;
            this.locationTo = locationTo;
        }

        // Getters for Item fields
        public String getQty() { return qty; }
        public String getDescription() { return description; }
        public String getDateOfTransfer() { return dateOfTransfer; }
        public String getLocationFrom() { return locationFrom; }
        public String getLocationTo() { return locationTo; }
    }

    // --- Getters for BorrowRequest fields (all standardized to camelCase) ---
    public String getDateSubmitted() {
        return dateSubmitted;
    }

    public String getDepartment() {
        return department;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public String getGender() {
        return gender;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getDateOfProject() {
        return dateOfProject;
    }

    public String getTimeOfProject() {
        return timeOfProject;
    }

    public String getVenue() {
        return venue;
    }

    public String getStatus() {
        return status;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public int getRequestId() {
        return requestId;
    }

    public List<Item> getItems() {
        return items;
    }

    // Optional: Setters if you need to modify fields after creation (e.g., updating status)
    public void setStatus(String status) {
        this.status = status;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
}