package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Register extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etReenterPassword;
    private TextView tvStatus;
    private Button btnRegister;
    private String name, email, password, reenterPassword;
    private RadioGroup radioGroupUserType;
    private RadioButton radioAdmin, radioStudent;
    private String userType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etReenterPassword = findViewById(R.id.etReenterPassword);
        tvStatus = findViewById(R.id.tvStatus);
        btnRegister = findViewById(R.id.btnRegister);
        radioGroupUserType = findViewById(R.id.radioGroupUserType);
        radioAdmin = findViewById(R.id.radioAdmin);
        radioStudent = findViewById(R.id.radioStudent);
        name = email = password = reenterPassword = "";
        userType = "";
    }

    public void save(View view) {
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        reenterPassword = etReenterPassword.getText().toString().trim();

        int selectedId = radioGroupUserType.getCheckedRadioButtonId();
        if (selectedId == radioAdmin.getId()) {
            userType = "Admin";
        } else if (selectedId == radioStudent.getId()) {
            userType = "Student";
        } else {
            Toast.makeText(this, "Please select user type (Admin or Student)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(reenterPassword)) {
            Toast.makeText(this, "Password Mismatch", Toast.LENGTH_SHORT).show();
        } else if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
        } else {
            tvStatus.setText("Successfully registered as " + userType + ".");
            btnRegister.setClickable(false);
        }
    }

    public void login(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}