package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private RadioGroup radioGroup;
    private RadioButton radioAdmin, radioStudent;
    private static final String URL = "http://192.168.254.149/Epermit/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        radioGroup = findViewById(R.id.radioGroup);
        radioAdmin = findViewById(R.id.radioAdmin);
        radioStudent = findViewById(R.id.radioStudent);
        radioStudent.setChecked(true);
    }

    public void login(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        int selectedId = radioGroup.getCheckedRadioButtonId();
        String role = (selectedId == R.id.radioAdmin) ? "Admin" : "Student";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, URL, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                if (obj.getBoolean("success")) {

                    Intent intent;
                    if (role.equals("Admin")) {
                        intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                    } else {
                        intent = new Intent(MainActivity.this, Success.class);
                    }
                    startActivity(intent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            if (error instanceof NoConnectionError) {
                Toast.makeText(this, "Login error: No internet connection or server is down.", Toast.LENGTH_LONG).show();
            } else if (error instanceof TimeoutError) {
                Toast.makeText(this, "Login error: Request timed out.", Toast.LENGTH_LONG).show();
            } else if (error instanceof AuthFailureError) {
                Toast.makeText(this, "Login error: Authentication failure.", Toast.LENGTH_LONG).show();
            } else if (error instanceof ServerError) {
                Toast.makeText(this, "Login error: Server responded with an error.", Toast.LENGTH_LONG).show();
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    String serverError = new String(error.networkResponse.data);
                    Toast.makeText(this, "Server Error: " + serverError, Toast.LENGTH_LONG).show();
                }
            } else if (error instanceof NetworkError) {
                Toast.makeText(this, "Login error: Network issue.", Toast.LENGTH_LONG).show();
            } else if (error instanceof ParseError) {
                Toast.makeText(this, "Login error: Response parsing failed.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Login error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
            error.printStackTrace();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                params.put("role", role);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(request);
    }

    public void register(View view) {
        startActivity(new Intent(this, Register.class));
    }
}