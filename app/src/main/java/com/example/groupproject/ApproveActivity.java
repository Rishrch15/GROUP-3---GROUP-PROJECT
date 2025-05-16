package com.example.groupproject;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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

        showFormData();
    }

    private void showFormData() {
        StringBuilder formData = new StringBuilder();
        formData.append("Date: ").append(textDate.getText().toString()).append("\n")
                .append("Department: ").append(textDepartment.getText().toString()).append("\n")
                .append("Requesting Name: ").append(textRequestingName.getText().toString()).append("\n")
                .append("Project Name: ").append(textProjectName.getText().toString()).append("\n")
                .append("Date and Time: ").append(textDateTime.getText().toString()).append("\n")
                .append("Venue: ").append(textVenue.getText().toString()).append("\n")
                .append("\nItem Details:\n")
                .append("Qty: ").append(textQty.getText().toString()).append("\n")
                .append("Description: ").append(textDescription.getText().toString()).append("\n")
                .append("Date of Transfer: ").append(textTransferDate.getText().toString()).append("\n")
                .append("From: ").append(textFrom.getText().toString()).append("\n")
                .append("To: ").append(textTo.getText().toString()).append("\n")
                .append("Date of Return: ").append(textReturnDate.getText().toString()).append("\n")
                .append("Remarks: ").append(textRemarks.getText().toString()).append("\n")
                .append("\nApproved By: ").append(textApprovedBy.getText().toString()).append("\n");

        Toast.makeText(this, formData.toString(), Toast.LENGTH_LONG).show();
    }
}
