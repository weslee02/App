package com.example.app;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private Cursor cursor;

    public PatientAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_card, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        // Move database cursor position to the current list item context
        if (!cursor.moveToPosition(position)) {
            return;
        }

        // Pull values out by mapping columns
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        float age = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AGE));
        String gender = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENDER));
        float distance = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DISTANCE));
        float missedDoses = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MISSED_DOSES));
        String phase = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TREATMENT_PHASE));
        String sideEffects = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIDE_EFFECTS));
        String support = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FAMILY_SUPPORT));
        String reminder = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_EXPOSURE));
        float score = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESULT_SCORE));

        // Bind data values directly into your UI Cards
        holder.tvCardId.setText("Patient Record #" + id);
        holder.tvCardScore.setText(String.format("Risk: %.1f%%", score * 100));
        holder.tvCardDemographics.setText("Age: " + (int)age + " | Gender: " + gender);
        holder.tvCardMetrics.setText("Distance: " + distance + "km | Missed Doses: " + (int)missedDoses);
        holder.tvCardDetails.setText("Phase: " + phase + "\nSupport: " + support + " | Side Effects: " + sideEffects + "\nReminders: " + reminder);
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    /**
     * Swap in a fresh cursor if database data updates
     */
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }



    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardId, tvCardScore, tvCardDemographics, tvCardMetrics, tvCardDetails;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the views using the IDs from your item_patient_card.xml
            tvCardId = itemView.findViewById(R.id.tvCardId);
            tvCardScore = itemView.findViewById(R.id.tvCardScore);
            tvCardDemographics = itemView.findViewById(R.id.tvCardDemographics);
            tvCardMetrics = itemView.findViewById(R.id.tvCardMetrics);
            tvCardDetails = itemView.findViewById(R.id.tvCardDetails);
        }
    }

}