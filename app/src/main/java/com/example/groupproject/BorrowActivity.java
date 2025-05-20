package com.example.groupproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class BorrowActivity extends AppCompatActivity {

    EditText editDate1, editDept, editName, editProject, editDate2, editTime, editVenue;
    LinearLayout itemsContainer;
    Button addItemBtn, submitBtn;

    public static ArrayList<BorrowRequest> requests = new ArrayList<>(); // Shared list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_form);

        editDate1 = findViewById(R.id.editTextDate1);
        editDept = findViewById(R.id.editTextDepartment);
        editName = findViewById(R.id.editTextBorrowerName);
        editProject = findViewById(R.id.editTextProjectName);
        editDate2 = findViewById(R.id.editTextDate2);
        editTime = findViewById(R.id.editTextTime);
        editVenue = findViewById(R.id.editTextVenue);
        itemsContainer = findViewById(R.id.itemsContainer);
        addItemBtn = findViewById(R.id.add_item_button);
        submitBtn = findViewById(R.id.buttonSubmit);

        addItemBtn.setOnClickListener(v -> addItemRow());

        submitBtn.setOnClickListener(v -> {
            List<BorrowRequest.Item> items = new ArrayList<>();
            for (int i = 0; i < itemsContainer.getChildCount(); i++) {
                LinearLayout row = (LinearLayout) itemsContainer.getChildAt(i);
                EditText qty = (EditText) row.getChildAt(0);
                EditText desc = (EditText) row.getChildAt(1);
                EditText date = (EditText) row.getChildAt(2);
                EditText from = (EditText) row.getChildAt(3);
                EditText to = (EditText) row.getChildAt(4);
                items.add(new BorrowRequest.Item(
                        qty.getText().toString(),
                        desc.getText().toString(),
                        date.getText().toString(),
                        from.getText().toString(),
                        to.getText().toString()));
            }

            BorrowRequest request = new BorrowRequest(
                    editDate1.getText().toString(),
                    editDept.getText().toString(),
                    editName.getText().toString(),
                    editProject.getText().toString(),
                    editDate2.getText().toString(),
                    editTime.getText().toString(),
                    editVenue.getText().toString(),
                    items);

            requests.add(request);

            Toast.makeText(this, "Request Submitted!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void addItemRow() {
        LinearLayout row = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.item_row, itemsContainer, false);
        itemsContainer.addView(row);
    }

    private  void saveBorrowRequest(BorrowRequest request) {
        SharedPreferences prefs = getSharedPreferences("permit_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(request);

        int count = prefs.getInt("request_count", 0);
        editor.putString("request_" + count, json);
        editor.putInt("request_count", count + 1);
        editor.apply();
    }
}

