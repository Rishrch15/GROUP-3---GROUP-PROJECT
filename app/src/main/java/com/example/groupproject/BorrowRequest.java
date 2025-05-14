package com.example.groupproject;

import java.io.Serializable;
import java.util.List;

public class BorrowRequest implements Serializable {
    public String date1, department, borrowerName, projectName, date2, time, venue;
    public List<Item> items;

    public static class Item implements Serializable {
        public String qty, description, dateOfTransfer, locationFrom, locationTo;

        public Item(String qty, String description, String dateOfTransfer, String locationFrom, String locationTo) {
            this.qty = qty;
            this.description = description;
            this.dateOfTransfer = dateOfTransfer;
            this.locationFrom = locationFrom;
            this.locationTo = locationTo;
        }
    }

    public BorrowRequest(String date1, String department, String borrowerName, String projectName,
                         String date2, String time, String venue, List<Item> items) {
        this.date1 = date1;
        this.department = department;
        this.borrowerName = borrowerName;
        this.projectName = projectName;
        this.date2 = date2;
        this.time = time;
        this.venue = venue;
        this.items = items;
    }
}
