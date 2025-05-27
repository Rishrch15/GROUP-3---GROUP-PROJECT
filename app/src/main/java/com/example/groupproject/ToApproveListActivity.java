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
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ToApproveListActivity extends AppCompatActivity {

    private static final String TAG = "ToApproveListActivity";
    private static final String BASE_URL = "http://192.168.0.105/EPermit/";  // Removed trailing slash
    private LinearLayout requestsContainer;
    private TextView noRequestsText;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_request_list);

        requestsContainer = findViewById(R.id.requestsContainer);
        noRequestsText = findViewById(R.id.noRequestsText);
        requestQueue = Volley.newRequestQueue(this);

        loadRequests("Pending");
    }

    private void loadRequests(String statusFilter) {
        requestsContainer.removeAllViews();
        noRequestsText.setVisibility(View.VISIBLE);
        noRequestsText.setText("Loading requests for approval...");

        try {
            String url = BASE_URL + "get_requests.php?status=" +
                    URLEncoder.encode(statusFilter, StandardCharsets.UTF_8.toString());

            Log.d(TAG, "Fetching requests from: " + url);

            // Using StringRequest to better handle raw responses
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            // Check for HTML error responses
                            if (response.contains("<br") || response.startsWith("<")) {
                                handleError("Server returned HTML error: " + response);
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
                                    noRequestsText.setText("No pending requests found.");
                                    noRequestsText.setVisibility(View.VISIBLE);
                                }
                            } else {
                                handleError("Server error: " + message);
                            }
                        } catch (JSONException e) {
                            handleError("JSON parsing error: " + e.getMessage() + "\nResponse: " + response);
                        } catch (JsonSyntaxException e) {
                            handleError("GSON parsing error: " + e.getMessage() + "\nResponse: " + response);
                        }
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String responseData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            handleError("Network error: " + error.getMessage() + "\nResponse: " + responseData);
                        } else {
                            handleError("Network error: " + error.getMessage());
                        }
                    });

            requestQueue.add(stringRequest);
        } catch (UnsupportedEncodingException e) {
            handleError("URL encoding error: " + e.getMessage());
        }
    }

    private void handleError(String errorMessage) {
        Log.e(TAG, errorMessage);
        runOnUiThread(() -> {
            noRequestsText.setText("Error loading requests. Please try again.");
            noRequestsText.setVisibility(View.VISIBLE);
            Toast.makeText(ToApproveListActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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

        // Set status colors
        if ("Approved".equalsIgnoreCase(request.getStatus())) {
            summaryStatus.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else if ("Rejected".equalsIgnoreCase(request.getStatus())) {
            summaryStatus.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else {
            summaryStatus.setTextColor(ContextCompat.getColor(this, R.color.orange));
        }

        requestSummaryView.setOnClickListener(v -> {
            Intent intent = new Intent(ToApproveListActivity.this, ApproveActivity.class);
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
}