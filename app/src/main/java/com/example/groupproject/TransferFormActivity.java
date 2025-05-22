package com.example.groupproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton; // Added for the remove item button
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; // Import for List deserialization

import java.lang.reflect.Type; // Import for Type
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID; // For generating unique request IDs

public class TransferFormActivity extends AppCompatActivity {

    // Declare UI elements
    private TextView textViewDateToday; // Renamed for clarity: this is the TextView showing the selected date
    private EditText editTextDepartment, editTextBorrowerName, editTextOthersSpecify;
    private TextView textViewGender; // Renamed for clarity: this is the TextView showing the selected gender

    private CheckBox checkBoxTransfer, checkBoxPullOut, checkBoxOfficeTables;
    private CheckBox checkBoxFilingCabinets, checkBoxOthers;

    private LinearLayout itemsContainer; // For dynamically added item rows
    private Button buttonSubmit, buttonDateToday, buttonGender, buttonAddItem; // Renamed for clarity
    private LayoutInflater inflater;

    // Assuming you have a class named BorrowRequest and an inner class Item
    private BorrowRequest request; // For loading existing data
    private List<BorrowRequest.Item> currentItems = new ArrayList<>(); // Crucial: To manage items added via dialog

    // Declare BottomNavigationView at class level
    private BottomNavigationView bottomNavigationView;

    // Key for SharedPreferences to store the list of requests
    private static final String PREFS_NAME = "all_borrow_requests";
    private static final String REQUESTS_KEY = "list_of_requests";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_form); // Ensure this XML layout name is correct

        // --- Initialize UI Elements ---
        textViewDateToday = findViewById(R.id.showText); // TextView for Date Today (using showText as per your XML)
        editTextDepartment = findViewById(R.id.editTextDepartment); // EditText for Department
        editTextBorrowerName = findViewById(R.id.editTextBorrowerName); // EditText for Borrower Name
        textViewGender = findViewById(R.id.showText4); // TextView for Gender (using showText4 as per your XML)

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

        // Set today's date and default gender on creation if not loading existing data
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        textViewDateToday.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day));
        textViewGender.setText("Male"); // Default gender

        // --- Set up Click Listeners ---
        buttonDateToday.setOnClickListener(v -> openDatePicker(textViewDateToday));
        // Allow clicking the TextView itself to open the picker
        textViewDateToday.setOnClickListener(v -> openDatePicker(textViewDateToday));

        buttonGender.setOnClickListener(v -> openGenderPicker(textViewGender)); // Pass the correct TextView
        // Allow clicking the TextView itself to open the picker
        textViewGender.setOnClickListener(v -> openGenderPicker(textViewGender));

        // This button now opens the dialog to add an item
        buttonAddItem.setOnClickListener(v -> showAddItemDialog());

        // --- Load existing data or set up initial state ---
        // This part needs adjustment if you want to load a specific request for editing.
        // For simply submitting new requests, you might not need to load a 'request' via intent.
        // However, if you intend to reuse this form to *edit* a request, this logic is useful.
        loadRequestFromIntent(); // Renamed and adjusted
        setupListeners(); // Setup other listeners like submit and checkbox

        // --- Bottom Navigation Setup (Moved to onCreate) ---
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Using R.id.nav_dashboard as an example, ensure this ID exists in your bottom_navigation_menu.xml
        // This should reflect the current activity or be set to a default.
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard); // Select current page's icon

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                Intent dashboardIntent = new Intent(TransferFormActivity.this, Success.class);
                startActivity(dashboardIntent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent profileIntent = new Intent(TransferFormActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            }
            else if (id == R.id.nav_logout) {
                performLogout();
                return true;
            }
            return false;
        });
    } // End of onCreate

    // --- Methods for form logic ---

    private void loadRequestFromIntent() {
        // This method will load a specific BorrowRequest object if passed via intent
        // (e.g., when editing an existing request from a list)
        request = (BorrowRequest) getIntent().getSerializableExtra("request_to_edit");

        if (request == null) {
            // If no request is passed via intent, initialize with current date and default gender
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            textViewDateToday.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day));
            textViewGender.setText("Male");
            editTextOthersSpecify.setVisibility(checkBoxOthers.isChecked() ? View.VISIBLE : View.GONE);
            return;
        }

        // If a request object exists, populate the form fields with its data
        textViewDateToday.setText(request.date1);
        editTextDepartment.setText(request.department);
        editTextBorrowerName.setText(request.borrowerName);
        textViewGender.setText(request.gender);

        checkBoxTransfer.setChecked(request.isTransfer);
        checkBoxPullOut.setChecked(request.isPullOut);
        checkBoxOfficeTables.setChecked(request.isOfficeTables);
        checkBoxFilingCabinets.setChecked(request.isFilingCabinets);
        checkBoxOthers.setChecked(request.isOthers);
        editTextOthersSpecify.setText(request.othersSpecify);
        editTextOthersSpecify.setVisibility(request.isOthers ? View.VISIBLE : View.GONE);

        itemsContainer.removeAllViews(); // Clear existing dynamic items
        currentItems.clear(); // Clear the list

        if (request.items != null && !request.items.isEmpty()) {
            for (BorrowRequest.Item item : request.items) {
                currentItems.add(item); // Add to the list
                addItemSummaryRow(item); // Display summary row
            }
        }
        Toast.makeText(this, "Editing existing request", Toast.LENGTH_SHORT).show();
    }


    private void setupListeners() {
        buttonSubmit.setOnClickListener(v -> submitForm());

        checkBoxOthers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editTextOthersSpecify.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) editTextOthersSpecify.setText("");
        });
    }

    // Method to show the dialog for adding an item
    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        // Assuming you have this layout defined: dialog_add_transfer_item.xml
        View dialogView = inflater.inflate(R.layout.dialog_add_transfer_item, null);
        builder.setView(dialogView);

        NumberPicker dialogQtyPicker = dialogView.findViewById(R.id.dialogNumberPickerQty);
        EditText dialogDescription = dialogView.findViewById(R.id.dialogEditTextDescription);
        TextView dialogDateOfTransfer = dialogView.findViewById(R.id.dialogTextViewDateOfTransfer);
        Button dialogDateButton = dialogView.findViewById(R.id.dialogButtonDateOfTransfer);
        EditText dialogLocationFrom = dialogView.findViewById(R.id.dialogEditTextLocationFrom);
        EditText dialogLocationTo = dialogView.findViewById(R.id.dialogEditTextLocationTo);

        dialogQtyPicker.setMinValue(1);
        dialogQtyPicker.setMaxValue(100);
        dialogQtyPicker.setValue(1); // Default quantity

        // Set current date to the dialog's date field
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        dialogDateOfTransfer.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day)); // Consistent format

        dialogDateButton.setOnClickListener(v -> openDatePicker(dialogDateOfTransfer)); // Use consistent openDatePicker
        dialogDateOfTransfer.setOnClickListener(v -> openDatePicker(dialogDateOfTransfer)); // Make TextView clickable too

        AlertDialog dialog = builder.create();

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", (d, which) -> {}); // Keep empty for custom listener
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, which) -> d.dismiss());

        dialog.show();

        // Custom listener for the positive button to prevent dismiss on validation failure
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String qtyStr = String.valueOf(dialogQtyPicker.getValue());
            String description = dialogDescription.getText().toString().trim();
            String dateOfTransfer = dialogDateOfTransfer.getText().toString().trim();
            String locationFrom = dialogLocationFrom.getText().toString().trim();
            String locationTo = dialogLocationTo.getText().toString().trim();
            String remarks = ""; // Added this to match the Item constructor signature

            if (description.isEmpty() || dateOfTransfer.isEmpty() ||
                    locationFrom.isEmpty() || locationTo.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_fill_all_item_fields_dialog), Toast.LENGTH_SHORT).show();
            } else {
                try {
                    int qty = Integer.parseInt(qtyStr);
                    BorrowRequest.Item newItem = new BorrowRequest.Item(
                            qty, description, dateOfTransfer, locationFrom, locationTo, remarks); // Now passing all 6 arguments

                    currentItems.add(newItem); // Add to the list
                    addItemSummaryRow(newItem); // Add item to summary view

                    Toast.makeText(this, getString(R.string.item_added), Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); // Dismiss dialog only on success
                } catch (NumberFormatException e) {
                    Toast.makeText(this, getString(R.string.invalid_quantity), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // This method is used to display the added item in the main form's itemsContainer
    private void addItemSummaryRow(BorrowRequest.Item item) {
        // Assuming you have this layout defined: item_transfer_summary_row.xml
        View itemRowView = inflater.inflate(R.layout.item_transfer_summary_row, itemsContainer, false);

        TextView summaryQty = itemRowView.findViewById(R.id.summaryTextViewQty);
        TextView summaryDescription = itemRowView.findViewById(R.id.summaryTextViewDescription);
        TextView summaryDateOfTransfer = itemRowView.findViewById(R.id.summaryTextViewDateOfTransfer);
        TextView summaryFrom = itemRowView.findViewById(R.id.summaryTextViewFrom);
        TextView summaryTo = itemRowView.findViewById(R.id.summaryTextViewTo);
        ImageButton removeItemButton = itemRowView.findViewById(R.id.removeItemButton);

        summaryQty.setText(String.valueOf(item.qty));
        summaryDescription.setText(item.description);
        summaryDateOfTransfer.setText(item.dateOfTransfer);
        summaryFrom.setText(item.locationFrom);
        summaryTo.setText(item.locationTo);

        removeItemButton.setOnClickListener(v -> {
            itemsContainer.removeView(itemRowView);
            currentItems.remove(item);
            Toast.makeText(this, getString(R.string.item_removed), Toast.LENGTH_SHORT).show();
        });

        itemsContainer.addView(itemRowView);
    }

    private void submitForm() {
        if (!validateForm()) {
            return;
        }

        BorrowRequest newRequest = new BorrowRequest();
        // Generate a unique ID for the request
        newRequest.requestId = UUID.randomUUID().toString(); // Ensure requestId is in BorrowRequest
        newRequest.date1 = textViewDateToday.getText().toString().trim();
        newRequest.department = editTextDepartment.getText().toString().trim();
        newRequest.borrowerName = editTextBorrowerName.getText().toString().trim();
        newRequest.gender = textViewGender.getText().toString().trim();

        newRequest.isTransfer = checkBoxTransfer.isChecked();
        newRequest.isPullOut = checkBoxPullOut.isChecked();
        newRequest.isOfficeTables = checkBoxOfficeTables.isChecked();
        newRequest.isFilingCabinets = checkBoxFilingCabinets.isChecked();
        newRequest.isOthers = checkBoxOthers.isChecked();
        if (newRequest.isOthers) {
            newRequest.othersSpecify = editTextOthersSpecify.getText().toString().trim();
        }

        // Use currentItems list directly
        newRequest.items = new ArrayList<>(currentItems); // Create a new list to avoid modifying the original
        newRequest.status = "Pending"; // Set the status to Pending

        // Get existing requests, add the new one, and save the updated list
        List<BorrowRequest> allRequests = loadAllRequests();
        allRequests.add(newRequest);
        saveAllRequests(allRequests);

        Toast.makeText(this, getString(R.string.form_submitted_saved), Toast.LENGTH_LONG).show();

        // Clear the form after submission
        clearForm();

        // Navigate to PendingActivity to see the submitted request
        Intent pendingIntent = new Intent(TransferFormActivity.this, PendingActivity.class);
        startActivity(pendingIntent);
        finish(); // Finish this activity so user doesn't come back to a filled form
    }

    private boolean validateForm() {
        if (textViewDateToday.getText().toString().trim().isEmpty() ||
                editTextDepartment.getText().toString().trim().isEmpty() ||
                editTextBorrowerName.getText().toString().trim().isEmpty() ||
                textViewGender.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_general_info), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (checkBoxOthers.isChecked() && editTextOthersSpecify.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.specify_others_details), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (currentItems.isEmpty()) { // Check the list, not the container child count directly
            Toast.makeText(this, getString(R.string.add_at_least_one_item), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Item validation is mostly handled by the dialog, so we can simplify this.
        // If an item is added via dialog, it's already validated for non-empty fields.
        return true;
    }

    private void openDatePicker(final TextView targetView) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String formatted = String.format(Locale.getDefault(), "%d-%02d-%02d", selectedYear, (selectedMonth + 1), selectedDay);
            targetView.setText(formatted);
        }, year, month, day);
        dialog.show();
    }

    private void openGenderPicker(final TextView targetView) {
        // Use string array from resources
        String[] genders = getResources().getStringArray(R.array.genders_array); // Assuming you have genders_array in strings.xml

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_gender));
        builder.setItems(genders, (dialog, which) -> targetView.setText(genders[which]));
        builder.show();
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Yes", proceed with logout
                        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear(); // Clear all session data
                        editor.apply(); // Apply changes asynchronously

                        // Navigate to the login screen
                        Intent loginIntent = new Intent(TransferFormActivity.this, MainActivity.class);
                        // Clear activity stack to prevent going back to main app activities
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(loginIntent);
                        finish(); // Finish current activity
                        Toast.makeText(TransferFormActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "No", dismiss the dialog and do nothing
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert) // Optional: Add an alert icon
                .show();
    }

    // New method to load ALL requests from SharedPreferences
    private List<BorrowRequest> loadAllRequests() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(REQUESTS_KEY, null);
        Type type = new TypeToken<ArrayList<BorrowRequest>>() {}.getType();
        List<BorrowRequest> requests = gson.fromJson(json, type);
        if (requests == null) {
            requests = new ArrayList<>();
        }
        return requests;
    }

    // New method to save ALL requests to SharedPreferences
    private void saveAllRequests(List<BorrowRequest> requests) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(requests);
        editor.putString(REQUESTS_KEY, json);
        editor.apply();
    }

    // New method to clear the form fields and reset state
    private void clearForm() {
        textViewDateToday.setText("");
        editTextDepartment.setText("");
        editTextBorrowerName.setText("");
        textViewGender.setText("");

        checkBoxTransfer.setChecked(false);
        checkBoxPullOut.setChecked(false);
        checkBoxOfficeTables.setChecked(false);
        checkBoxFilingCabinets.setChecked(false);
        checkBoxOthers.setChecked(false);
        editTextOthersSpecify.setText("");
        editTextOthersSpecify.setVisibility(View.GONE);

        itemsContainer.removeAllViews(); // Remove all dynamically added item summary rows
        currentItems.clear(); // Clear the list of items

        // Reset to default values for convenience
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        textViewDateToday.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day));
        textViewGender.setText("Male");
    }

    // --- Helper classes (You need to define these if they don't exist) ---
    // Make sure these classes are either in their own files or as static nested classes
    // if you want to keep them in this file.

    // Example BorrowRequest class to hold form data
    public static class BorrowRequest implements java.io.Serializable {
        public String requestId; // Unique ID for each request
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
        public String status; // e.g., "Pending", "Approved", "Rejected"

        public BorrowRequest() {
            items = new ArrayList<>();
            this.status = "Pending"; // Default status for new requests
        }

        // Inner class for an item
        public static class Item implements java.io.Serializable {
            public int qty; // Changed to int based on NumberPicker
            public String description;
            public String dateOfTransfer;
            public String locationFrom;
            public String locationTo;
            public String remarks; // Added remarks

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
}