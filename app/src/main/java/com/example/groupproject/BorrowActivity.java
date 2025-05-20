package com.example.groupproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class BorrowActivity extends AppCompatActivity {

    TextView textDate, textDate2, text3, text4;

    EditText editDate1, editDept, editName, editProject, editDate2, editTime, editVenue;
    LinearLayout itemsContainer;
    Button addItemBtn, submitBtn, button, button2, button3, button4;

    public static ArrayList<BorrowRequest> requests = new ArrayList<>(); // Shared list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_form);

        textDate = findViewById(R.id.showText);
        button = findViewById(R.id.dateButton);
        editDept = findViewById(R.id.editTextDepartment);
        editName = findViewById(R.id.editTextBorrowerName);
        text4 = findViewById(R.id.showText4);
        button4 = findViewById(R.id.genderButton);
        editProject = findViewById(R.id.editTextProjectName);
        textDate2 = findViewById(R.id.showText2);
        button2 = findViewById(R.id.dateButton2);
        text3 = findViewById(R.id.showText3);
        button3 = findViewById(R.id.timeButton);
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
            Gson gson = new Gson();
            String requestJson = gson.toJson(request);

            Intent intent = new Intent(BorrowActivity.this, DetailActivity.class);
            intent.putExtra("request_json", requestJson);  // send JSON string
            startActivity(intent);


            Toast.makeText(this, "Request Submitted!", Toast.LENGTH_SHORT).show();
            finish();
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openDialog1();
            }

        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openDialog2();
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openDialog3();
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openDialog4();
            }
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

    private void openDialog1(){
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                textDate.setText(String.valueOf(year)+"."+String.valueOf(month)+"."+String.valueOf(day));
            }
        }, 2025, 4, 21);

        dialog.show();
    }

    private void openDialog2(){
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                textDate2.setText(String.valueOf(year)+"."+String.valueOf(month)+"."+String.valueOf(day));
            }
        }, 2025, 4, 21);

        dialog.show();
    }

    private void openDialog3() {
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                String amPm;
                int hourFormat;

                if (hour >= 12) {
                    amPm = "PM";
                    hourFormat = (hour > 12) ? hour - 12 : hour;
                } else {
                    amPm = "AM";
                    hourFormat = (hour == 0) ? 12 : hour;
                }

                text3.setText(String.valueOf(hourFormat) + ":" + String.format("%02d", minute) + " " + amPm);
            }
        }, 12, 0, false);

        dialog.show();
    }

    private void openDialog4() {
        String[] genders = {"Male", "Female"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Gender");
        builder.setItems(genders, (dialog, which) -> {
            // Set selected gender to a TextView (you can change this to your target view)
            text4.setText(genders[which]);
        });

        builder.show();
    }



}

