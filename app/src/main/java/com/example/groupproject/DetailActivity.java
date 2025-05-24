package com.example.groupproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private static final String BASE_URL = "http://192.168.254.149/Epermit/get_request_details.php";
    private TextView tvDateSubmitted, tvDepartment, tvBorrowerName, tvGender, tvProjectName,
            tvDateOfProject, tvTimeOfProject, tvVenue, tvStatus, tvApprovedBy;
    private LinearLayout itemsDetailContainer;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);

        requestQueue = Volley.newRequestQueue(this);

        tvDateSubmitted = findViewById(R.id.tvDateSubmitted);
        tvDepartment = findViewById(R.id.tvDepartment);
        tvBorrowerName = findViewById(R.id.tvBorrowerName);
        tvGender = findViewById(R.id.tvGender);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvDateOfProject = findViewById(R.id.tvDateOfProject);
        tvTimeOfProject = findViewById(R.id.tvTimeOfProject);
        tvVenue = findViewById(R.id.tvVenue);
        tvStatus = findViewById(R.id.tvStatus);
        tvApprovedBy = findViewById(R.id.tvApprovedBy);
        itemsDetailContainer = findViewById(R.id.itemsDetailContainer);

        int requestId = getIntent().getIntExtra("request_id", -1);

        if (requestId != -1) {
            loadRequestDetails(requestId);
        } else {
            Toast.makeText(this, "Request ID not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
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
                        } else {
                            Toast.makeText(DetailActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "PHP reported error: " + message);
                            finish();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error in response", e);
                        Toast.makeText(DetailActivity.this, "Error parsing server response.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(DetailActivity.this, "Network error. Could not load details.", Toast.LENGTH_LONG).show();
                    finish();
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void displayRequestDetails(BorrowRequest request) {
        // Corrected: Using getter methods
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

        if ("Approved".equals(request.getStatus())) {
            tvStatus.setTextColor(getResources().getColor(R.color.green));
        } else if ("Rejected".equals(request.getStatus())) {
            tvStatus.setTextColor(getResources().getColor(R.color.red));
        } else {
            tvStatus.setTextColor(getResources().getColor(R.color.orange));
        }
    }

    private void addItemDetailRow(BorrowRequest.Item item) {
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
}