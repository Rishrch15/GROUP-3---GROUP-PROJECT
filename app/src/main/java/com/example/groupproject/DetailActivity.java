package com.example.groupproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;

public class DetailActivity extends AppCompatActivity {

    TextView detailsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailsText = findViewById(R.id.detailsText);

        String json = getIntent().getStringExtra("request_json");
        Gson gson = new Gson();
        BorrowRequest request = gson.fromJson(json, BorrowRequest.class);

        if (request != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Date Submitted: ").append(request.todayDate).append("\n")
                    .append("Name: ").append(request.borrowerName).append("\n")
                    .append("Dept: ").append(request.department).append("\n")
                    .append("Gender: ").append(request.gender).append("\n")
                    .append("Project: ").append(request.projectName).append("\n")
                    .append("Date Needed: ").append(request.dateNeeded).append("\n")
                    .append("Time: ").append(request.time).append("\n")
                    .append("Venue: ").append(request.venue).append("\n\nItems:\n");

            for (BorrowRequest.Item item : request.items) {
                sb.append("- ").append(item.qty).append(" ")
                        .append(item.description).append(" | ")
                        .append(item.DateOfTransfer).append(" | ")
                        .append(item.locationFrom).append(" ➜ ")
                        .append(item.locationTo).append("\n");
            }

            detailsText.setText(sb.toString());

            // Save to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("permit_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            String savedJson = gson.toJson(request);
            editor.putString("request_json", savedJson);
            editor.apply();

            // Automatically open ApproveActivity and pass the JSON string
            Intent intent = new Intent(DetailActivity.this, ApproveActivity.class);
            intent.putExtra("request_json", savedJson);
            startActivity(intent);
            // Optionally finish this activity if you don't want to return here
            finish();
        }
    }
}
