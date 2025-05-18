package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private String email, password;
    private RadioGroup radioGroup;
    private RadioButton radioAdmin, radioStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = password = "";
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        radioGroup = findViewById(R.id.radioGroup);
        radioAdmin = findViewById(R.id.radioAdmin);
        radioStudent = findViewById(R.id.radioStudent);

        // Set default selection (optional, but good practice)
        radioStudent.setChecked(true); // Default to student
    }

    public void login(View view) {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        if (!email.equals("") && !password.equals("")) {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = findViewById(selectedId);

            if (selectedRadioButton == radioAdmin) {
                Log.d("res", "Admin Login successful");
                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class); // Replace with your actual Admin Dashboard Activity
                startActivity(intent);
                finish();
            } else if (selectedRadioButton == radioStudent) {
                Log.d("res", "Student Login successful");
                Intent intent = new Intent(MainActivity.this, Success.class); // Replace with your actual Student Dashboard Activity
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please select Admin or Student!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
        }
    }

    public void register(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
        finish();
    }
}