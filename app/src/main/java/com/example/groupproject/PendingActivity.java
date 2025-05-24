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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class PendingActivity extends AppCompatActivity {

    private static final String TAG = "PendingActivity";
    public static final String BASE_URL = "http://192.168.185.26/E-permit/login.php";

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

        loadRequests("Pending");
    }

    private void loadRequests(String statusFilter) {
        requestsContainer.removeAllViews();
        noRequestsText.setVisibility(View.GONE);
        noRequestsText.setText("Loading pending requests...");
        noRequestsText.setVisibility(View.VISIBLE);

        String url = BASE_URL + "get_requests.php?status=" + URLEncoder.encode(statusFilter) + "&borrower_id=" + URLEncoder.encode(currentBorrowerId);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        if (success) {
                            JSONArray requestsArray = response.getJSONArray("requests");
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
                            noRequestsText.setText("Error: " + message);
                            noRequestsText.setVisibility(View.VISIBLE);
                            Log.e(TAG, "PHP reported error: " + message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error in response", e);
                        noRequestsText.setText("Error parsing server response.");
                        noRequestsText.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response Data: " + new String(error.networkResponse.data));
                    }
                    noRequestsText.setText("Network error. Could not load requests.");
                    noRequestsText.setVisibility(View.VISIBLE);
                    Toast.makeText(PendingActivity.this, "Network error.", Toast.LENGTH_LONG).show();
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void addRequestSummaryToContainer(BorrowRequest request) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout requestSummaryView = (LinearLayout) inflater.inflate(R.layout.item_request_summary, requestsContainer, false);

        TextView summaryTitle = requestSummaryView.findViewById(R.id.summaryTitle);
        TextView summaryStatus = requestSummaryView.findViewById(R.id.summaryStatus);
        TextView summaryDate = requestSummaryView.findViewById(R.id.summaryDate);

        // Corrected: Using getter methods for private fields
        summaryTitle.setText("Project: " + request.getProjectName() + " by " + request.getBorrowerName());
        summaryStatus.setText("Status: " + request.getStatus());
        summaryDate.setText("Submitted: " + request.getDateSubmitted());

        // Set status color
        // Corrected: Using getter methods
        if ("Approved".equals(request.getStatus())) {
            summaryStatus.setTextColor(getResources().getColor(R.color.green));
        } else if ("Rejected".equals(request.getStatus())) {
            summaryStatus.setTextColor(getResources().getColor(R.color.red));
        } else { // Pending
            summaryStatus.setTextColor(getResources().getColor(R.color.orange));
        }


        requestSummaryView.setOnClickListener(v -> {
            Intent intent = new Intent(PendingActivity.this, DetailActivity.class);
            // Corrected: Using getter methods
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