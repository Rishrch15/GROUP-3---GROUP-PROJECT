package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText etName, etEmail, etPassword, etReenterPassword;
    private Button btnRegister;
    private RadioGroup radioGroupGender;
    private RadioButton radioMale, radioFemale;
    private static final String URL = "http://192.168.254.149/Epermit/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etReenterPassword = findViewById(R.id.etReenterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);
        radioMale.setChecked(true);
        btnRegister.setOnClickListener(view -> registerUser());
    }
    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String rePassword = etReenterPassword.getText().toString().trim();

        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        String gender = (selectedGenderId == R.id.radioMale) ? "Male" : "Female";

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
                params.put("gender", gender);
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