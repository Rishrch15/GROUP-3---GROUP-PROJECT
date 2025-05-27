package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PendingActivity extends AppCompatActivity {

    private static final String TAG = "PendingActivity";
    private static final String BASE_URL = "http://192.168.185.219/EPermit/";

    private LinearLayout requestsContainer;
    private TextView noRequestsText;
    private RequestQueue requestQueue;
    private String currentBorrowerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_request_list);

        requestsContainer = findViewById(R.id.requestsContainer);
        noRequestsText = findViewById(R.id.noRequestsText);

        requestQueue = Volley.newRequestQueue(this);
        currentBorrowerId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "Current Borrower ID (Device ID): " + currentBorrowerId);

        loadRequests("Pending");
    }

    private void loadRequests(String statusFilter) {
        requestsContainer.removeAllViews();
        noRequestsText.setVisibility(View.VISIBLE);
        noRequestsText.setText("Loading " + statusFilter.toLowerCase() + " requests...");

        try {
            String url = BASE_URL + "get_requests.php?status=" +
                    URLEncoder.encode(statusFilter, StandardCharsets.UTF_8.toString()) +
                    "&borrower_id=" + URLEncoder.encode(currentBorrowerId, StandardCharsets.UTF_8.toString());

            Log.d(TAG, "Fetching requests from URL: " + url);

            // Using StringRequest instead of JsonObjectRequest to better handle errors
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            // First check if response contains HTML tags (indicating PHP error)
                            if (response.contains("<br") || response.startsWith("<")) {
                                handleErrorResponse("Server returned HTML error: " + response);
                                return;
                            }

                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            String message = jsonResponse.getString("message");

                            if (success) {
                                JSONArray requestsArray = jsonResponse.getJSONArray("requests");
                                if (requestsArray.length() > 0) {
                                    requestsContainer.removeAllViews();
                                    noRequestsText.setVisibility(View.GONE);

                                    Gson gson = new Gson();
                                    Type listType = new TypeToken<ArrayList<BorrowRequest>>(){}.getType();
                                    List<BorrowRequest> requests = gson.fromJson(requestsArray.toString(), listType);

                                    for (BorrowRequest request : requests) {
                                        addRequestSummaryToContainer(request);
                                    }
                                } else {
                                    noRequestsText.setText("No " + statusFilter.toLowerCase() + " requests found.");
                                    noRequestsText.setVisibility(View.VISIBLE);
                                }
                            } else {
                                handleErrorResponse("Server error: " + message);
                            }
                        } catch (JSONException e) {
                            handleErrorResponse("JSON parsing error: " + e.getMessage() + "\nResponse: " + response);
                        } catch (JsonSyntaxException e) {
                            handleErrorResponse("GSON parsing error: " + e.getMessage() + "\nResponse: " + response);
                        }
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String responseData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            handleErrorResponse("Network error: " + error.getMessage() + "\nResponse: " + responseData);
                        } else {
                            handleErrorResponse("Network error: " + error.getMessage());
                        }
                    });

            requestQueue.add(stringRequest);
        } catch (UnsupportedEncodingException e) {
            handleErrorResponse("URL encoding error: " + e.getMessage());
        }
    }

    private void handleErrorResponse(String errorMessage) {
        Log.e(TAG, errorMessage);
        runOnUiThread(() -> {
            noRequestsText.setText("Error loading requests. Please try again.");
            noRequestsText.setVisibility(View.VISIBLE);
            Toast.makeText(PendingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        });
    }

    private void addRequestSummaryToContainer(BorrowRequest request) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View requestSummaryView = inflater.inflate(R.layout.item_request_summary, requestsContainer, false);

        TextView summaryTitle = requestSummaryView.findViewById(R.id.summaryTitle);
        TextView summaryStatus = requestSummaryView.findViewById(R.id.summaryStatus);
        TextView summaryDate = requestSummaryView.findViewById(R.id.summaryDate);

        summaryTitle.setText("Project: " + request.getProjectName() + " by " + request.getBorrowerName());
        summaryStatus.setText("Status: " + request.getStatus());
        summaryDate.setText("Submitted: " + request.getDateSubmitted());

        if ("Approved".equalsIgnoreCase(request.getStatus())) {
            summaryStatus.setTextColor(getResources().getColor(R.color.green, null));
        } else if ("Rejected".equalsIgnoreCase(request.getStatus())) {
            summaryStatus.setTextColor(getResources().getColor(R.color.red, null));
        } else {
            summaryStatus.setTextColor(getResources().getColor(R.color.orange, null));
        }

        requestSummaryView.setOnClickListener(v -> {
            Intent intent = new Intent(PendingActivity.this, DetailActivity.class);
            intent.putExtra("request_id", request.getRequestId());
            startActivity(intent);
        });

        requestsContainer.addView(requestSummaryView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests("Pending");
    }

    public static class BorrowRequest implements Serializable {
        @SerializedName("request_id")
        private int requestId;

        @SerializedName("date_submitted")
        private String dateSubmitted;

        @SerializedName("department")
        private String department;

        @SerializedName("borrower_name")
        private String borrowerName;

        @SerializedName("gender")
        private String gender;

        @SerializedName("borrower_id") // Matches your database column (user_id was in your earlier schema)
        private String borrowerId;

        @SerializedName("project_name")
        private String projectName;

        @SerializedName("date_of_project")
        private String dateOfProject;

        @SerializedName("time_of_project")
        private String timeOfProject;

        @SerializedName("venue")
        private String venue;

        @SerializedName("status")
        private String status;

        @SerializedName("approved_by") // Field from your DB schema
        private String approvedBy;

        @SerializedName("items") // This field is for the list of items
        private List<Item> items;

        // Constructor for when creating a new request (without request_id initially)
        public BorrowRequest(String dateSubmitted, String department, String borrowerName,
                             String gender, String borrowerId, String projectName,
                             String dateOfProject, String timeOfProject, String venue,
                             String status, List<Item> items) {
            this.dateSubmitted = dateSubmitted;
            this.department = department;
            this.borrowerName = borrowerName;
            this.gender = gender;
            this.borrowerId = borrowerId;
            this.projectName = projectName;
            this.dateOfProject = dateOfProject;
            this.timeOfProject = timeOfProject;
            this.venue = venue;
            this.status = status;
            this.items = items;
            // request_id and approved_by are set by the database or later updates
        }

        // Constructor for when fetching from database (includes request_id and approved_by)
        public BorrowRequest(int requestId, String dateSubmitted, String department, String borrowerName,
                             String gender, String borrowerId, String projectName,
                             String dateOfProject, String timeOfProject, String venue,
                             String status, String approvedBy, List<Item> items) {
            this.requestId = requestId;
            this.dateSubmitted = dateSubmitted;
            this.department = department;
            this.borrowerName = borrowerName;
            this.gender = gender;
            this.borrowerId = borrowerId;
            this.projectName = projectName;
            this.dateOfProject = dateOfProject;
            this.timeOfProject = timeOfProject;
            this.venue = venue;
            this.status = status;
            this.approvedBy = approvedBy;
            this.items = items;
        }

        // --- Getters ---
        public int getRequestId() {
            return requestId;
        }

        public String getDateSubmitted() {
            return dateSubmitted;
        }

        public String getDepartment() {
            return department;
        }

        public String getBorrowerName() {
            return borrowerName;
        }

        public String getGender() {
            return gender;
        }

        public String getBorrowerId() {
            return borrowerId;
        }

        public String getProjectName() {
            return projectName;
        }

        public String getDateOfProject() {
            return dateOfProject;
        }

        public String getTimeOfProject() {
            return timeOfProject;
        }

        public String getVenue() {
            return venue;
        }

        public String getStatus() {
            return status;
        }

        public String getApprovedBy() {
            return approvedBy;
        }

        public List<Item> getItems() {
            return items;
        }

        // --- Item Sub-class ---
        public static class Item implements Serializable {
            @SerializedName("item_id")
            private int itemId; // If you fetch item IDs from DB
            @SerializedName("qty")
            private String qty;
            @SerializedName("description")
            private String description;
            @SerializedName("dateOfTransfer")
            private String dateOfTransfer;
            @SerializedName("locationFrom")
            private String locationFrom;
            @SerializedName("locationTo")
            private String locationTo;
            @SerializedName("remarks")
            private String remarks;

            // Constructor for creating new items
            public Item(String qty, String description, String dateOfTransfer,
                        String locationFrom, String locationTo, String remarks) {
                this.qty = qty;
                this.description = description;
                this.dateOfTransfer = dateOfTransfer;
                this.locationFrom = locationFrom;
                this.locationTo = locationTo;
                this.remarks = remarks;
            }

            // Constructor for fetching items from DB
            public Item(int itemId, String qty, String description, String dateOfTransfer,
                        String locationFrom, String locationTo, String remarks) {
                this.itemId = itemId;
                this.qty = qty;
                this.description = description;
                this.dateOfTransfer = dateOfTransfer;
                this.locationFrom = locationFrom;
                this.locationTo = locationTo;
                this.remarks = remarks;
            }

            // --- Getters for Item ---
            public int getItemId() {
                return itemId;
            }

            public String getQty() {
                return qty;
            }

            public String getDescription() {
                return description;
            }

            public String getDateOfTransfer() {
                return dateOfTransfer;
            }

            public String getLocationFrom() {
                return locationFrom;
            }

            public String getLocationTo() {
                return locationTo;
            }

            public String getRemarks() {
                return remarks;
            }
        }
    }
}