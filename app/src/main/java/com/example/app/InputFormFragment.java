package com.example.app;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class InputFormFragment extends Fragment {

    // UI Elements
    private TextView etAge, etDistance, etMissedDoses;
    private TextView genderSelect, treatmentPhaseSelect, sideEffectsSelect, familySupportSelect, reminderExposureSelect;
    private Button btnAnalyze;

    // TensorFlow Lite Interpreter
    private Interpreter tflite;

    // Selection Arrays for Dropdowns
    private final String[] genderOptions = {"Male", "Female"};
    private final String[] phaseOptions = {"Intensive Phase", "Continuation Phase"};
    private final String[] sideEffectsOptions = {"No", "Yes"};
    private final String[] supportOptions = {"No Support", "Low Support", "High Support"};
    private final String[] reminderOptions = {"None", "SMS Only", "Phone Call & SMS"};

    private DatabaseHelper dbHelper;

    public InputFormFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate the fragment layout matrix container
        View view = inflater.inflate(R.layout.data_details_activity, container, false);

        // 2. Initialize UI Elements bound to the layout view instance
        initViews(view);

        // 3. Use requireContext() to safely initialize SQLite
        dbHelper = new DatabaseHelper(requireContext());

        // 4. Initialize TensorFlow Lite Model
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to load TFLite model.", Toast.LENGTH_LONG).show();
        }

        // 5. Set up Dropdown Dialogs
        setupDropdown(genderSelect, genderOptions);
        setupDropdown(treatmentPhaseSelect, phaseOptions);
        setupDropdown(sideEffectsSelect, sideEffectsOptions);
        setupDropdown(familySupportSelect, supportOptions);
        setupDropdown(reminderExposureSelect, reminderOptions);

        // 6. Analyze Button Click Listener
        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runInference();
            }
        });

        return view;
    }

    private void initViews(View view) {
        etAge = view.findViewById(R.id.etAge);
        genderSelect = view.findViewById(R.id.genderSelectOption);
        etDistance = view.findViewById(R.id.etDistance);
        etMissedDoses = view.findViewById(R.id.etMissedDoses);
        treatmentPhaseSelect = view.findViewById(R.id.TreatmentPhaseSelectOption);
        sideEffectsSelect = view.findViewById(R.id.SideEffectsSelectOption);
        familySupportSelect = view.findViewById(R.id.FamilySupportSelectOption);
        reminderExposureSelect = view.findViewById(R.id.ReminderExposureSelectOption);
        btnAnalyze = view.findViewById(R.id.btnAnalyze);
    }

    /**
     * Helper to show an AlertDialog when a selection TextView is clicked.
     */
    private void setupDropdown(final TextView textView, final String[] options) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fixed context reference
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(textView.getHint());
                builder.setItems(options, (dialog, which) -> textView.setText(options[which]));
                builder.show();
            }
        });
    }

    /**
     * Memory-maps the model file from the assets folder.
     */
    private MappedByteBuffer loadModelFile() throws IOException {
        // Fixed assets access point wrapper
        AssetFileDescriptor fileDescriptor = requireActivity().getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Gathers inputs, encodes categorical strings to floats, and runs ML inference.
     */
    void runInference() {
        if (tflite == null) {
            Toast.makeText(requireContext(), "Model is not initialized.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Validation Check
        if (TextUtils.isEmpty(etAge.getText()) || TextUtils.isEmpty(etDistance.getText()) ||
                TextUtils.isEmpty(etMissedDoses.getText()) || TextUtils.isEmpty(genderSelect.getText()) ||
                TextUtils.isEmpty(treatmentPhaseSelect.getText()) || TextUtils.isEmpty(sideEffectsSelect.getText()) ||
                TextUtils.isEmpty(familySupportSelect.getText()) || TextUtils.isEmpty(reminderExposureSelect.getText())) {

            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 2. Parse Numerical Data
            float age = Float.parseFloat(etAge.getText().toString().trim());
            float distance = Float.parseFloat(etDistance.getText().toString().trim());
            float missedDoses = Float.parseFloat(etMissedDoses.getText().toString().trim());

            // 3. Map Categorical Selections to Float Encodings
            float genderEncoded = getIndex(genderOptions, genderSelect.getText().toString());
            float phaseEncoded = getIndex(phaseOptions, treatmentPhaseSelect.getText().toString());
            float sideEffectsEncoded = getIndex(sideEffectsOptions, sideEffectsSelect.getText().toString());
            float supportEncoded = getIndex(supportOptions, familySupportSelect.getText().toString());
            float reminderEncoded = getIndex(reminderOptions, reminderExposureSelect.getText().toString());

            // 4. Structure the Input Array (Shape: [1, 8] for 8 features)
            float[][] inputVal = new float[][]{{
                    age,
                    genderEncoded,
                    distance,
                    missedDoses,
                    phaseEncoded,
                    sideEffectsEncoded,
                    supportEncoded,
                    reminderEncoded
            }};

            // 5. Structure the Output Array
            float[][] outputVal = new float[1][2];

            // 6. Execute Model Inference
            tflite.run(inputVal, outputVal);

            float class1Prob = outputVal[0][1];

            String genderStr = genderSelect.getText().toString();
            String phaseStr = treatmentPhaseSelect.getText().toString();
            String sideEffectsStr = sideEffectsSelect.getText().toString();
            String supportStr = familySupportSelect.getText().toString();
            String reminderStr = reminderExposureSelect.getText().toString();

            showResultDialog(age, genderStr, distance, missedDoses, phaseStr, sideEffectsStr, supportStr, reminderStr, class1Prob);

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid number inputs.", Toast.LENGTH_SHORT).show();
        }
    }

    private float getIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return (float) i;
        }
        return 0.0f;
    }

    /**
     * Displays inference output to the user.
     */
    private void showResultDialog(final float age, final String gender, final float distance, final float missedDoses,
                                  final String phase, final String sideEffects, final String support, final String reminder, final float score) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Analysis Result");

        String message = String.format("Adherence Risk Score: %.2f%%\n", score * 100);
        if (score > 0.5) {
            message += "Warning: High risk of non-adherence. Intervention recommended.";
        } else {
            message += "Patient adherence is tracking normally.";
        }

        builder.setMessage(message);
        builder.setPositiveButton("Save to Database", (dialog, which) -> {
            boolean isInserted = dbHelper.insertRecord(
                    age,
                    gender,
                    distance,
                    missedDoses,
                    phase,
                    sideEffects,
                    support,
                    reminder,
                    score
            );

            if (isInserted) {
                Toast.makeText(requireContext(), "Data successfully saved for model training!", Toast.LENGTH_SHORT).show();
                clearFormFields();
            } else {
                Toast.makeText(requireContext(), "Database storage failure.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void clearFormFields() {
        etAge.setText("");
        genderSelect.setText("");
        etDistance.setText("");
        etMissedDoses.setText("");
        treatmentPhaseSelect.setText("");
        sideEffectsSelect.setText("");
        familySupportSelect.setText("");
        reminderExposureSelect.setText("");
    }

    @Override
    public void onDestroyView() {
        // Close resources when fragment view scope terminates
        if (tflite != null) {
            tflite.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroyView();
    }
}