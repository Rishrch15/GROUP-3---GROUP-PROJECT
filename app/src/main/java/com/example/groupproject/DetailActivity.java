package com.example.groupproject;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    TextView detailsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailsText = findViewById(R.id.detailsText);
        BorrowRequest request = (BorrowRequest) getIntent().getSerializableExtra("request");

        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(request.borrowerName).append("\n")
                .append("Dept: ").append(request.department).append("\n")
                .append("Project: ").append(request.projectName).append("\n")
                .append("Date: ").append(request.date1).append("\n")
                .append("Time: ").append(request.time).append("\n")
                .append("Venue: ").append(request.venue).append("\n\nItems:\n");

        for (BorrowRequest.Item item : request.items) {
            sb.append("- ").append(item.qty).append(" ")
                    .append(item.description).append(" | ")
                    .append(item.dateOfTransfer).append(" | ")
                    .append(item.locationFrom).append(" ➜ ")
                    .append(item.locationTo).append("\n");
        }

        detailsText.setText(sb.toString());
    }
}

