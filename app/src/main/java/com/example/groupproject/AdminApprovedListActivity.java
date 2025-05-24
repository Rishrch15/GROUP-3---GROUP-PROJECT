package com.example.groupproject;

import android.content.Intent;
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
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminApprovedListActivity extends AppCompatActivity {
    private static final String TAG = "AdminApprovedList";
    private static final String BASE_URL = "http://192.168.254.149/Epermit/get_requests.php";
    private LinearLayout requestsContainer;
    private TextView noRequestsText;
    private RequestQueue requestQueue;
    private List<BorrowRequest> allFetchedRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_request_list);
        requestsContainer = findViewById(R.id.requestsContainer);
        noRequestsText = findViewById(R.id.noRequestsText);
        ((TextView) findViewById(R.id.listTitle)).setText("All Approved/Rejected Requests");
        requestQueue = Volley.newRequestQueue(this);
        loadRequests();
    }
    private void loadRequests() {
        requestsContainer.removeAllViews();
        allFetchedRequests.clear();
        noRequestsText.setVisibility(View.GONE);
        noRequestsText.setText("Loading all approved/rejected requests...");
        noRequestsText.setVisibility(View.VISIBLE);
        try {
            String approvedUrl = BASE_URL + "get_requests.php?status=" + URLEncoder.encode("Approved", "UTF-8");
            loadRequestsFromUrl(approvedUrl, "Approved");
            String rejectedUrl = BASE_URL + "get_requests.php?status=" + URLEncoder.encode("Rejected", "UTF-8");
            loadRequestsFromUrl(rejectedUrl, "Rejected");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "URL encoding failed: " + e.getMessage());
            Toast.makeText(this, "Error preparing request URL.", Toast.LENGTH_SHORT).show();
            noRequestsText.setText("Error loading requests: URL encoding failed.");
            noRequestsText.setVisibility(View.VISIBLE);
        }
    }
    private void loadRequestsFromUrl(String url, String type) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray requestsArray = response.getJSONArray("requests");
                            if (requestsArray.length() > 0) {
                                Gson gson = new Gson();
                                Type listType = new TypeToken<ArrayList<BorrowRequest>>(){}.getType();
                                List<BorrowRequest> newRequests = gson.fromJson(requestsArray.toString(), listType);
                                allFetchedRequests.addAll(newRequests);
                                renderAllRequests();
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error for " + type + " requests", e);
                    } finally {
                        // After processing a response, check if any requests have been loaded so far
                        if (allFetchedRequests.isEmpty()) {
                            noRequestsText.setText("No approved or rejected requests found for admin.");
                            noRequestsText.setVisibility(View.VISIBLE);
                        } else {
                            noRequestsText.setVisibility(View.GONE);
                        }
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error for " + type + " requests: " + error.toString());
                    if (allFetchedRequests.isEmpty()) { // Only show network error if nothing loaded from either call
                        noRequestsText.setText("Network error. Could not load requests.");
                        noRequestsText.setVisibility(View.VISIBLE);
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }
    private void renderAllRequests() {
        requestsContainer.removeAllViews();
        Collections.sort(allFetchedRequests, (r1, r2) -> r2.getDateSubmitted().compareTo(r1.getDateSubmitted()));
        for (BorrowRequest request : allFetchedRequests) {
            addRequestSummaryToContainer(request);
        }
    }

    private void addRequestSummaryToContainer(BorrowRequest request) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout requestSummaryView = (LinearLayout) inflater.inflate(R.layout.item_request_summary, requestsContainer, false);
        TextView summaryTitle = requestSummaryView.findViewById(R.id.summaryTitle);
        TextView summaryStatus = requestSummaryView.findViewById(R.id.summaryStatus);
        TextView summaryDate = requestSummaryView.findViewById(R.id.summaryDate);

        summaryTitle.setText("Project: " + request.getProjectName() + " by " + request.getBorrowerName());
        summaryStatus.setText("Status: " + request.getStatus());
        summaryDate.setText("Submitted: " + request.getDateSubmitted() +
                (request.getApprovedBy() != null && !request.getApprovedBy().isEmpty() ? "\nApproved by: " + request.getApprovedBy() : ""));

        if ("Approved".equals(request.getStatus())) {
            summaryStatus.setTextColor(getResources().getColor(R.color.green));
        } else if ("Rejected".equals(request.getStatus())) {
            summaryStatus.setTextColor(getResources().getColor(R.color.red));
        } else {
            summaryStatus.setTextColor(getResources().getColor(R.color.orange));
        }

        requestSummaryView.setOnClickListener(v -> {
            Intent intent = new Intent(AdminApprovedListActivity.this, DetailActivity.class);
            intent.putExtra("request_id", request.getRequestId());
            startActivity(intent);
        });

        requestsContainer.addView(requestSummaryView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }
}