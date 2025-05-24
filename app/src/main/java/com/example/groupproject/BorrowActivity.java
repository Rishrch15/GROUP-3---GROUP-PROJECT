package com.example.groupproject;

import static com.example.groupproject.PendingActivity.BASE_URL;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem; // Added for BottomNavigationView
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.NumberPicker;

import androidx.annotation.NonNull; // Added for BottomNavigationView
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView; // Added for BottomNavigationView
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BorrowActivity extends AppCompatActivity {

    private static final String TAG = "BorrowActivity";
    public static final String URL = "http://192.168.185.26/E-permit/request.php";

    // Declare UI elements as class members
    TextView textDate, textDate2, text3, text4;
    EditText editDept, editName, editProject, editVenue;
    LinearLayout itemsContainer;
    Button addItemBtn, submitBtn, button, button2, button3, button4;
    public BottomNavigationView bottomNavigationView; // Declared here

    public List<BorrowRequest.Item> currentItems = new ArrayList<>();
    public RequestQueue requestQueue;
    public String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_form); // Make sure this XML layout name is correct

        requestQueue = Volley.newRequestQueue(this);

        // Get device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "Device ID: " + deviceId);

        // Initialize UI elements
        textDate = findViewById(R.id.showText); // Assuming showText for date submitted
        button = findViewById(R.id.dateButton); // Button for date submitted
        editDept = findViewById(R.id.editTextDepartment);
        editName = findViewById(R.id.editTextBorrowerName);
        text4 = findViewById(R.id.showText4); // Assuming showText4 for gender
        button4 = findViewById(R.id.genderButton); // Button for gender
        editProject = findViewById(R.id.editTextProjectName);
        textDate2 = findViewById(R.id.showText2); // Assuming showText2 for date of project
        button2 = findViewById(R.id.dateButton2); // Button for date of project
        text3 = findViewById(R.id.showText3); // Assuming showText3 for time of project
        button3 = findViewById(R.id.timeButton); // Button for time of project
        editVenue = findViewById(R.id.editTextVenue);

        itemsContainer = findViewById(R.id.itemsContainer);
        addItemBtn = findViewById(R.id.add_item_button);
        submitBtn = findViewById(R.id.buttonSubmit);

        // Set initial dates and time
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        textDate.setText(String.format(Locale.getDefault(), "%d.%02d.%02d", year, month + 1, day));
        textDate2.setText(String.format(Locale.getDefault(), "%d.%02d.%02d", year, month + 1, day));

        String amPm = hour >= 12 ? "PM" : "AM";
        int hourFormat = (hour > 12) ? hour - 12 : (hour == 0 ? 12 : hour);
        text3.setText(String.format(Locale.getDefault(), "%d:%02d %s", hourFormat, minute, amPm));
        text4.setText("Male"); // Set a default gender

        // Set up click listeners
        addItemBtn.setOnClickListener(v -> showAddItemDialog());
        submitBtn.setOnClickListener(v -> submitBorrowRequest());

        button.setOnClickListener(v -> openDatePickerDialog(textDate));
        button2.setOnClickListener(v -> openDatePickerDialog(textDate2));
        button3.setOnClickListener(v -> openTimePickerDialog(text3));
        button4.setOnClickListener(v -> openGenderPickerDialog(text4));

        // Bottom Navigation Setup - MOVED HERE
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Set the selected item based on the current activity if needed
        // bottomNavigationView.setSelectedItemId(R.id.nav_borrow); // Example: if this is the borrow activity

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_dashboard) {
                    // Assuming Success is your dashboard activity
                    Intent dashboardIntent = new Intent(BorrowActivity.this, Success.class);
                    startActivity(dashboardIntent);
                    // No Toast here, as you're navigating
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent profileIntent = new Intent(BorrowActivity.this, ProfileActivity.class);
                    startActivity(profileIntent);
                    return true;
                } else if (id == R.id.nav_logout) {
                    performLogout();
                    return true;
                }
                return false;
            }
        });
    }

    public void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        // Ensure dialog_add_item.xml exists and has the correct IDs
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        NumberPicker dialogQtyPicker = dialogView.findViewById(R.id.dialogNumberPickerQty);
        EditText dialogDescription = dialogView.findViewById(R.id.dialogEditTextDescription);
        TextView dialogDateOfTransfer = dialogView.findViewById(R.id.dialogTextViewDateOfTransfer);
        Button dialogDateButton = dialogView.findViewById(R.id.dialogButtonDateOfTransfer);
        EditText dialogLocationFrom = dialogView.findViewById(R.id.dialogEditTextLocationFrom);
        EditText dialogLocationTo = dialogView.findViewById(R.id.dialogEditTextLocationTo);
        // If you have a remarks field in your dialog_add_item.xml, initialize it here:
        // EditText dialogRemarks = dialogView.findViewById(R.id.dialogEditTextRemarks);


        dialogQtyPicker.setMinValue(1);
        dialogQtyPicker.setMaxValue(100);
        dialogQtyPicker.setValue(1); // Default quantity

        // Set current date to the dialog's date field
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        dialogDateOfTransfer.setText(String.format(Locale.getDefault(), "%d.%02d.%02d", year, month + 1, day));

        dialogDateButton.setOnClickListener(v -> openDatePickerDialog(dialogDateOfTransfer));
        // Make the TextView itself clickable for convenience
        dialogDateOfTransfer.setOnClickListener(v -> openDatePickerDialog(dialogDateOfTransfer));


        AlertDialog dialog = builder.create();

        // Custom listener for the positive button to prevent dismiss on validation failure
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", (d, which) -> {}); // Empty for custom listener
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, which) -> d.dismiss());

        dialog.show(); // Show the dialog before setting the custom button listener

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String qty = String.valueOf(dialogQtyPicker.getValue());
            String description = dialogDescription.getText().toString().trim();
            String dateOfTransfer = dialogDateOfTransfer.getText().toString().trim();
            String locationFrom = dialogLocationFrom.getText().toString().trim();
            String locationTo = dialogLocationTo.getText().toString().trim();
            String remarks = ""; // Placeholder for remarks, adjust if you add a field for it

            if (description.isEmpty() || dateOfTransfer.isEmpty() ||
                    locationFrom.isEmpty() || locationTo.isEmpty()) {
                Toast.makeText(this, "Please fill all item fields in the dialog.", Toast.LENGTH_SHORT).show();
            } else {
                // Ensure BorrowRequest.Item constructor matches this signature
                BorrowRequest.Item newItem = new BorrowRequest.Item(
                        qty, description, dateOfTransfer, locationFrom, locationTo, remarks); // Added remarks

                currentItems.add(newItem);
                addItemSummaryRow(newItem);

                Toast.makeText(this, "Item added!", Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // Dismiss dialog only on successful addition
            }
        });
    }

    public void addItemSummaryRow(BorrowRequest.Item item) {
        // Ensure item_summary_row.xml exists and has the correct IDs
        LinearLayout row = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.item_summary_row, itemsContainer, false);

        TextView summaryQty = row.findViewById(R.id.summaryTextViewQty);
        TextView summaryDescription = row.findViewById(R.id.summaryTextViewDescription);
        TextView summaryDateOfTransfer = row.findViewById(R.id.summaryTextViewDateOfTransfer);
        TextView summaryFrom = row.findViewById(R.id.summaryTextViewFrom);
        TextView summaryTo = row.findViewById(R.id.summaryTextViewTo);
        ImageButton removeItemButton = row.findViewById(R.id.removeItemButton);

        summaryQty.setText(item.qty); // item.qty is already a String, no need for String.valueOf()
        summaryDescription.setText(item.description);
        summaryDateOfTransfer.setText(item.dateOfTransfer);
        summaryFrom.setText(item.locationFrom);
        summaryTo.setText(item.locationTo);

        removeItemButton.setOnClickListener(v -> {
            itemsContainer.removeView(row);
            currentItems.remove(item); // Remove from the list
            Toast.makeText(this, "Item removed.", Toast.LENGTH_SHORT).show();
        });

        itemsContainer.addView(row);
    }

    public void submitBorrowRequest() {
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(this, "Device ID not available. Cannot submit request.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate main form fields
        if (editDept.getText().toString().trim().isEmpty() ||
                editName.getText().toString().trim().isEmpty() ||
                editProject.getText().toString().trim().isEmpty() ||
                editVenue.getText().toString().trim().isEmpty() ||
                textDate.getText().toString().trim().isEmpty() ||
                textDate2.getText().toString().trim().isEmpty() ||
                text3.getText().toString().trim().isEmpty() ||
                text4.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill all main form fields.", Toast.LENGTH_LONG).show();
            return;
        }

        // Validate if at least one item has been added
        if (currentItems.isEmpty()) {
            Toast.makeText(this, "Please add at least one item to the request.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create BorrowRequest object
        // Ensure the constructor signature in your BorrowRequest class matches these arguments
        BorrowRequest newRequest = new BorrowRequest(
                textDate.getText().toString().trim(),         // date_submitted
                editDept.getText().toString().trim(),         // department
                editName.getText().toString().trim(),         // borrower_name
                text4.getText().toString().trim(),            // gender
                deviceId,                                     // borrower_id (device ID)
                editProject.getText().toString().trim(),      // project_name
                textDate2.getText().toString().trim(),        // date_of_project
                text3.getText().toString().trim(),            // time_of_project
                editVenue.getText().toString().trim(),        // venue
                "Pending",                                    // status (Default to "Pending" for new requests)
                currentItems                                  // List<Item>
        );

        // Convert BorrowRequest object to JSON
        Gson gson = new Gson();
        String jsonString = gson.toJson(newRequest);
        JSONObject requestJsonObject;
        try {
            requestJsonObject = new JSONObject(jsonString);
            Log.d(TAG, "Request JSON: " + requestJsonObject.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error converting request to JSON", e);
            Toast.makeText(this, "Error processing request data.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "192.168.185.26/E-permit/submit.php"; // Ensure this URL is correct for your API endpoint

        // Create and add the JSON object request to the Volley queue
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestJsonObject,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(BorrowActivity.this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            int requestId = response.getInt("request_id"); // Assuming your PHP returns this
                            Log.d(TAG, "Request submitted with ID: " + requestId);
                            clearForm(); // Clear form on successful submission

                            // Navigate to PendingActivity
                            Intent intent = new Intent(BorrowActivity.this, PendingActivity.class);
                            intent.putExtra("new_request_id", requestId); // Pass ID if needed
                            startActivity(intent);
                            finish(); // Finish current activity
                        } else {
                            Log.e(TAG, "PHP reported error: " + message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error in response: " + e.getMessage(), e);
                        Toast.makeText(BorrowActivity.this, "Error parsing server response.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(BorrowActivity.this, "Network error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void openDatePickerDialog(final TextView targetTextView){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            targetTextView.setText(String.format(Locale.getDefault(), "%d.%02d.%02d", year1, month1 + 1, dayOfMonth));
        }, year, month, day);

        dialog.show();
    }

    private void openTimePickerDialog(final TextView targetTextView) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String amPm;
            int hourFormat;

            if (hourOfDay >= 12) {
                amPm = "PM";
                hourFormat = (hourOfDay > 12) ? hourOfDay - 12 : hourOfDay;
            } else {
                amPm = "AM";
                hourFormat = (hourOfDay == 0) ? 12 : hourOfDay;
            }
            targetTextView.setText(String.format(Locale.getDefault(), "%d:%02d %s", hourFormat, minute1, amPm));
        }, hour, minute, false); // false for 12-hour format

        dialog.show();
    }

    private void openGenderPickerDialog(final TextView targetTextView) {
        String[] genders = {"Male", "Female"}; // Consider moving to strings.xml

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Gender"); // Consider moving to strings.xml
        builder.setItems(genders, (dialog, which) -> {
            targetTextView.setText(genders[which]);
        });

        builder.show();
    }

    private void clearForm() {
        // Reset all form fields to their initial state or empty
        textDate.setText("");
        editDept.setText("");
        editName.setText("");
        text4.setText("");
        editProject.setText("");
        textDate2.setText("");
        text3.setText("");
        editVenue.setText("");
        itemsContainer.removeAllViews(); // Clear all dynamically added item rows
        currentItems.clear(); // Clear the list of items

        // Reset to current date/time and default gender
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        textDate.setText(String.format(Locale.getDefault(), "%d.%02d.%02d", year, month + 1, day));
        textDate2.setText(String.format(Locale.getDefault(), "%d.%02d.%02d", year, month + 1, day));
        String amPm = hour >= 12 ? "PM" : "AM";
        int hourFormat = (hour > 12) ? hour - 12 : (hour == 0 ? 12 : hour);
        text3.setText(String.format(Locale.getDefault(), "%d:%02d %s", hourFormat, minute, amPm));
        text4.setText("Male"); // Default gender
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
                        Intent loginIntent = new Intent(BorrowActivity.this, MainActivity.class);
                        // Clear activity stack to prevent going back to main app activities
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(loginIntent);
                        finish(); // Finish current activity
                        Toast.makeText(BorrowActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
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


    // You need to ensure your BorrowRequest.java and BorrowRequest.Item classes
    // are defined correctly, possibly in a separate file or as nested public static classes.
    // Here's an example of how they *should* look to match this code:
    public static class BorrowRequest implements java.io.Serializable {
        public String date_submitted;
        public String department;
        public String borrower_name;
        public String gender;
        public String borrower_id;
        public String project_name;
        public String date_of_project;
        public String time_of_project;
        public String venue;
        public String status; // Added to match the constructor
        public List<Item> items;

        // Constructor matching the call in submitBorrowRequest()
        public BorrowRequest(String date_submitted, String department, String borrower_name,
                             String gender, String borrower_id, String project_name,
                             String date_of_project, String time_of_project, String venue,
                             String status, List<Item> items) {
            this.date_submitted = date_submitted;
            this.department = department;
            this.borrower_name = borrower_name;
            this.gender = gender;
            this.borrower_id = borrower_id;
            this.project_name = project_name;
            this.date_of_project = date_of_project;
            this.time_of_project = time_of_project;
            this.venue = venue;
            this.status = status;
            this.items = items;
        }

        public static class Item implements java.io.Serializable {
            public String qty; // Changed to String to match NumberPicker output
            public String description;
            public String dateOfTransfer;
            public String locationFrom;
            public String locationTo;
            public String remarks; // Added remarks to match TransferFormActivity's Item constructor

            // Constructor matching the call in showAddItemDialog()
            public Item(String qty, String description, String dateOfTransfer,
                        String locationFrom, String locationTo, String remarks) {
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