package com.example.groupproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

public class Success extends AppCompatActivity {

    ImageButton btnBarrow, btnPending, btnApprove, btnTransfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.success);

        btnBarrow = findViewById(R.id.btnBarrow);
        btnPending = findViewById(R.id.btnPending);
        btnApprove = findViewById(R.id.btnApprove);
        btnTransfer = findViewById(R.id.btnTransfer);

        btnBarrow.setOnClickListener(v -> {
            Intent intent = new Intent(Success.this, BorrowActivity.class);
            startActivity(intent);
        });

        btnPending.setOnClickListener(v -> {
            Intent intent = new Intent(Success.this, ListActivity.class);
            startActivity(intent);
        });

        btnApprove.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("permit_data", MODE_PRIVATE);
            String json = prefs.getString("request_json", null);

            if (json != null) {
                Gson gson = new Gson();
                BorrowRequest request = gson.fromJson(json, BorrowRequest.class);

                Intent intent = new Intent(Success.this, ApproveActivity.class);
                intent.putExtra("request", request);
                startActivity(intent);
            } else {
                Toast.makeText(Success.this, "No request data available", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
