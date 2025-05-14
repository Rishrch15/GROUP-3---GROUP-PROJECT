package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BorrowActivity extends AppCompatActivity {

    EditText date1, department, borrowerName, projectName, date2, time, venue;
    EditText qty, description, transferDate, locationFrom, locationTo, remarks;
    Button submitButton;
    Button addItemButton;

    LinearLayout itemsContainer;
    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_form);

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
        transferDate = findViewById(R.id.editTextDateOfTransfer);
        locationFrom = findViewById(R.id.editTextLocationFrom);
        locationTo = findViewById(R.id.editTextLocationTo);

        submitButton = findViewById(R.id.buttonSubmit);
        addItemButton = findViewById(R.id.add_item_button);

        itemsContainer = findViewById(R.id.itemsContainer);
        inflater = LayoutInflater.from(this);

        // Submit button logic
        submitButton.setOnClickListener(v -> {
            if (validateFields()) {
                Intent intent = new Intent(BorrowActivity.this, ActivityPending.class);

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

                startActivity(intent);
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Add Item button logic
        addItemButton.setOnClickListener(v -> {
            View itemRow = inflater.inflate(R.layout.item_row, itemsContainer, false);
            itemsContainer.addView(itemRow);
        });
    }

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
                && !locationTo.getText().toString().isEmpty();
    }
}
