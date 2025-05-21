package com.example.groupproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

        TextView textName, textEmail, textGender;
        BottomNavigationView bottomNavigationView; // Declare at class level

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_profile); // Make sure this layout exists and is correct

                textName = findViewById(R.id.textName);
                textEmail = findViewById(R.id.textEmail);
                textGender = findViewById(R.id.textGender);

                // Example hardcoded data (you can load this from SharedPreferences or DB)
                // It's good practice to load real user data here, not hardcode.
                textName.setText("Irish Rocha");
                textEmail.setText("irish@gmail.com");
                textGender.setText("Female");

                bottomNavigationView = findViewById(R.id.bottom_navigation);

                bottomNavigationView.setSelectedItemId(R.id.nav_profile);

                bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                                int id = item.getItemId();

                                if (id == R.id.nav_profile) {
                                        Toast.makeText(ProfileActivity.this, "You are already on the Profile page.", Toast.LENGTH_SHORT).show();
                                        return true;
                                } else if (id == R.id.nav_dashboard) {

                                        Intent dashboardIntent = new Intent(ProfileActivity.this, Success.class);
                                        startActivity(dashboardIntent);

                                        return true;
                                } else if (id == R.id.nav_logout) {
                                        performLogout();
                                        return true;
                                }
                                return false;
                        }
                });
        }

        private void performLogout() {
                SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Intent loginIntent = new Intent(ProfileActivity.this, MainActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
                Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        }
}