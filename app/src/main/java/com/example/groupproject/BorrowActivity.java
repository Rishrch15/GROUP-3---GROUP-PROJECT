package com.example.groupproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.NumberPicker;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BorrowActivity extends AppCompatActivity {

    private static final String TAG = "BorrowActivity";
    private static final String BASE_URL = "http://10.0.2.2/borrow_api/";

    TextView textDate, textDate2, text3, text4;
    EditText editDept, editName, editProject, editVenue;
    LinearLayout itemsContainer;
    Button addItemBtn, submitBtn, button, button2, button3, button4;

    private List<BorrowRequest.Item> currentItems = new ArrayList<>();
    private RequestQueue requestQueue;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_form);

        requestQueue = Volley.newRequestQueue(this);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "Device ID: " + deviceId);

        textDate = findViewById(R.id.showText);
        button = findViewById(R.id.dateButton);
        editDept = findViewById(R.id.editTextDepartment);
        editName = findViewById(R.id.editTextBorrowerName);
        text4 = findViewById(R.id.showText4);
        button4 = findViewById(R.id.genderButton);
        editProject = findViewById(R.id.editTextProjectName);
        textDate2 = findViewById(R.id.showText2);
        button2 = findViewById(R.id.dateButton2);
        text3 = findViewById(R.id.showText3);
        button3 = findViewById(R.id.timeButton);
        editVenue = findViewById(R.id.editTextVenue);

        itemsContainer = findViewById(R.id.itemsContainer);
        addItemBtn = findViewById(R.id.add_item_button);
        submitBtn = findViewById(R.id.buttonSubmit);

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

        addItemBtn.setOnClickListener(v -> showAddItemDialog());
        submitBtn.setOnClickListener(v -> submitBorrowRequest());

        button.setOnClickListener(v -> openDatePickerDialog(textDate));
        button2.setOnClickListener(v -> openDatePickerDialog(textDate2));
        button3.setOnClickListener(v -> openTimePickerDialog(text3));
        button4.setOnClickListener(v -> openGenderPickerDialog(text4));
    }

    private void showAddItemDialog() {
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

        dialogQtyPicker.setMinValue(1);
        dialogQtyPicker.setMaxValue(100);
        dialogQtyPicker.setValue(1);

        dialogDateButton.setOnClickListener(v -> openDatePickerDialog(dialogDateOfTransfer));

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        dialogDateOfTransfer.setText(String.format(Locale.getDefault(), "%d.%02d.%02d", year, month + 1, day));

        AlertDialog dialog = builder.create();

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", (d, which) -> {});
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, which) -> d.dismiss());

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String qty = String.valueOf(dialogQtyPicker.getValue());
            String description = dialogDescription.getText().toString().trim();
            String dateOfTransfer = dialogDateOfTransfer.getText().toString().trim();
            String locationFrom = dialogLocationFrom.getText().toString().trim();
            String locationTo = dialogLocationTo.getText().toString().trim();

            if (description.isEmpty() || dateOfTransfer.isEmpty() ||
                    locationFrom.isEmpty() || locationTo.isEmpty()) {
                Toast.makeText(this, "Please fill all item fields in the dialog.", Toast.LENGTH_SHORT).show();
            } else {
                BorrowRequest.Item newItem = new BorrowRequest.Item(
                        qty, description, dateOfTransfer, locationFrom, locationTo);

                currentItems.add(newItem);
                addItemSummaryRow(newItem);

                Toast.makeText(this, "Item added!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void addItemSummaryRow(BorrowRequest.Item item) {
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
            itemsContainer.removeView(row);
            currentItems.remove(item);
            Toast.makeText(this, "Item removed.", Toast.LENGTH_SHORT).show();
        });

        itemsContainer.addView(row);
    }

    private void submitBorrowRequest() {
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(this, "Device ID not available. Cannot submit request.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (editDept.getText().toString().isEmpty() ||
                editName.getText().toString().isEmpty() ||
                editProject.getText().toString().isEmpty() ||
                editVenue.getText().toString().isEmpty() ||
                textDate.getText().toString().isEmpty() ||
                textDate2.getText().toString().isEmpty() ||
                text3.getText().toString().isEmpty() ||
                text4.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all main form fields.", Toast.LENGTH_LONG).show();
            return;
        }

        if (currentItems.isEmpty()) {
            Toast.makeText(this, "Please add at least one item to the request.", Toast.LENGTH_SHORT).show();
            return;
        }

        // FIX APPLIED HERE: The constructor matches the definition in BorrowRequest.java
        BorrowRequest newRequest = new BorrowRequest(
                textDate.getText().toString(),        // 1. date_submitted
                editDept.getText().toString(),        // 2. department
                editName.getText().toString(),        // 3. borrower_name
                text4.getText().toString(),           // 4. gender
                deviceId,                             // 5. borrower_id (device ID)
                editProject.getText().toString(),     // 6. project_name
                textDate2.getText().toString(),       // 7. date_of_project
                text3.getText().toString(),           // 8. time_of_project
                editVenue.getText().toString(),       // 9. venue
                "Pending",                            // 10. status (the missing String argument from previous errors)
                currentItems                          // 11. List<Item>
        );

        Gson gson = new Gson();
        String jsonString = gson.toJson(newRequest);
        JSONObject requestJsonObject;
        try {
            requestJsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.e(TAG, "Error converting request to JSON", e);
            Toast.makeText(this, "Error processing request data.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "add_request.php";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestJsonObject,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(BorrowActivity.this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            int requestId = response.getInt("request_id");
                            Log.d(TAG, "Request submitted with ID: " + requestId);
                            clearForm();

                            Intent intent = new Intent(BorrowActivity.this, PendingActivity.class);
                            intent.putExtra("new_request_id", requestId);
                            startActivity(intent);

                        } else {
                            Log.e(TAG, "PHP reported error: " + message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error in response", e);
                        Toast.makeText(BorrowActivity.this, "Error parsing server response.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(BorrowActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
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
        }, hour, minute, false);

        dialog.show();
    }

    private void openGenderPickerDialog(final TextView targetTextView) {
        String[] genders = {"Male", "Female"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Gender");
        builder.setItems(genders, (dialog, which) -> {
            targetTextView.setText(genders[which]);
        });

        builder.show();
    }

    private void clearForm() {
        textDate.setText("");
        editDept.setText("");
        editName.setText("");
        text4.setText("");
        editProject.setText("");
        textDate2.setText("");
        text3.setText("");
        editVenue.setText("");
        itemsContainer.removeAllViews();
        currentItems.clear();
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
    }
}