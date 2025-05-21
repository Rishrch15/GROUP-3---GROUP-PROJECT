package com.example.groupproject;

import java.util.List;

public class BorrowRequest {
    public String todayDate;
    public String department;
    public String borrowerName;
    public String gender;
    public String projectName;
    public String dateNeeded;
    public String time;
    public String venue;
    public List<Item> items;

    public BorrowRequest(String todayDate, String department, String borrowerName,
                         String gender, String projectName, String dateNeeded,
                         String time, String venue, List<Item> items) {
        this.todayDate = todayDate;
        this.department = department;
        this.borrowerName = borrowerName;
        this.gender = gender;
        this.projectName = projectName;
        this.dateNeeded = dateNeeded;
        this.time = time;
        this.venue = venue;
        this.items = items;
    }

    public static class Item {
        public String qty;
        public String description;
        public String DateOfTransfer;
        public String locationFrom;
        public String locationTo;
        public Item(String qty, String description, String DateOfTransfer,
                    String locationFrom, String locationTo) {
            this.qty = qty;
            this.description = description;
            this.DateOfTransfer = DateOfTransfer;
            this.locationFrom = locationFrom;
            this.locationTo = locationTo;
        }

    }
}