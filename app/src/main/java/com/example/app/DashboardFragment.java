package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        // Required empty public constructor
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.data_entry_activity, container, false);

        Button btnEnterData = view.findViewById(R.id.btnEnterData);

        btnEnterData.setOnClickListener(v -> {
            // 1. Instantiate the target fragment you want to navigate to
            InputFormFragment targetFragment = new InputFormFragment();

            // 2. Use the parent activity's FragmentManager to swap them
            getParentFragmentManager().beginTransaction()
                    // Replace the main XML container ID with your new fragment
                    .replace(R.id.fragment_container, targetFragment)
                    // Optional: Adds the transaction to the back stack so the user can press the hardware "Back" button to return
                    .addToBackStack(null)
                    // Commit the transaction to execute the change
                    .commit();


            // 2. Find the BottomNavigationView inside the parent activity and select the correct menu ID
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                        getActivity().findViewById(R.id.bottom_navigation);

                if (bottomNav != null) {
                    // This line automatically forces the selection highlight to jump to the "New Entry" tab
                    bottomNav.setSelectedItemId(R.id.nav_input_form);
                }
            }
        });

        return view;
    }

}
