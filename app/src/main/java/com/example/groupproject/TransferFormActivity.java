package com.example.groupproject;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;

public class TransferFormActivity extends AppCompatActivity {

    private TextView textDate, textDepartment, textRequestingName;
    private TextView textQty, textDescription, textDateOfTransfer, textFrom, textTo, textRemarks;
    private CheckBox checkBoxTransfer, checkBoxPullOut, checkBoxOfficeTables;
    private CheckBox checkBoxFilingCabinets, checkBoxOthers;
    private EditText editTextOthersSpecify;

    private LinearLayout itemsContainer;
    private Button buttonSubmit;
    private LayoutInflater inflater;

    private BorrowRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_form);

        textDate = findViewById(R.id.textViewDateLabel);
        textDepartment = findViewById(R.id.textViewDepartmentLabel);
        textRequestingName = findViewById(R.id.textViewRequestingNameLabel);

        checkBoxTransfer = findViewById(R.id.checkBoxTransfer);
        checkBoxPullOut = findViewById(R.id.checkBoxPullOut);
        checkBoxOfficeTables = findViewById(R.id.checkBoxOfficeTables);
        checkBoxFilingCabinets = findViewById(R.id.checkBoxFilingCabinets);
        checkBoxOthers = findViewById(R.id.checkBoxOthers);
        editTextOthersSpecify = findViewById(R.id.editTextOthers);

        itemsContainer = findViewById(R.id.itemsContainer);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        textQty = findViewById(R.id.textViewQtyLabel);
        textDescription = findViewById(R.id.textViewDescriptionLabel);
        textDateOfTransfer = findViewById(R.id.textViewTransferDateLabel);
        textFrom = findViewById(R.id.textViewFromLabel);
        textTo = findViewById(R.id.textViewToLabel);
        textRemarks = findViewById(R.id.textViewRemarksLabel);

        inflater = LayoutInflater.from(this);

        loadRequestData();
        setupListeners();
    }

    private void loadRequestData() {
        request = (BorrowRequest) getIntent().getSerializableExtra("request");

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

        if (request.items != null && !request.items.isEmpty()) {
            BorrowRequest.Item item = request.items.get(0);
            textQty.setText(String.valueOf(item.qty));
            textDescription.setText(item.description);
            textDateOfTransfer.setText(item.dateOfTransfer);
            textFrom.setText(item.locationFrom);
            textTo.setText(item.locationTo);
        }

        textRemarks.setText("None");
    }

    private void setupListeners() {

        buttonSubmit.setOnClickListener(v -> submitForm());

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
        if (!validateForm()) {
            return;
        }

        String formData = collectFormData();
        Toast.makeText(this, "Form Submitted!\n" + formData, Toast.LENGTH_LONG).show();
    }

    private boolean validateForm() {
        if (textDate.getText().toString().trim().isEmpty() ||
                textDepartment.getText().toString().trim().isEmpty() ||
                textRequestingName.getText().toString().trim().isEmpty()) {

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

        sb.append("Date: ").append(textDate.getText().toString().trim()).append("\n")
                .append("Department: ").append(textDepartment.getText().toString().trim()).append("\n")
                .append("Requesting Officer: ").append(textRequestingName.getText().toString().trim()).append("\n");

        sb.append("\nRequest Type:\n")
                .append("  Transfer: ").append(checkBoxTransfer.isChecked()).append("\n")
                .append("  Pull Out: ").append(checkBoxPullOut.isChecked()).append("\n")
                .append("  Office Tables: ").append(checkBoxOfficeTables.isChecked()).append("\n")
                .append("  Filing Cabinets: ").append(checkBoxFilingCabinets.isChecked()).append("\n")
                .append("  Others: ").append(checkBoxOthers.isChecked());

        if (checkBoxOthers.isChecked()) {
            sb.append(" - ").append(editTextOthersSpecify.getText().toString().trim());
        }

        sb.append("\n\nItems:\n");
        for (int i = 0; i < itemsContainer.getChildCount(); i++) {
            View itemRow = itemsContainer.getChildAt(i);

            sb.append("Item ").append(i + 1).append(":\n")
                    .append("  Qty: ").append(((EditText) itemRow.findViewById(R.id.editTextQty)).getText().toString().trim()).append("\n")
                    .append("  Description: ").append(((EditText) itemRow.findViewById(R.id.editTextDescription)).getText().toString().trim()).append("\n")
                    .append("  Date of Transfer: ").append(((EditText) itemRow.findViewById(R.id.editTextDateOfTransfer)).getText().toString().trim()).append("\n")
                    .append("  Location From: ").append(((EditText) itemRow.findViewById(R.id.editTextLocationFrom)).getText().toString().trim()).append("\n")
                    .append("  Location To: ").append(((EditText) itemRow.findViewById(R.id.editTextLocationTo)).getText().toString().trim()).append("\n")
                    .append("  Remarks: ").append(((EditText) itemRow.findViewById(R.id.editTextRemarks)).getText().toString().trim()).append("\n\n");
        }

        return sb.toString();
    }
}
