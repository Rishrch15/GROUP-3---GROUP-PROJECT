package com.example.groupproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem; // Make sure this is imported
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText; // Use EditText for input fields
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Import for @NonNull
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.ArrayList; // Needed if you plan to store multiple items
import java.util.List; // Needed if you plan to store multiple items

public class TransferFormActivity extends AppCompatActivity {

    // Declare UI elements
    private TextView showTextDateToday; // Renamed for clarity: this is the TextView showing the selected date
    private EditText editTextDepartment, editTextBorrowerName, editTextOthersSpecify;
    private TextView showTextGender; // Renamed for clarity: this is the TextView showing the selected gender

    private CheckBox checkBoxTransfer, checkBoxPullOut, checkBoxOfficeTables;
    private CheckBox checkBoxFilingCabinets, checkBoxOthers;

    private LinearLayout itemsContainer; // For dynamically added item rows
    private Button buttonSubmit, buttonDateToday, buttonGender, buttonAddItem; // Renamed for clarity
    private LayoutInflater inflater;

    // Assuming you have a class named BorrowRequest and an inner class Item
    private BorrowRequest request;

    // Declare BottomNavigationView at class level
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_form); // Ensure this XML layout name is correct

        // --- Initialize UI Elements ---
        showTextDateToday = findViewById(R.id.showText); // TextView for Date Today
        editTextDepartment = findViewById(R.id.editTextDepartment); // EditText for Department
        editTextBorrowerName = findViewById(R.id.editTextBorrowerName); // EditText for Borrower Name
        showTextGender = findViewById(R.id.showText4); // TextView for Gender

        checkBoxTransfer = findViewById(R.id.checkBoxTransfer);
        checkBoxPullOut = findViewById(R.id.checkBoxPullOut);
        checkBoxOfficeTables = findViewById(R.id.checkBoxOfficeTables);
        checkBoxFilingCabinets = findViewById(R.id.checkBoxFilingCabinets);
        checkBoxOthers = findViewById(R.id.checkBoxOthers);
        editTextOthersSpecify = findViewById(R.id.editTextOthers);

        itemsContainer = findViewById(R.id.itemsContainer); // LinearLayout to hold item rows
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonAddItem = findViewById(R.id.buttonAddItem); // Initialize the Add Item button

        buttonDateToday = findViewById(R.id.dateButton); // Button to open Date Today picker
        buttonGender = findViewById(R.id.genderButton); // Button to open Gender picker

        inflater = LayoutInflater.from(this); // Initialize LayoutInflater

        // --- Set up Click Listeners ---
        buttonDateToday.setOnClickListener(v -> openDatePicker(showTextDateToday));
        buttonGender.setOnClickListener(v -> openGenderPicker(showTextGender)); // Pass the correct TextView

        buttonAddItem.setOnClickListener(v -> addItemRow()); // Listener for Add Item button

        // --- Load existing data or set up initial state ---
        loadRequestData();
        setupListeners(); // Setup other listeners like submit and checkbox

        // --- Bottom Navigation Setup (Moved to onCreate) ---
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard); // Highlight transfer icon, assuming this is the correct ID for this page

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_dashboard) {
                    Intent dashboardIntent = new Intent(TransferFormActivity.this, Success.class); // Use TransferFormActivity.this
                    startActivity(dashboardIntent);
                    // Optional: finish() this activity if you don't want it on the back stack
                    // finish();
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent profileIntent = new Intent(TransferFormActivity.this, ProfileActivity.class); // Use TransferFormActivity.this
                    startActivity(profileIntent);
                    // Optional: finish() this activity
                    // finish();
                    return true;
                } else if (id == R.id.nav_logout) {
                    performLogout();
                    return true;
                } else if (id == R.id.nav_dashboard) { // Assuming nav_transfer is the ID for this form
                    Toast.makeText(TransferFormActivity.this, "You are already on the Dashboard", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    } // End of onCreate

    // --- Methods for form logic ---

    private void loadRequestData() {
        // This part needs careful handling if you're dynamically adding rows.
        // The current XML has one fixed row. If you load data, you'd populate that fixed row
        // or add new rows if there are multiple items in 'request.items'.

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
            // If no request data, ensure your initial UI state is ready for new input
            // For example, make sure the "Others" EditText is hidden if the checkbox isn't checked
            editTextOthersSpecify.setVisibility(checkBoxOthers.isChecked() ? View.VISIBLE : View.GONE);
            return;
        }

        // Set text for the UI elements based on loaded data
        showTextDateToday.setText(request.date1); // Assuming date1 is the field for "Date Today"
        editTextDepartment.setText(request.department);
        editTextBorrowerName.setText(request.borrowerName);
        showTextGender.setText(request.gender); // Assuming 'gender' field exists in BorrowRequest

        // Set checkbox states
        checkBoxTransfer.setChecked(request.isTransfer); // Assuming isTransfer field exists
        checkBoxPullOut.setChecked(request.isPullOut); // Assuming isPullOut field exists
        checkBoxOfficeTables.setChecked(request.isOfficeTables); // Assuming isOfficeTables field exists
        checkBoxFilingCabinets.setChecked(request.isFilingCabinets); // Assuming isFilingCabinets field exists
        checkBoxOthers.setChecked(request.isOthers); // Assuming isOthers field exists
        editTextOthersSpecify.setText(request.othersSpecify); // Assuming othersSpecify field exists
        editTextOthersSpecify.setVisibility(request.isOthers ? View.VISIBLE : View.GONE);


        // Clear existing dynamic items if any (important when loading to avoid duplicates)
        itemsContainer.removeAllViews();

        if (request.items != null && !request.items.isEmpty()) {
            // Populate the first item row (if it's static in XML) or dynamically add all
            // If activity_transfer_form.xml already contains one item row with IDs like editTextQty, etc.
            // then you can populate that one first and then add new rows for subsequent items.
            // For now, I'm assuming you will always dynamically add rows, even for the first one.
            for (BorrowRequest.Item item : request.items) {
                addItemRow(item); // Call a version that takes an Item object to populate
            }
        } else {
            // If no items, add an empty row to start with for new input
            addItemRow();
        }
    }

    private void setupListeners() {
        buttonSubmit.setOnClickListener(v -> submitForm());

        checkBoxOthers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editTextOthersSpecify.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) editTextOthersSpecify.setText("");
        });
    }

    // Method to add an empty item row for new input
    private void addItemRow() {
        addItemRow(null); // Call the overloaded method without an Item to populate
    }

    // Overloaded method to add an item row, optionally populating it with data
    private void addItemRow(BorrowRequest.Item itemData) {
        // You MUST have a layout file named 'item_row.xml' in res/layout/
        // This file should contain the EditTexts for qty, description, date, from, to, remarks.
        View itemRowView = inflater.inflate(R.layout.item_row, itemsContainer, false);

        final EditText editTextQty = itemRowView.findViewById(R.id.editTextQty);
        final EditText editTextDescription = itemRowView.findViewById(R.id.editTextDescription);
        final EditText editTextDateOfTransfer = itemRowView.findViewById(R.id.editTextDateOfTransfer);
        final EditText editTextFrom = itemRowView.findViewById(R.id.editTextFrom);
        final EditText editTextTo = itemRowView.findViewById(R.id.editTextTo);
        final EditText editTextRemarks = itemRowView.findViewById(R.id.editTextRemarks);

        // Populate if itemData is provided (for loading existing requests)
        if (itemData != null) {
            editTextQty.setText(String.valueOf(itemData.qty));
            editTextDescription.setText(itemData.description);
            editTextDateOfTransfer.setText(itemData.dateOfTransfer);
            editTextFrom.setText(itemData.locationFrom);
            editTextTo.setText(itemData.locationTo);
            editTextRemarks.setText(itemData.remarks); // Assuming a remarks field in Item
        }

        // Set listener for the date picker button within this new row

        itemsContainer.addView(itemRowView);
    }

    private void submitForm() {
        if (!validateForm()) {
            return;
        }

        // Collect form data into a BorrowRequest object
        BorrowRequest newRequest = new BorrowRequest();
        newRequest.date1 = showTextDateToday.getText().toString().trim();
        newRequest.department = editTextDepartment.getText().toString().trim();
        newRequest.borrowerName = editTextBorrowerName.getText().toString().trim();
        newRequest.gender = showTextGender.getText().toString().trim();

        newRequest.isTransfer = checkBoxTransfer.isChecked();
        newRequest.isPullOut = checkBoxPullOut.isChecked();
        newRequest.isOfficeTables = checkBoxOfficeTables.isChecked();
        newRequest.isFilingCabinets = checkBoxFilingCabinets.isChecked();
        newRequest.isOthers = checkBoxOthers.isChecked();
        if (newRequest.isOthers) {
            newRequest.othersSpecify = editTextOthersSpecify.getText().toString().trim();
        }

        List<BorrowRequest.Item> items = new ArrayList<>();
        for (int i = 0; i < itemsContainer.getChildCount(); i++) {
            View itemRow = itemsContainer.getChildAt(i);
            EditText qtyEditText = itemRow.findViewById(R.id.editTextQty);
            EditText descriptionEditText = itemRow.findViewById(R.id.editTextDescription);
            EditText dateOfTransferEditText = itemRow.findViewById(R.id.editTextDateOfTransfer);
            EditText locationFromEditText = itemRow.findViewById(R.id.editTextFrom);
            EditText locationToEditText = itemRow.findViewById(R.id.editTextTo);
            EditText remarksEditText = itemRow.findViewById(R.id.editTextRemarks);

            // Basic validation for dynamically added items
            if (qtyEditText.getText().toString().trim().isEmpty() ||
                    descriptionEditText.getText().toString().trim().isEmpty() ||
                    dateOfTransferEditText.getText().toString().trim().isEmpty() ||
                    locationFromEditText.getText().toString().trim().isEmpty() ||
                    locationToEditText.getText().toString().trim().isEmpty() ||
                    remarksEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please fill all fields for item " + (i + 1), Toast.LENGTH_SHORT).show();
                return; // Stop submission if any item field is empty
            }

            try {
                int qty = Integer.parseInt(qtyEditText.getText().toString().trim());
                items.add(new BorrowRequest.Item(
                        qty,
                        descriptionEditText.getText().toString().trim(),
                        dateOfTransferEditText.getText().toString().trim(),
                        locationFromEditText.getText().toString().trim(),
                        locationToEditText.getText().toString().trim(),
                        remarksEditText.getText().toString().trim()
                ));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity for item " + (i + 1), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        newRequest.items = items;


        // Save or send the newRequest object
        saveRequestData(newRequest); // Example: save to SharedPreferences
        Toast.makeText(this, "Form Submitted and Saved!", Toast.LENGTH_LONG).show();

        // Optionally, navigate away or clear form after submission
        // finish();
    }

    private boolean validateForm() {
        // Validate main fields
        if (showTextDateToday.getText().toString().trim().isEmpty() ||
                editTextDepartment.getText().toString().trim().isEmpty() ||
                editTextBorrowerName.getText().toString().trim().isEmpty() ||
                showTextGender.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill in all general information fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate "Others" checkbox with text field
        if (checkBoxOthers.isChecked() && editTextOthersSpecify.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please specify details for 'Others'", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate at least one item row exists and its fields are filled
        if (itemsContainer.getChildCount() == 0) {
            Toast.makeText(this, "Please add at least one item row", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Iterate through each dynamically added item row for validation
        for (int i = 0; i < itemsContainer.getChildCount(); i++) {
            View itemRow = itemsContainer.getChildAt(i);
            EditText qtyEditText = itemRow.findViewById(R.id.editTextQty);
            EditText descriptionEditText = itemRow.findViewById(R.id.editTextDescription);
            EditText dateOfTransferEditText = itemRow.findViewById(R.id.editTextDateOfTransfer);
            EditText locationFromEditText = itemRow.findViewById(R.id.editTextFrom);
            EditText locationToEditText = itemRow.findViewById(R.id.editTextTo);
            EditText remarksEditText = itemRow.findViewById(R.id.editTextRemarks);

            if (qtyEditText.getText().toString().trim().isEmpty() ||
                    descriptionEditText.getText().toString().trim().isEmpty() ||
                    dateOfTransferEditText.getText().toString().trim().isEmpty() ||
                    locationFromEditText.getText().toString().trim().isEmpty() ||
                    locationToEditText.getText().toString().trim().isEmpty() ||
                    remarksEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please fill all fields for item " + (i + 1), Toast.LENGTH_SHORT).show();
                return false;
            }

            try {
                Integer.parseInt(qtyEditText.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity for item " + (i + 1), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }


    private void openDatePicker(final TextView targetView) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String formatted = String.format("%d-%02d-%02d", selectedYear, (selectedMonth + 1), selectedDay);
            targetView.setText(formatted);
        }, year, month, day);
        dialog.show();
    }

    private void openGenderPicker(final TextView targetView) {
        String[] genders = {"Male", "Female"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Gender");
        builder.setItems(genders, (dialog, which) -> targetView.setText(genders[which]));
        builder.show();
    }

    private void performLogout() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all data
        editor.apply();

        // Assuming MainActivity is your login activity
        Intent loginIntent = new Intent(TransferFormActivity.this, MainActivity.class); // Use TransferFormActivity.this
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear activity stack
        startActivity(loginIntent);
        finish(); // Finish the current activity
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
    }

    // --- Helper classes (You need to define these if they don't exist) ---
    // Make sure these classes are either in their own files or as static nested classes
    // if you want to keep them in this file.

    // Example BorrowRequest class to hold form data
    public static class BorrowRequest implements java.io.Serializable {
        public String date1;
        public String department;
        public String borrowerName;
        public String gender;
        public boolean isTransfer;
        public boolean isPullOut;
        public boolean isOfficeTables;
        public boolean isFilingCabinets;
        public boolean isOthers;
        public String othersSpecify;
        public List<Item> items; // List to hold multiple items

        public BorrowRequest() {
            items = new ArrayList<>();
        }

        // Inner class for an item
        public static class Item implements java.io.Serializable {
            public int qty;
            public String description;
            public String dateOfTransfer;
            public String locationFrom;
            public String locationTo;
            public String remarks;

            public Item(int qty, String description, String dateOfTransfer, String locationFrom, String locationTo, String remarks) {
                this.qty = qty;
                this.description = description;
                this.dateOfTransfer = dateOfTransfer;
                this.locationFrom = locationFrom;
                this.locationTo = locationTo;
                this.remarks = remarks;
            }
        }
    }

    // Example method to save request data (e.g., to SharedPreferences)
    private void saveRequestData(BorrowRequest request) {
        SharedPreferences prefs = getSharedPreferences("permit_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(request);
        editor.putString("request_json", json);
        editor.apply();
    }
}