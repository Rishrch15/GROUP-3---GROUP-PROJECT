package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView; // Import TextView
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etReenterPassword;
    private Button btnRegister;
    private Spinner spinnerGender;
    private TextView tvLoginLink; // Declare TextView for the login link
    private String selectedGender = "";

    private static final String URL = "http://192.168.0.105/EPermit/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register); // Ensure your layout file is named 'register.xml'

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etReenterPassword = findViewById(R.id.etReenterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        spinnerGender = findViewById(R.id.spinnerGender);
        tvLoginLink = findViewById(R.id.tvLoginLink); // Initialize the TextView

        // Populate the Spinner
        String[] genderOptions = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genderOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Set an OnItemSelectedListener for the Spinner
        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGender = "";
            }
        });

        btnRegister.setOnClickListener(view -> registerUser());

        // Set an OnClickListener for the login link TextView
        tvLoginLink.setOnClickListener(view -> {
            // Start the MainActivity (assuming MainActivity is your login screen)
            startActivity(new Intent(Register.this, MainActivity.class));
            finish(); // Finish the current Register activity so user can't go back with back button
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String rePassword = etReenterPassword.getText().toString().trim();

        if (selectedGender.equals("Select Gender") || selectedGender.isEmpty()) {
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(rePassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, URL, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                if (obj.getBoolean("success")) {
                    startActivity(new Intent(Register.this, MainActivity.class));
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            if (error instanceof NoConnectionError) {
                Toast.makeText(this, "Error: No internet connection or server is down.", Toast.LENGTH_LONG).show();
            } else if (error instanceof TimeoutError) {
                Toast.makeText(this, "Error: Request timed out.", Toast.LENGTH_LONG).show();
            } else if (error instanceof AuthFailureError) {
                Toast.makeText(this, "Error: Authentication failure.", Toast.LENGTH_LONG).show();
            } else if (error instanceof ServerError) {
                Toast.makeText(this, "Error: Server responded with an error.", Toast.LENGTH_LONG).show();
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    String serverError = new String(error.networkResponse.data);
                    Toast.makeText(this, "Server Error: " + serverError, Toast.LENGTH_LONG).show();
                }
            } else if (error instanceof NetworkError) {
                Toast.makeText(this, "Error: Network issue.", Toast.LENGTH_LONG).show();
            } else if (error instanceof ParseError) {
                Toast.makeText(this, "Error: Response parsing failed.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Login error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
            error.printStackTrace();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("gender", selectedGender);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }
}