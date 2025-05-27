package com.example.groupproject;

import java.util.List;

public class BorrowRequest {
    // Main form fields
    private String dateSubmitted;
    private String department;
    private String borrowerName;
    private String gender;
    private String borrowerId;
    private String projectName;
    private String dateOfProject;
    private String timeOfProject;
    private String venue;
    private String status;

    // Optional field (only include if your database has this column)
    private String approvedBy;

    private int requestId;
    private List<Item> items;

    // Constructor for new requests (without approvedBy)
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
    }

    // Constructor for server responses (with all fields)
    public BorrowRequest(int requestId, String dateSubmitted, String department,
                         String borrowerName, String gender, String borrowerId,
                         String projectName, String dateOfProject, String timeOfProject,
                         String venue, String status, String approvedBy, List<Item> items) {
        this(dateSubmitted, department, borrowerName, gender, borrowerId, projectName,
                dateOfProject, timeOfProject, venue, status, items);
        this.requestId = requestId;
        this.approvedBy = approvedBy;
    }

    // Getters
    public String getDateSubmitted() { return dateSubmitted; }
    public String getDepartment() { return department; }
    public String getBorrowerName() { return borrowerName; }
    public String getGender() { return gender; }
    public String getBorrowerId() { return borrowerId; }
    public String getProjectName() { return projectName; }
    public String getDateOfProject() { return dateOfProject; }
    public String getTimeOfProject() { return timeOfProject; }
    public String getVenue() { return venue; }
    public String getStatus() { return status; }
    public String getApprovedBy() { return approvedBy; } // Can be null if column doesn't exist
    public int getRequestId() { return requestId; }
    public List<Item> getItems() { return items; }

    // Item class
    public static class Item {
        private String qty;
        private String description;
        private String dateOfTransfer;
        private String locationFrom;
        private String locationTo;

        public Item(String qty, String description, String dateOfTransfer,
                    String locationFrom, String locationTo) {
            this.qty = qty;
            this.description = description;
            this.dateOfTransfer = dateOfTransfer;
            this.locationFrom = locationFrom;
            this.locationTo = locationTo;
        }

        // Getters
        public String getQty() { return qty; }
        public String getDescription() { return description; }
        public String getDateOfTransfer() { return dateOfTransfer; }
        public String getLocationFrom() { return locationFrom; }
        public String getLocationTo() { return locationTo; }
    }
}