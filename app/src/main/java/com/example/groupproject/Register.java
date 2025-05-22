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
    private RadioGroup radioGroupGender; // Changed to radioGroupGender
    private RadioButton radioMale, radioFemale; // Added for gender selection
    private String name, email, password, reenterPassword, gender; // Added gender variable

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register); // Make sure this corresponds to your layout file name

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etReenterPassword = findViewById(R.id.etReenterPassword);
        tvStatus = findViewById(R.id.tvStatus);
        btnRegister = findViewById(R.id.btnRegister);

        // Initialize RadioGroup and RadioButtons for Gender
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);

        // Initialize string variables (optional, as they'll be updated on button click)
        name = "";
        email = "";
        password = "";
        reenterPassword = "";
        gender = "";

        // Set the OnClickListener for the Register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(view); // Call the save method when the button is clicked
            }
        });

        // You might want to set a default gender selection
        radioMale.setChecked(true); // Default to Male
    }

    public void save(View view) {
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        reenterPassword = etReenterPassword.getText().toString().trim();

        // Get selected gender
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.radioMale) {
            gender = "Male";
        } else if (selectedGenderId == R.id.radioFemale) {
            gender = "Female";
        } else {
            // This case should ideally not happen if a default is set, but good for robust error handling
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Basic client-side validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || reenterPassword.isEmpty()) {
            Toast.makeText(this, "All fields cannot be empty!", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(reenterPassword)) {
            Toast.makeText(this, "Password Mismatch! Please re-enter your password correctly.", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) { // Example: Minimum password length
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
        }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
        }
        else {

            tvStatus.setText("Successfully registered as " + name + " (Gender: " + gender + ").");
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show();

            // Simulate navigation to MainActivity (Login Screen) after successful registration
            Intent intent = new Intent(Register.this, MainActivity.class);
            //
            startActivity(intent);
            finish(); // Finish Register activity so user can't go back to it with back button
        }
    }

    public void login(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish Register activity to prevent going back
    }
}