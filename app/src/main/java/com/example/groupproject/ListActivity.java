package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List; // Import List

public class ListActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> listTitles;

    // A placeholder for the list of requests.
    // In a real application, you'd likely fetch this from a database, API,
    // or receive it via an Intent from the previous activity.
    // For now, assuming BorrowActivity.requests is a public static List<BorrowRequest>
    // If it's not static and public, you'll need to pass it via Intent or fetch it.
    private static List<BorrowRequest> requests = new ArrayList<>(); // Initialize to avoid NullPointerException

    // You would ideally set this list from BorrowActivity
    public static void setRequests(List<BorrowRequest> newRequests) {
        if (newRequests != null) {
            requests.clear();
            requests.addAll(newRequests);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = findViewById(R.id.listView);
        listTitles = new ArrayList<>();

        // Use the static 'requests' list from this class or ensure BorrowActivity.requests is properly populated
        if (requests != null) { // Add a null check for safety
            for (BorrowRequest br : requests) { // Changed to 'requests' from BorrowActivity.requests
                listTitles.add(br.getBorrowerName() + " - " + br.getProjectName());
            }
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listTitles);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (requests != null && position < requests.size()) { // Changed to 'requests'
                BorrowRequest selected = requests.get(position);
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra("request_id", selected.getRequestId()); // Pass the ID
                startActivity(intent);
            }
        });
    }
}