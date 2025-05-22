package com.example.groupproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

public class Success extends AppCompatActivity {

    ImageButton btnBarrow, btnPending, btnApprove, btnTransfer;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.success);

        btnBarrow = findViewById(R.id.btnBarrow);
        btnPending = findViewById(R.id.btnPending);
        btnApprove = findViewById(R.id.btnApprove);
        btnTransfer = findViewById(R.id.btnTransfer);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_dashboard) {
                    Toast.makeText(Success.this, "Dashboard Selected", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent profileIntent = new Intent(Success.this, ProfileActivity.class);
                    startActivity(profileIntent);
                    return true;
                } else if (id == R.id.nav_logout) {
                    performLogout();
                    return true;
                }
                return false;
            }
        });

        btnBarrow.setOnClickListener(v -> {
            Intent intent = new Intent(Success.this, BorrowActivity.class);
            startActivity(intent);
        });

        btnPending.setOnClickListener(v -> {
            // As per previous corrections, ListActivity now expects request_id.
            // However, this button likely shows a list of pending requests,
            // not a single one, so it probably doesn't need to pass a request_id here.
            Intent intent = new Intent(Success.this, ListActivity.class);
            startActivity(intent);
        });

        btnApprove.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("permit_data", MODE_PRIVATE);
            String json = prefs.getString("request_json", null);

            if (json != null) {
                Gson gson = new Gson();
                BorrowRequest request = gson.fromJson(json, BorrowRequest.class);

                Intent intent = new Intent(Success.this,ToApproveListActivity.class);
                // Corrected: Pass request_id instead of the whole object
                intent.putExtra("request_id", request.getRequestId()); // Assuming BorrowRequest has getRequestId()
                startActivity(intent);
            } else {
                Toast.makeText(Success.this, "No request data available", Toast.LENGTH_SHORT).show();
            }
        });

        btnTransfer.setOnClickListener(v -> {
            Intent intent = new Intent(Success.this, TransferFormActivity.class);
            startActivity(intent);
        });
    }

    private void performLogout() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent loginIntent = new Intent(Success.this, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        finish();
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
    }
}