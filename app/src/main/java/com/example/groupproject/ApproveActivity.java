package com.example.groupproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ApproveActivity extends AppCompatActivity { // Using your class name

    private static final String TAG = "ApproveActivity";
    private static final String BASE_URL = "http://192.168.254.149/Epermit/update_request_status.php"; // Your WampServer IP

    private TextView tvDateSubmitted, tvDepartment, tvBorrowerName, tvGender, tvProjectName,
            tvDateOfProject, tvTimeOfProject, tvVenue, tvStatus, tvApprovedBy;
    private LinearLayout itemsDetailContainer;
    private Button btnApprove, btnReject;

    private RequestQueue requestQueue;
    private int currentRequestId = -1; // Store the request ID

    // For demo purposes, hardcode an admin name. In a real app, this comes from admin login.
    private String adminName = "Admin User 1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_form); // Assuming this is your XML for approval form

        requestQueue = Volley.newRequestQueue(this);

        // Initialize TextViews
        tvDateSubmitted = findViewById(R.id.detailDateSubmitted);
        tvDepartment = findViewById(R.id.detailDepartment);
        tvBorrowerName = findViewById(R.id.detailBorrowerName);
        tvGender = findViewById(R.id.detailGender);
        tvProjectName = findViewById(R.id.detailProjectName);
        tvDateOfProject = findViewById(R.id.detailDateOfProject);
        tvTimeOfProject = findViewById(R.id.detailTimeOfProject);
        tvVenue = findViewById(R.id.detailVenue);
        tvStatus = findViewById(R.id.detailStatus);
        tvApprovedBy = findViewById(R.id.detailApprovedBy);

        itemsDetailContainer = findViewById(R.id.detailItemsContainer);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);

        currentRequestId = getIntent().getIntExtra("request_id", -1);

        if (currentRequestId != -1) {
            loadRequestDetails(currentRequestId);
        } else {
            Toast.makeText(this, "Request ID not found for approval.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnApprove.setOnClickListener(v -> updateRequestStatus("Approved"));
        btnReject.setOnClickListener(v -> updateRequestStatus("Rejected"));
    }

    private void loadRequestDetails(int requestId) {
        String url = BASE_URL + "get_request_details.php?request_id=" + requestId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        if (success) {
                            JSONObject requestJson = response.getJSONObject("request");
                            Gson gson = new Gson();
                            BorrowRequest request = gson.fromJson(requestJson.toString(), BorrowRequest.class);

                            displayRequestDetails(request);
                            // Only show buttons if status is Pending
                            if ("Pending".equals(request.getStatus())) { // Using getter
                                btnApprove.setVisibility(View.VISIBLE);
                                btnReject.setVisibility(View.VISIBLE);
                            } else {
                                btnApprove.setVisibility(View.GONE);
                                btnReject.setVisibility(View.GONE);
                            }

                        } else {
                            Toast.makeText(ApproveActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "PHP reported error: " + message);
                            finish();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error in response", e);
                        Toast.makeText(ApproveActivity.this, "Error parsing server response.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(ApproveActivity.this, "Network error. Could not load details.", Toast.LENGTH_LONG).show();
                    finish();
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void displayRequestDetails(BorrowRequest request) {
        // FIX: Using getter methods for all BorrowRequest fields
        tvDateSubmitted.setText("Date Submitted: " + request.getDateSubmitted());
        tvDepartment.setText("Department: " + request.getDepartment());
        tvBorrowerName.setText("Borrower Name: " + request.getBorrowerName());
        tvGender.setText("Gender: " + request.getGender());
        tvProjectName.setText("Project Name: " + request.getProjectName());
        tvDateOfProject.setText("Date of Project: " + request.getDateOfProject());
        tvTimeOfProject.setText("Time of Project: " + request.getTimeOfProject());
        tvVenue.setText("Venue: " + request.getVenue());
        tvStatus.setText("Status: " + request.getStatus());

        if (request.getApprovedBy() != null && !request.getApprovedBy().isEmpty()) { // Using getter
            tvApprovedBy.setText("Approved By: " + request.getApprovedBy());
            tvApprovedBy.setVisibility(View.VISIBLE);
        } else {
            tvApprovedBy.setVisibility(View.GONE);
        }

        itemsDetailContainer.removeAllViews();
        if (request.getItems() != null && !request.getItems().isEmpty()) { // Using getter
            for (BorrowRequest.Item item : request.getItems()) { // Using getter
                addItemDetailRow(item);
            }
        } else {
            TextView noItemsText = new TextView(this);
            noItemsText.setText("No items listed for this request.");
            noItemsText.setPadding(0, 16, 0, 0);
            itemsDetailContainer.addView(noItemsText);
        }

        // Set status color
        if ("Approved".equals(request.getStatus())) {
            tvStatus.setTextColor(getResources().getColor(R.color.green));
        } else if ("Rejected".equals(request.getStatus())) {
            tvStatus.setTextColor(getResources().getColor(R.color.red));
        } else { // Pending
            tvStatus.setTextColor(getResources().getColor(R.color.orange));
        }
    }

    private void addItemDetailRow(BorrowRequest.Item item) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemRow = inflater.inflate(R.layout.item_detail_row, itemsDetailContainer, false); // Using item_detail_row

        // Accessing Item fields directly as they are public in the Item inner class
        TextView detailQty = itemRow.findViewById(R.id.detailTextViewQty);
        TextView detailDescription = itemRow.findViewById(R.id.detailTextViewDescription);
        TextView detailDateOfTransfer = itemRow.findViewById(R.id.detailTextViewDateOfTransfer);
        TextView detailLocationFrom = itemRow.findViewById(R.id.detailTextViewLocationFrom);
        TextView detailLocationTo = itemRow.findViewById(R.id.detailTextViewLocationTo);

        // FIX: Using getters for Item fields as well, good practice even if they are public
        detailQty.setText("Quantity: " + item.getQty());
        detailDescription.setText("Description: " + item.getDescription());
        detailDateOfTransfer.setText("Date of Transfer: " + item.getDateOfTransfer());
        detailLocationFrom.setText("From: " + item.getLocationFrom());
        detailLocationTo.setText("To: " + item.getLocationTo());

        itemsDetailContainer.addView(itemRow);
    }

    private void updateRequestStatus(String status) {
        String url = BASE_URL + "update_request_status.php";

        Map<String, String> params = new HashMap<>();
        params.put("request_id", String.valueOf(currentRequestId));
        params.put("status", status);
        params.put("approved_by", adminName); // The name of the admin who approved/rejected

        JSONObject jsonParams = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonParams,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(ApproveActivity.this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            // After update, navigate back to the admin's To Approve List
                            finish(); // Close this activity
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error in response", e);
                        Toast.makeText(ApproveActivity.this, "Error parsing server response.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error updating status: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(ApproveActivity.this, "Network error updating status: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });

        requestQueue.add(jsonObjectRequest);
    }
}