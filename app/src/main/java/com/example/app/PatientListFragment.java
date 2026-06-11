package com.example.app;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PatientListFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private PatientAdapter adapter;
    private Cursor cursor;

    public PatientListFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate the scroll container layout
        View view = inflater.inflate(R.layout.activity_view_records, container, false);

        // 2. Initialize the Database Helper with fragment context
        dbHelper = new DatabaseHelper(requireContext());

        // 3. Fetch raw pointer sequence data from SQLite database
        cursor = dbHelper.getAllRecords();

        // 4. Setup the high-performance scroll container layout using the 'view' prefix
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 5. Connect UI list view with your custom data adapter mapping class
        adapter = new PatientAdapter(cursor);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        // Close data context pointers when the fragment view is destroyed to safeguard memory
        if (cursor != null) {
            cursor.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroyView();
    }
}