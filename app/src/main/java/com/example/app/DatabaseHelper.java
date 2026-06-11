package com.example.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "AdherenceData.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "patient_records";

    // Column Names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_MISSED_DOSES = "missed_doses";
    public static final String COLUMN_TREATMENT_PHASE = "treatment_phase";
    public static final String COLUMN_SIDE_EFFECTS = "side_effects";
    public static final String COLUMN_FAMILY_SUPPORT = "family_support";
    public static final String COLUMN_REMINDER_EXPOSURE = "reminder_exposure";
    public static final String COLUMN_RESULT_SCORE = "result_score";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_AGE + " REAL, "
                + COLUMN_GENDER + " TEXT, "
                + COLUMN_DISTANCE + " REAL, "
                + COLUMN_MISSED_DOSES + " REAL, "
                + COLUMN_TREATMENT_PHASE + " TEXT, "
                + COLUMN_SIDE_EFFECTS + " TEXT, "
                + COLUMN_FAMILY_SUPPORT + " TEXT, "
                + COLUMN_REMINDER_EXPOSURE + " TEXT, "
                + COLUMN_RESULT_SCORE + " REAL" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    /**
     * Inserts a complete analysis row into the SQLite database.
     */
    public boolean insertRecord(float age, String gender, float distance, float missedDoses,
                                String phase, String sideEffects, String support, String reminder, float score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_AGE, age);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_DISTANCE, distance);
        values.put(COLUMN_MISSED_DOSES, missedDoses);
        values.put(COLUMN_TREATMENT_PHASE, phase);
        values.put(COLUMN_SIDE_EFFECTS, sideEffects);
        values.put(COLUMN_FAMILY_SUPPORT, support);
        values.put(COLUMN_REMINDER_EXPOSURE, reminder);
        values.put(COLUMN_RESULT_SCORE, score);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1; // Returns true if insertion succeeded
    }



    /**
     * Fetches all saved patient records ordered from newest to oldest.
     */
    public Cursor getAllRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_NAME,
                null, // null selects all columns
                null,
                null,
                null,
                null,
                COLUMN_ID + " DESC" // Order by newest first
        );
    }

}