package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

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
            Intent intent = new Intent(Success.this, ApproveActivity.class);
            startActivity(intent);
        });

        btnTransfer.setOnClickListener(v -> {
            Intent intent = new Intent(Success.this, TransferFormActivity.class);
            startActivity(intent);
        });
    }
}
