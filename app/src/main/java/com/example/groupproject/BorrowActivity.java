package com.example.groupproject;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.NumberPicker;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BorrowActivity extends AppCompatActivity {

    private static final String TAG = "BorrowActivity";

    private static final String BASE_URL = "http://192.168.100.160/EPermit/";

    TextView textDate, textDate2, text3, text4;
    EditText editProject, editVenue;
    LinearLayout itemsContainer;
    Button addItemBtn, submitBtn, button, button2, button3, button4;
    public BottomNavigationView bottomNavigationView;

    public List<BorrowRequest.Item> currentItems = new ArrayList<>();
    public RequestQueue requestQueue;
    public String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_form);

        requestQueue = Volley.newRequestQueue(this);
        // Ensure you have READ_PHONE_STATE permission if device ID is sensitive,
        // or consider alternative unique identifiers if this is for user tracking.
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "Device ID: " + deviceId);

        textDate = findViewById(R.id.showText);
        button = findViewById(R.id.dateButton);
        editProject = findViewById(R.id.editTextProjectName);
        textDate2 = findViewById(R.id.showText2);
        button2 = findViewById(R.id.dateButton2);
        text3 = findViewById(R.id.showText3);
        button3 = findViewById(R.id.timeButton);
        editVenue = findViewById(R.id.editTextVenue);

        itemsContainer = findViewById(R.id.itemsContainer);
        addItemBtn = findViewById(R.id.add_item_button);
        submitBtn = findViewById(R.id.buttonSubmit);

        // Initialize dates and times
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

        // Set listeners
        addItemBtn.setOnClickListener(v -> showAddItemDialog());
        submitBtn.setOnClickListener(v -> submitBorrowRequest());

        button.setOnClickListener(v -> openDatePickerDialog(textDate));
        button2.setOnClickListener(v -> openDatePickerDialog(textDate2));
        button3.setOnClickListener(v -> openTimePickerDialog(text3));

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                Intent dashboardIntent = new Intent(BorrowActivity.this, Success.class); // Assuming Success is your dashboard
                startActivity(dashboardIntent);
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
        });
    }

    public void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        NumberPicker dialogQtyPicker = dialogView.findViewById(R.id.dialogNumberPickerQty);
        EditText dialogDescription = dialogView.findViewById(R.id.dialogEditTextDescription);
        TextView dialogDateOfTransfer = dialogView.findViewById(R.id.dialogTextViewDateOfTransfer);
        Button dialogDateButton = dialogView.findViewById(R.id.dialogButtonDateOfTransfer);
        EditText dialogLocationFrom = dialogView.findViewById(R.id.dialogEditTextLocationFrom);
        EditText dialogLocationTo = dialogView.findViewById(R.id.dialogEditTextLocationTo);
        // EditText dialogRemarks = dialogView.findViewById(R.id.dialogEditTextRemarks); // If you decide to add remarks to the dialog

        dialogQtyPicker.setMinValue(1);
        dialogQtyPicker.setMaxValue(100);
        dialogQtyPicker.setValue(1);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        dialogDateOfTransfer.setText(String.format(Locale.getDefault(), "%d.%02d.%02d", year, month + 1, day));

        dialogDateButton.setOnClickListener(v -> openDatePickerDialog(dialogDateOfTransfer));
        dialogDateOfTransfer.setOnClickListener(v -> openDatePickerDialog(dialogDateOfTransfer)); // Allow clicking the text too

        AlertDialog dialog = builder.create();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", (d, which) -> { /* Handled by custom listener below */ });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, which) -> d.dismiss());
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String qty = String.valueOf(dialogQtyPicker.getValue());
            String description = dialogDescription.getText().toString().trim();
            String dateOfTransfer = dialogDateOfTransfer.getText().toString().trim();
            String locationFrom = dialogLocationFrom.getText().toString().trim();
            String locationTo = dialogLocationTo.getText().toString().trim();
            String remarks = "";

            if (description.isEmpty() || dateOfTransfer.isEmpty() ||
                    locationFrom.isEmpty() || locationTo.isEmpty()) {
                Toast.makeText(this, "Please fill all item fields in the dialog.", Toast.LENGTH_SHORT).show();
            } else {
                BorrowRequest.Item newItem = new BorrowRequest.Item(
                        qty, description, dateOfTransfer, locationFrom, locationTo, remarks);

                currentItems.add(newItem);
                addItemSummaryRow(newItem);

                Toast.makeText(this, "Item added!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    public void addItemSummaryRow(BorrowRequest.Item item) {
        // Inflate the item_summary_row layout
        LinearLayout row = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.item_summary_row, itemsContainer, false);

        TextView summaryQty = row.findViewById(R.id.summaryTextViewQty);
        TextView summaryDescription = row.findViewById(R.id.summaryTextViewDescription);
        TextView summaryDateOfTransfer = row.findViewById(R.id.summaryTextViewDateOfTransfer);
        TextView summaryFrom = row.findViewById(R.id.summaryTextViewFrom);
        TextView summaryTo = row.findViewById(R.id.summaryTextViewTo);
        ImageButton removeItemButton = row.findViewById(R.id.removeItemButton);

        summaryQty.setText(item.qty);
        summaryDescription.setText(item.description);
        summaryDateOfTransfer.setText(item.dateOfTransfer);
        summaryFrom.setText(item.locationFrom);
        summaryTo.setText(item.locationTo);

        removeItemButton.setOnClickListener(v -> {
            itemsContainer.removeView(row); // Remove the view from the layout
            currentItems.remove(item); // Remove the item from the list
            Toast.makeText(this, "Item removed.", Toast.LENGTH_SHORT).show();
        });

        itemsContainer.addView(row); // Add the new row to the container
    }

    public void submitBorrowRequest() {
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(this, "Device ID not available. Cannot submit request.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate main form fields
        if (
                editProject.getText().toString().trim().isEmpty() ||
                editVenue.getText().toString().trim().isEmpty() ||
                textDate.getText().toString().trim().isEmpty() ||
                textDate2.getText().toString().trim().isEmpty() ||
                text3.getText().toString().trim().isEmpty() ||
                text4.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill all main form fields.", Toast.LENGTH_LONG).show();
            return;
        }

        // Validate items
        if (currentItems.isEmpty()) {
            Toast.makeText(this, "Please add at least one item to the request.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log all collected data before sending
        Log.d(TAG, "Form Data - Date Submitted: " + textDate.getText().toString().trim());
        Log.d(TAG, "Form Data - Project Name: " + editProject.getText().toString().trim());
        Log.d(TAG, "Form Data - Date of Project: " + textDate2.getText().toString().trim());
        Log.d(TAG, "Form Data - Time of Project: " + text3.getText().toString().trim());
        Log.d(TAG, "Form Data - Venue: " + editVenue.getText().toString().trim());
        Log.d(TAG, "Form Data - Number of Items: " + currentItems.size());


        // Create BorrowRequest object
        BorrowRequest newRequest = new BorrowRequest(
                textDate.getText().toString().trim(),
                text4.getText().toString().trim(),
                deviceId,
                editProject.getText().toString().trim(),
                textDate2.getText().toString().trim(),
                editVenue.getText().toString().trim(),
                "Pending", // Initial status
                currentItems
        );

        Gson gson = new Gson();
        String jsonString = gson.toJson(newRequest);
        JSONObject requestJsonObject;
        try {
            requestJsonObject = new JSONObject(jsonString);
            Log.d(TAG, "Request JSON (Full): " + requestJsonObject.toString(2)); // Use 2 for pretty printing in logs
        } catch (JSONException e) {
            Log.e(TAG, "Error converting request to JSON", e);
            Toast.makeText(this, "Error processing request data locally.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "add_borrow_request.php";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestJsonObject,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(BorrowActivity.this, message, Toast.LENGTH_LONG).show();

                        if (success) {
                            int requestId = response.optInt("request_id", -1); // Use optInt to safely get ID
                            Log.d(TAG, "Request submitted successfully. Server Response: " + response.toString());
                            Log.d(TAG, "New Request ID: " + requestId);
                            clearForm(); // Clear the form after successful submission

                            Intent intent = new Intent(BorrowActivity.this, PendingActivity.class);
                            intent.putExtra("new_request_id", requestId); // Pass the new ID if needed
                            startActivity(intent);
                            finish(); // Finish BorrowActivity so it's not on the back stack
                        } else {
                            Log.e(TAG, "Server reported error: " + message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error in server response: " + e.getMessage(), e);
                        Toast.makeText(BorrowActivity.this, "Error parsing server response. Check server logs.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        String responseData = new String(error.networkResponse.data);
                        Log.e(TAG, "Response Data (Raw from server): " + responseData); // Crucial for debugging
                        Toast.makeText(BorrowActivity.this, "Server Error: " + responseData, Toast.LENGTH_LONG).show();
                    } else {
                        String errorMessage = "Network error: ";
                        if (error.getMessage() != null) {
                            errorMessage += error.getMessage();
                        } else {
                            errorMessage += "Unknown error. Check internet connection and server URL.";
                        }
                        Toast.makeText(BorrowActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    public void openDatePickerDialog(final TextView targetTextView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            targetTextView.setText(String.format(Locale.getDefault(), "%d.%02d.%02d", year1, month1 + 1, dayOfMonth));
        }, year, month, day);

        // Disable past dates
        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        // Disable dates beyond 1 year from now
        Calendar maxDate = (Calendar) calendar.clone();
        maxDate.add(Calendar.YEAR, 1);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

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
        }, hour, minute, false);

        dialog.show();
    }


    public void clearForm() {

        editProject.setText("");
        editVenue.setText("");
        itemsContainer.removeAllViews(); // Remove all dynamically added item rows
        currentItems.clear(); // Clear the list of items

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

// ====== DATE PICKER SETUP ======
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Update displayed date when selected
                    textDate.setText(String.format(Locale.getDefault(), "%d.%02d.%02d", selectedYear, selectedMonth + 1, selectedDay));
                    textDate2.setText(String.format(Locale.getDefault(), "%d.%02d.%02d", selectedYear, selectedMonth + 1, selectedDay));
                },
                year, month, day
        );

    }

    public void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear(); // Clear all session data
                    editor.apply(); // Apply changes asynchronously

                    // Navigate to the login screen and clear activity stack
                    Intent loginIntent = new Intent(BorrowActivity.this, MainActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    finish(); // Finish current activity
                    Toast.makeText(BorrowActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss(); // Dismiss the dialog if "No" is clicked
                })
                .setIcon(android.R.drawable.ic_dialog_alert) // Add an alert icon
                .show();
    }

    // --- BorrowRequest and Item Classes ---
    public static class BorrowRequest implements Serializable {
        public String date_submitted;

        public String borrower_id; // Maps to deviceId
        public String project_name;
        public String date_of_project;
        public String time_of_project;
        public String venue;
        public String status;
        public List<Item> items; // List of Item objects

        public BorrowRequest(String date_submitted, String borrower_id, String project_name,
                             String date_of_project, String time_of_project, String venue,
                             String status, List<Item> items) {
            this.date_submitted = date_submitted;
            this.borrower_id = borrower_id;
            this.project_name = project_name;
            this.date_of_project = date_of_project;
            this.time_of_project = time_of_project;
            this.venue = venue;
            this.status = status;
            this.items = items;
        }

        public static class Item implements Serializable {
            // Ensure these variable names exactly match the JSON keys for each item
            public String qty;
            public String description;
            public String dateOfTransfer; // Note: PHP should expect 'dateOfTransfer'
            public String locationFrom;
            public String locationTo;
            public String remarks;

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