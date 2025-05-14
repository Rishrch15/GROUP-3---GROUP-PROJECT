package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button; // Already present
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BorrowActivity extends AppCompatActivity {

    EditText date1, department, borrowerName, projectName, date2, time, venue;
    EditText qty, description, transferDate, locationFrom, locationTo, remarks;
    Button submitButton;
    Button addItemButton; // Added declaration for the "Add Item" button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_form); // Use your layout name here

        // Initialize input fields
        date1 = findViewById(R.id.editTextDate1);
        department = findViewById(R.id.editTextDepartment);
        borrowerName = findViewById(R.id.editTextBorrowerName);
        projectName = findViewById(R.id.editTextProjectName);
        date2 = findViewById(R.id.editTextDate2);
        time = findViewById(R.id.editTextTime);
        venue = findViewById(R.id.editTextVenue);

        qty = findViewById(R.id.editTextQty);
        description = findViewById(R.id.editTextDescription);
        transferDate = findViewById(R.id.editTextTransferDate);
        locationFrom = findViewById(R.id.editTextLocationFrom);
        locationTo = findViewById(R.id.editTextLocationTo);
        remarks = findViewById(R.id.editTextRemarks);

        submitButton = findViewById(R.id.buttonSubmit);
        addItemButton = findViewById(R.id.add_item_button); // Initialize the "Add Item" button

        // Handle submission for the Submit button (remains the same)
        submitButton.setOnClickListener(v -> {
            if (validateFields()) {
                Intent intent = new Intent(BorrowActivity.this, ActivityPending.class);

                // Put data in intent
                intent.putExtra("date1", date1.getText().toString());
                intent.putExtra("department", department.getText().toString());
                intent.putExtra("borrowerName", borrowerName.getText().toString());
                intent.putExtra("projectName", projectName.getText().toString());
                intent.putExtra("date2", date2.getText().toString());
                intent.putExtra("time", time.getText().toString());
                intent.putExtra("venue", venue.getText().toString());

                intent.putExtra("qty", qty.getText().toString());
                intent.putExtra("description", description.getText().toString());
                intent.putExtra("transferDate", transferDate.getText().toString());
                intent.putExtra("locationFrom", locationFrom.getText().toString());
                intent.putExtra("locationTo", locationTo.getText().toString());
                intent.putExtra("remarks", remarks.getText().toString());

                startActivity(intent);
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // You'll likely want to add an OnClickListener for the "Add Item" button as well
        addItemButton.setOnClickListener(v -> {
            // Handle the logic for adding a new item here
            // This might involve adding a new row to your TableLayout
            // or navigating to another activity to add an item.
            Toast.makeText(this, "Add Item button clicked", Toast.LENGTH_SHORT).show(); // Placeholder
        });
    }

    // Validate all fields (remains the same)
    private boolean validateFields() {
        return !date1.getText().toString().isEmpty()
                && !department.getText().toString().isEmpty()
                && !borrowerName.getText().toString().isEmpty()
                && !projectName.getText().toString().isEmpty()
                && !date2.getText().toString().isEmpty()
                && !time.getText().toString().isEmpty()
                && !venue.getText().toString().isEmpty()
                && !qty.getText().toString().isEmpty()
                && !description.getText().toString().isEmpty()
                && !transferDate.getText().toString().isEmpty()
                && !locationFrom.getText().toString().isEmpty()
                && !locationTo.getText().toString().isEmpty()
                && !remarks.getText().toString().isEmpty();
    }
}