package com.example.app;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_entry_activity);

        Button btnEnterData = findViewById(R.id.btnEnterData);

        btnEnterData.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MainActivity.this,
                            DataActivity.class);
            startActivity(intent);
        });
    }
}

