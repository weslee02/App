package com.example.app;

import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ViewRecordsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientAdapter adapter;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_records);

        dbHelper = new DatabaseHelper(this);

        // Fetch raw pointer sequence data from SQLite database
        cursor = dbHelper.getAllRecords();

        // Setup the modern high-performance scroll container layout
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Connect UI list view with our new custom data adapter mapping class
        adapter = new PatientAdapter(cursor);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close data context pointers to safeguard memory pipelines
        if (cursor != null) {
            cursor.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}