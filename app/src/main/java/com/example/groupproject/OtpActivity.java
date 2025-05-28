package com.example.groupproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class OtpActivity extends AppCompatActivity {

    private EditText otpEditText;
    private Button sendOtpButton, verifyOtpButton;
    private RequestQueue requestQueue;

    private final String SEND_OTP_URL = "http://192.168.100.160/Epermit/send_otp.php";
    private final String VERIFY_OTP_URL = "http://192.168.100.160/Epermit/verify_otp.php";

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        otpEditText = findViewById(R.id.etOtp);
        sendOtpButton = findViewById(R.id.btnResend);
        verifyOtpButton = findViewById(R.id.btnVerify);
        requestQueue = Volley.newRequestQueue(this);

        // Get email from intent or use dummy
        userEmail = getIntent().getStringExtra("user_email_for_otp");
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "test@example.com";
        }

        sendOtpButton.setOnClickListener(v -> sendOtp(userEmail));
        verifyOtpButton.setOnClickListener(v -> verifyOtp(userEmail));
    }

    private void sendOtp(String email) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, SEND_OTP_URL,
                response -> {
                    Log.d("sendOtp", "Response: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("sendOtp", "Error: " + error.toString());
                    Toast.makeText(this, "Failed to send OTP", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        requestQueue.add(postRequest);
    }

    private void verifyOtp(String email) {
        String otp = otpEditText.getText().toString().trim();
        if (otp.isEmpty()) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest postRequest = new StringRequest(Request.Method.POST, VERIFY_OTP_URL,
                response -> {
                    Log.d("verifyOtp", "Response: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show();
                        if ("verified".equals(json.getString("status"))) {
                            // OTP verified - go to next activity
                            startActivity(new Intent(OtpActivity.this, MainActivity.class));
                            finish();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("verifyOtp", "Error: " + error.toString());
                    Toast.makeText(this, "Failed to verify OTP", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("email", email);
                params.put("otp", otp);
                return params;
            }
        };

        requestQueue.add(postRequest);
    }
}
