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
    private static final String URL = "http://192.168.0.105/EPermit/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }

    public void login(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, URL, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();

                if (obj.getBoolean("success")) {
                    Intent intent = new Intent(MainActivity.this, Success.class); // or AdminDashboardActivity if role-based
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                Toast.makeText(this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }, error -> {
            String errorMsg = "Login error: ";
            if (error instanceof NoConnectionError) {
                errorMsg += "No internet connection or server is down.";
            } else if (error instanceof TimeoutError) {
                errorMsg += "Request timed out.";
            } else if (error instanceof AuthFailureError) {
                errorMsg += "Authentication failure.";
            } else if (error instanceof ServerError) {
                errorMsg += "Server error.";
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    errorMsg += " " + new String(error.networkResponse.data);
                }
            } else if (error instanceof NetworkError) {
                errorMsg += "Network issue.";
            } else if (error instanceof ParseError) {
                errorMsg += "Response parsing failed.";
            } else {
                errorMsg += error.getMessage();
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            error.printStackTrace();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
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
