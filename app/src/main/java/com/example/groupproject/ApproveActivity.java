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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ApproveActivity extends AppCompatActivity {

    public static final String TAG = "ApproveActivity";

    public static final String UPDATE_STATUS_URL = "http://192.168.100.160/Epermit/update_request_status.php";
    public static final String GET_DETAILS_URL_BASE = "http://192.168.100.160/Epermit/get_request_details.php";

    public TextView tvDateSubmitted, tvDepartment, tvBorrowerName, tvGender, tvProjectName, tvDateOfProject, tvTimeOfProject, tvVenue, tvStatus, tvApprovedBy;
    public LinearLayout itemsDetailContainer;
    public Button btnApprove, btnReject;
    public RequestQueue requestQueue;
    public int currentRequestId = -1;
    private String adminName = "Admin User 1"; // Set your admin name here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_form);

        requestQueue = Volley.newRequestQueue(this);
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
        Log.d(TAG, "Received Request ID: " + currentRequestId);

        if (currentRequestId != -1) {
            loadRequestDetails(currentRequestId);
        } else {
            Toast.makeText(this, "Request ID not found for approval. Cannot load details.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Request ID was -1 from intent.");
            finish();
        }

        btnApprove.setOnClickListener(v -> updateRequestStatus("Approved"));
        btnReject.setOnClickListener(v -> updateRequestStatus("Rejected"));
    }

    public void loadRequestDetails(int requestId) {
        String url = GET_DETAILS_URL_BASE + "?request_id=" + requestId;
        Log.d(TAG, "Loading request details from URL: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Log.d(TAG, "Load Details Response: " + response.toString());

                        if (success) {
                            JSONObject requestJson = response.getJSONObject("request");
                            Gson gson = new Gson();
                            BorrowRequest request = gson.fromJson(requestJson.toString(), BorrowRequest.class);

                            displayRequestDetails(request);

                            if ("Pending".equalsIgnoreCase(request.getStatus())) {
                                btnApprove.setVisibility(View.VISIBLE);
                                btnReject.setVisibility(View.VISIBLE);
                            } else {
                                btnApprove.setVisibility(View.GONE);
                                btnReject.setVisibility(View.GONE);
                            }

                        } else {
                            Toast.makeText(ApproveActivity.this, "Error loading details: " + message, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "PHP error loading details: " + message);
                            finish();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error for details response", e);
                        Toast.makeText(ApproveActivity.this, "Error parsing server response for details.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                },
                error -> {
                    handleVolleyError(error, "loading details");
                    finish();
                });

        requestQueue.add(jsonObjectRequest);
    }

    public void displayRequestDetails(BorrowRequest request) {
        tvDateSubmitted.setText("Date Submitted: " + request.getDateSubmitted());
        tvDepartment.setText("Department: " + request.getDepartment());
        tvBorrowerName.setText("Borrower Name: " + request.getBorrowerName());
        tvGender.setText("Gender: " + request.getGender());
        tvProjectName.setText("Project Name: " + request.getProjectName());
        tvDateOfProject.setText("Date of Project: " + request.getDateOfProject());
        tvTimeOfProject.setText("Time of Project: " + request.getTimeOfProject());
        tvVenue.setText("Venue: " + request.getVenue());
        tvStatus.setText("Status: " + request.getStatus());

        if (request.getApprovedBy() != null && !request.getApprovedBy().isEmpty()) {
            tvApprovedBy.setText("Approved By: " + request.getApprovedBy());
            tvApprovedBy.setVisibility(View.VISIBLE);
        } else {
            tvApprovedBy.setVisibility(View.GONE);
        }

        itemsDetailContainer.removeAllViews();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (BorrowRequest.Item item : request.getItems()) {
                addItemDetailRow(item);
            }
        } else {
            TextView noItemsText = new TextView(this);
            noItemsText.setText("No items listed for this request.");
            noItemsText.setPadding(0, 16, 0, 0);
            itemsDetailContainer.addView(noItemsText);
        }

        if ("Approved".equalsIgnoreCase(request.getStatus())) {
            tvStatus.setTextColor(getResources().getColor(R.color.green));
        } else if ("Rejected".equalsIgnoreCase(request.getStatus())) {
            tvStatus.setTextColor(getResources().getColor(R.color.red));
        } else { // Pending
            tvStatus.setTextColor(getResources().getColor(R.color.orange));
        }
    }

    public void addItemDetailRow(BorrowRequest.Item item) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemRow = inflater.inflate(R.layout.item_detail_row, itemsDetailContainer, false);

        TextView detailQty = itemRow.findViewById(R.id.detailTextViewQty);
        TextView detailDescription = itemRow.findViewById(R.id.detailTextViewDescription);
        TextView detailDateOfTransfer = itemRow.findViewById(R.id.detailTextViewDateOfTransfer);
        TextView detailLocationFrom = itemRow.findViewById(R.id.detailTextViewLocationFrom);
        TextView detailLocationTo = itemRow.findViewById(R.id.detailTextViewLocationTo);

        detailQty.setText("Quantity: " + item.getQty());
        detailDescription.setText("Description: " + item.getDescription());
        detailDateOfTransfer.setText("Date of Transfer: " + item.getDateOfTransfer());
        detailLocationFrom.setText("From: " + item.getLocationFrom());
        detailLocationTo.setText("To: " + item.getLocationTo());

        itemsDetailContainer.addView(itemRow);
    }

    public void updateRequestStatus(String status) {
        Log.d(TAG, "Updating request status for ID: " + currentRequestId);
        Log.d(TAG, "New Status: " + status);
        Log.d(TAG, "Approved By: " + adminName);

        if (currentRequestId == -1 || adminName == null || adminName.isEmpty() || status == null || status.isEmpty()) {
            Toast.makeText(this, "Invalid update parameters.", Toast.LENGTH_LONG).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_STATUS_URL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        String message = jsonResponse.getString("message");
                        Toast.makeText(ApproveActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (success) finish();
                    } catch (JSONException e) {
                        Toast.makeText(ApproveActivity.this, "Invalid response from server", Toast.LENGTH_LONG).show();
                    }
                },
                error -> handleVolleyError(error, "updating status")) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("request_id", String.valueOf(currentRequestId));
                params.put("status", status);
                params.put("approved_by", adminName);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void handleVolleyError(VolleyError error, String action) {
        String message = "Unknown error while " + action + ".";
        if (error.networkResponse != null && error.networkResponse.data != null) {
            String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
            Log.e(TAG, "Volley " + action + " error: " + body);
            message = "Server error: " + body;
        } else if (error.getMessage() != null) {
            message = error.getMessage();
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // BorrowRequest and nested Item class
    public static class BorrowRequest {
        private int requestId;
        private String dateSubmitted;
        private String department;
        private String borrowerName;
        private String gender;
        private String projectName;
        private String dateOfProject;
        private String timeOfProject;
        private String venue;
        private String status;
        private String approvedBy;
        private java.util.List<Item> items;

        public int getRequestId() { return requestId; }
        public String getDateSubmitted() { return dateSubmitted; }
        public String getDepartment() { return department; }
        public String getBorrowerName() { return borrowerName; }
        public String getGender() { return gender; }
        public String getProjectName() { return projectName; }
        public String getDateOfProject() { return dateOfProject; }
        public String getTimeOfProject() { return timeOfProject; }
        public String getVenue() { return venue; }
        public String getStatus() { return status; }
        public String getApprovedBy() { return approvedBy; }
        public java.util.List<Item> getItems() { return items; }

        public static class Item {
            private int qty;
            private String description;
            private String dateOfTransfer;
            private String locationFrom;
            private String locationTo;

            public int getQty() { return qty; }
            public String getDescription() { return description; }
            public String getDateOfTransfer() { return dateOfTransfer; }
            public String getLocationFrom() { return locationFrom; }
            public String getLocationTo() { return locationTo; }
        }
    }
}
