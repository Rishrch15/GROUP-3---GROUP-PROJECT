package com.example.groupproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TransferFormActivity extends AppCompatActivity {

    // Form header fields
    private EditText editTextDate, editTextControlNo, editTextDepartment;
    private EditText editTextRequestingOfficer, editTextSignature;

    // Checkbox fields
    private CheckBox checkBoxTransfer, checkBoxPullOut, checkBoxOfficeTables;
    private CheckBox checkBoxFilingCabinets, checkBoxOthers;
    private EditText editTextOthersSpecify;

    // Items container and buttons
    private LinearLayout itemsContainer;
    private Button buttonAddItem, buttonSubmit;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_form);

        // Initialize views
        initializeViews();

        // Set up listeners
        setupListeners();
    }

    private void initializeViews() {
        // Header fields
        editTextDate = findViewById(R.id.editTextDate);
        editTextDepartment = findViewById(R.id.editTextDepartment);
        editTextRequestingOfficer = findViewById(R.id.editTextRequestingOfficer);

        // Checkboxes
        checkBoxTransfer = findViewById(R.id.checkBoxTransfer);
        checkBoxPullOut = findViewById(R.id.checkBoxPullOut);
        checkBoxOfficeTables = findViewById(R.id.checkBoxOfficeTables);
        checkBoxFilingCabinets = findViewById(R.id.checkBoxFilingCabinets);
        checkBoxOthers = findViewById(R.id.checkBoxOthers);
        editTextOthersSpecify = findViewById(R.id.editTextOthers);

        // Items container and buttons
        itemsContainer = findViewById(R.id.itemsContainer);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        inflater = LayoutInflater.from(this);
    }

    private void setupListeners() {
        // Add item button
        buttonAddItem.setOnClickListener(v -> addItemRow());

        // Submit button
        buttonSubmit.setOnClickListener(v -> submitForm());

        // Others checkbox listener
        checkBoxOthers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editTextOthersSpecify.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) editTextOthersSpecify.setText("");
        });
    }

    private void addItemRow() {
        View itemRowView = inflater.inflate(R.layout.item_row, itemsContainer, false);
        itemsContainer.addView(itemRowView);
    }

    private void submitForm() {
        // Validate required fields
        if (!validateForm()) {
            return;
        }

        // Collect form data
        String formData = collectFormData();

        // Display submission (replace with actual submission logic)
        Toast.makeText(this, "Form Submitted!\n" + formData, Toast.LENGTH_LONG).show();
    }

    private boolean validateForm() {
        if (editTextDate.getText().toString().trim().isEmpty() ||
                editTextControlNo.getText().toString().trim().isEmpty() ||
                editTextDepartment.getText().toString().trim().isEmpty() ||
                editTextRequestingOfficer.getText().toString().trim().isEmpty() ||
                editTextSignature.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (itemsContainer.getChildCount() == 0) {
            Toast.makeText(this, "Please add at least one item", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String collectFormData() {
        StringBuilder sb = new StringBuilder();

        // Header information
        sb.append("Date: ").append(editTextDate.getText().toString().trim()).append("\n")
                .append("Control No: ").append(editTextControlNo.getText().toString().trim()).append("\n")
                .append("Department: ").append(editTextDepartment.getText().toString().trim()).append("\n")
                .append("Requesting Officer: ").append(editTextRequestingOfficer.getText().toString().trim()).append("\n")
                .append("Signature: ").append(editTextSignature.getText().toString().trim()).append("\n");

        // Request type
        sb.append("\nRequest Type:\n")
                .append("  Transfer: ").append(checkBoxTransfer.isChecked()).append("\n")
                .append("  Pull Out: ").append(checkBoxPullOut.isChecked()).append("\n")
                .append("  Office Tables: ").append(checkBoxOfficeTables.isChecked()).append("\n")
                .append("  Filing Cabinets: ").append(checkBoxFilingCabinets.isChecked()).append("\n")
                .append("  Others: ").append(checkBoxOthers.isChecked());

        if (checkBoxOthers.isChecked()) {
            sb.append(" - ").append(editTextOthersSpecify.getText().toString().trim());
        }

        // Items
        sb.append("\n\nItems:\n");
        for (int i = 0; i < itemsContainer.getChildCount(); i++) {
            View itemRow = itemsContainer.getChildAt(i);

            sb.append("Item ").append(i + 1).append(":\n")
                    .append("  Qty: ").append(((EditText)itemRow.findViewById(R.id.editTextQty)).getText().toString().trim()).append("\n")
                    .append("  Description: ").append(((EditText)itemRow.findViewById(R.id.editTextDescription)).getText().toString().trim()).append("\n")
                    .append("  Date of Transfer: ").append(((EditText)itemRow.findViewById(R.id.editTextDateOfTransfer)).getText().toString().trim()).append("\n")
                    .append("  Location From: ").append(((EditText)itemRow.findViewById(R.id.editTextLocationFrom)).getText().toString().trim()).append("\n")
                    .append("  Location To: ").append(((EditText)itemRow.findViewById(R.id.editTextLocationTo)).getText().toString().trim()).append("\n")
                    .append("  Remarks: ").append(((EditText)itemRow.findViewById(R.id.editTextRemarks)).getText().toString().trim()).append("\n\n");
        }

        return sb.toString();
    }
}