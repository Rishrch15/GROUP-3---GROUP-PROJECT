package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout toApproveLayout;
    private LinearLayout approvedLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        toApproveLayout = findViewById(R.id.toApproveLayout);
        approvedLayout = findViewById(R.id.approvedLayout);

        toApproveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to the "To Approve" list activity
                Intent intent = new Intent(AdminDashboardActivity.this, ToApproveListActivity.class);
                startActivity(intent);
                Toast.makeText(AdminDashboardActivity.this, "Opening To Approve List", Toast.LENGTH_SHORT).show();
            }
        });

        approvedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to the "Approved" list activity
                Intent intent = new Intent(AdminDashboardActivity.this, ApprovedListActivity.class);
                startActivity(intent);
                Toast.makeText(AdminDashboardActivity.this, "Opening Approved List", Toast.LENGTH_SHORT).show();
            }
        });
    }
}