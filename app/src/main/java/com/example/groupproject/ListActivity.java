package com.example.groupproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> listTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = findViewById(R.id.listView);
        listTitles = new ArrayList<>();

        for (BorrowRequest br : BorrowActivity.requests) {
            listTitles.add(br.borrowerName + " - " + br.projectName);
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listTitles);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            BorrowRequest selected = BorrowActivity.requests.get(position);
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("request", selected);
            startActivity(intent);
        });
    }
}

