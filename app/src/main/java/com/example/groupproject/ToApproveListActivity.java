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
import java.util.ArrayList;
import java.util.List;

public class ToApproveListActivity extends AppCompatActivity {

    private static final String TAG = "ToApproveListActivity";
    private static final String BASE_URL = "http://192.168.254.149/Epermit/get_requests.php/";
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
        noRequestsText.setVisibility(View.GONE);
        noRequestsText.setText("Loading requests for approval...");
        noRequestsText.setVisibility(View.VISIBLE);

        String url = BASE_URL + "get_requests.php?status=" + URLEncoder.encode(statusFilter);
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
                                noRequestsText.setText("No " + statusFilter.toLowerCase() + " requests to approve found.");
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
                    Toast.makeText(ToApproveListActivity.this, "Network error.", Toast.LENGTH_LONG).show();
                });
        requestQueue.add(jsonObjectRequest);
    }
    private void addRequestSummaryToContainer(BorrowRequest request) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout requestSummaryView = (LinearLayout) inflater.inflate(R.layout.item_request_summary, requestsContainer, false);

        TextView summaryTitle = requestSummaryView.findViewById(R.id.summaryTitle);
        TextView summaryStatus = requestSummaryView.findViewById(R.id.summaryStatus);
        TextView summaryDate = requestSummaryView.findViewById(R.id.summaryDate);
        summaryTitle.setText("Project: " + request.projectName + " by " + request.borrowerName);
        summaryStatus.setText("Status: " + request.status);
        summaryDate.setText("Submitted: " + request.dateSubmitted);
        if ("Approved".equals(request.status)) {
            summaryStatus.setTextColor(getResources().getColor(R.color.green));
        } else if ("Rejected".equals(request.status)) {
            summaryStatus.setTextColor(getResources().getColor(R.color.red));
        } else {
            summaryStatus.setTextColor(getResources().getColor(R.color.orange));
        }
        requestSummaryView.setOnClickListener(v -> {
            Intent intent = new Intent(ToApproveListActivity.this, ApproveActivity.class); // Go to your ApproveActivity
            intent.putExtra("request_id", request.requestId);
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