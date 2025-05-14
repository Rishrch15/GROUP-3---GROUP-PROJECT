package com.example.groupproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityPending extends AppCompatActivity {

    EditText editTextDate, editTextDepartment, editTextRequestingName,
            editTextProjectName, editTextDateTime, editTextVenue,
            editQty, editDescription, editDateTransfer,
            editLocationFromTo, editDateReturn, editRemarks, editStatus;
    Button btnSubmitPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_form);

        editTextDate = findViewById(R.id.editTextDate);
        editTextDepartment = findViewById(R.id.editTextDepartment);
        editTextRequestingName = findViewById(R.id.editTextRequestingName);
        editTextProjectName = findViewById(R.id.editTextProjectName);
        editTextDateTime = findViewById(R.id.editTextDateTime);
        editTextVenue = findViewById(R.id.editTextVenue);

        editQty = findViewById(R.id.editQty);
        editDescription = findViewById(R.id.editDescription);
        editDateTransfer = findViewById(R.id.editDateTransfer);

        editLocationFromTo = findViewById(R.id.editLocationFromTo);
        editDateReturn = findViewById(R.id.editDateReturn);
        editRemarks = findViewById(R.id.editRemarks);
        editStatus = findViewById(R.id.editStatus);

        btnSubmitPending = findViewById(R.id.btnSubmitPending);

        btnSubmitPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRequest();
            }
        });
    }

    private void submitRequest() {
        String name = editTextRequestingName.getText().toString().trim();
        String project = editTextProjectName.getText().toString().trim();
        String status = editStatus.getText().toString().trim();

        if (name.isEmpty() || project.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Request by " + name + " for project '" + project + "' submitted (" + status + ")", Toast.LENGTH_LONG).show();
    }
}

