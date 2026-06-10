package com.example.app;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class DataActivity  extends AppCompatActivity {

    // UI Elements
    private TextView etAge, etDistance, etMissedDoses;
    private TextView genderSelect, treatmentPhaseSelect, sideEffectsSelect, familySupportSelect, reminderExposureSelect;
    private Button btnAnalyze;

    // TensorFlow Lite Interpreter
    private Interpreter tflite;

    // Selection Arrays for Dropdowns
    private final String[] genderOptions = {"Male", "Female", "Other"};
    private final String[] phaseOptions = {"Intensive Phase", "Continuation Phase"};
    private final String[] sideEffectsOptions = {"No", "Yes"};
    private final String[] supportOptions = {"No Support", "Low Support", "High Support"};
    private final String[] reminderOptions = {"None", "SMS Only", "Phone Call & SMS"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_details_activity);

        // Initialize UI Elements
        initViews();

        // Initialize TensorFlow Lite Model
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load TFLite model.", Toast.LENGTH_LONG).show();
        }

        // Set up Dropdown Dialogs
        setupDropdown(genderSelect, genderOptions);
        setupDropdown(treatmentPhaseSelect, phaseOptions);
        setupDropdown(sideEffectsSelect, sideEffectsOptions);
        setupDropdown(familySupportSelect, supportOptions);
        setupDropdown(reminderExposureSelect, reminderOptions);

        // Analyze Button Click Listener
        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runInference();
            }
        });
    }

    private void initViews() {
        etAge = findViewById(R.id.etAge);
        genderSelect = findViewById(R.id.genderSelectOption);
        etDistance = findViewById(R.id.etDistance);
        etMissedDoses = findViewById(R.id.etMissedDoses);
        treatmentPhaseSelect = findViewById(R.id.TreatmentPhaseSelectOption);
        sideEffectsSelect = findViewById(R.id.SideEffectsSelectOption);
        familySupportSelect = findViewById(R.id.FamilySupportSelectOption);
        reminderExposureSelect = findViewById(R.id.ReminderExposureSelectOption);
        btnAnalyze = findViewById(R.id.btnAnalyze);
    }

    /**
     * Helper to show an AlertDialog when a selection TextView is clicked.
     */
    private void setupDropdown(final TextView textView, final String[] options) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DataActivity.this);
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
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    /**
     * Gathers inputs, encodes categorical strings to floats, and runs ML inference.
     */
    private void runInference() {
        if (tflite == null) {
            Toast.makeText(this, "Model is not initialized.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Validation Check
        if (TextUtils.isEmpty(etAge.getText()) || TextUtils.isEmpty(etDistance.getText()) ||
                TextUtils.isEmpty(etMissedDoses.getText()) || TextUtils.isEmpty(genderSelect.getText()) ||
                TextUtils.isEmpty(treatmentPhaseSelect.getText()) || TextUtils.isEmpty(sideEffectsSelect.getText()) ||
                TextUtils.isEmpty(familySupportSelect.getText()) || TextUtils.isEmpty(reminderExposureSelect.getText())) {

            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
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
            // Pass the options array as the first parameter, and the text as the second
            float supportEncoded = getIndex(supportOptions, familySupportSelect.getText().toString());
            float reminderEncoded = getIndex(reminderOptions, reminderExposureSelect.getText().toString());

            // 4. Structure the Input Array (Shape: [1, 8] for 8 features)
            // Note: Update the order below to precisely match your training dataset schema
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
            // Assumes a single float output (e.g., adherence probability score between 0 and 1)
            float[][] outputVal = new float[1][2];

            // 6. Execute Model Inference
            tflite.run(inputVal, outputVal);

            // 7. Display Result
//            float predictionResult = outputVal[0][0];

            float class0Prob = outputVal[0][0];
            float class1Prob = outputVal[0][1];

            showResultDialog(class1Prob);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number inputs.", Toast.LENGTH_SHORT).show();
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
    private void showResultDialog(float score) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Analysis Result");

        // Customizing the message based on output score (assuming risk/probability output)
        String message = String.format("Adherence Risk Score: %.2f%%\n", score * 100);
        if (score > 0.5) {
            message += "Warning: High risk of non-adherence. Intervention recommended.";
        } else {
            message += "Patient adherence is tracking normally.";
        }

        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }


    @Override
    protected void onDestroy() {
        if (tflite != null) {
            tflite.close();
        }
        super.onDestroy();
    }

}
