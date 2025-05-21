package com.example.groupproject;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;

public class ApproveActivity extends AppCompatActivity {

    private TextView textDate, textDepartment, textRequestingName, textProjectName,
            textDateTime, textVenue, textQty, textDescription, textTransferDate,
            textFrom, textTo, textReturnDate, textRemarks, textApprovedBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_form);

        // Bind views
        textDate = findViewById(R.id.textViewDate);
        textDepartment = findViewById(R.id.textViewDepartment);
        textRequestingName = findViewById(R.id.textViewRequestingName);
        textProjectName = findViewById(R.id.textViewProjectName);
        textDateTime = findViewById(R.id.textViewDateTime);
        textVenue = findViewById(R.id.textViewVenue);
        textQty = findViewById(R.id.textViewQty);
        textDescription = findViewById(R.id.textViewDescription);
        textTransferDate = findViewById(R.id.textViewTransferDate);
        textFrom = findViewById(R.id.textViewFrom);
        textTo = findViewById(R.id.textViewTo);
        textReturnDate = findViewById(R.id.textViewReturnDate);
        textRemarks = findViewById(R.id.textViewRemarks);
        textApprovedBy = findViewById(R.id.textViewApprovedBy);

        // Get data from Intent
        String requestJson = getIntent().getStringExtra("request_json");

        if (requestJson != null) {
            BorrowRequest request = new Gson().fromJson(requestJson, BorrowRequest.class);
            populateUI(request);
        } else {
            textRequestingName.setText("No data received");
        }
    }

    private void populateUI(BorrowRequest request) {
        textDate.setText(request.todayDate); // submitted date
        textDepartment.setText(request.department);
        textRequestingName.setText(request.borrowerName);
        textProjectName.setText(request.projectName);
        textDateTime.setText(request.dateNeeded + " - " + request.time);
        textVenue.setText(request.venue);

        if (request.items != null && !request.items.isEmpty()) {
            BorrowRequest.Item item = request.items.get(0);
            textQty.setText(item.qty);
            textDescription.setText(item.description);
            textTransferDate.setText(item.DateOfTransfer);
            textFrom.setText(item.locationFrom);
            textTo.setText(item.locationTo);
        } else {
            textQty.setText("N/A");
            textDescription.setText("N/A");
            textTransferDate.setText("N/A");
            textFrom.setText("N/A");
            textTo.setText("N/A");
        }

        textReturnDate.setText("N/A"); // you can change this if needed
        textRemarks.setText("None");   // customize if needed
        textApprovedBy.setText("");    // optional field
    }
}
