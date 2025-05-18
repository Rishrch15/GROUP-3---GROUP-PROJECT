package com.example.groupproject;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

public class ApproveActivity extends AppCompatActivity {

    private TextView textDate, textDepartment, textRequestingName, textProjectName, textDateTime, textVenue;
    private TextView textQty, textDescription, textTransferDate, textFrom, textTo, textReturnDate, textRemarks;
    private TextView textApprovedBy;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_form);

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

        loadRequestData();
    }

    private void loadRequestData() {
        BorrowRequest request = (BorrowRequest) getIntent().getSerializableExtra("request");

        if (request == null) {
            SharedPreferences prefs = getSharedPreferences("permit_data", MODE_PRIVATE);
            String json = prefs.getString("request_json", null);
            if (json != null) {
                Gson gson = new Gson();
                request = gson.fromJson(json, BorrowRequest.class);
            }
        }

        if (request == null) {
            Toast.makeText(this, "No request data available", Toast.LENGTH_SHORT).show();
            return;
        }

        textDate.setText(request.date1);
        textDepartment.setText(request.department);
        textRequestingName.setText(request.borrowerName);
        textProjectName.setText(request.projectName);
        textDateTime.setText(request.time);
        textVenue.setText(request.venue);

        if (request.items != null && !request.items.isEmpty()) {
            BorrowRequest.Item item = request.items.get(0);
            textQty.setText(String.valueOf(item.qty));
            textDescription.setText(item.description);
            textTransferDate.setText(item.dateOfTransfer);
            textFrom.setText(item.locationFrom);
            textTo.setText(item.locationTo);
        }

        textReturnDate.setText("N/A");
        textRemarks.setText("None");
        textApprovedBy.setText("");
    }
}
