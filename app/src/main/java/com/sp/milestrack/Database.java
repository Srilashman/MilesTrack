package com.sp.milestrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "userinfo.db";
    private static final int SCHEMA_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Will be called once when the database is not created
        db.execSQL("CREATE TABLE userinfo_table (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " date TEXT, height REAL, weight REAL, age REAL, weightlossgoal TEXT);");
        db.execSQL("CREATE TABLE trainingschedule_table (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " start_date TEXT, end_date TEXT);");
        db.execSQL("CREATE TABLE trainingplans_table (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " exercise TEXT, distance DECIMAL(10, 2), intensity TEXT, status BIT, date TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Will not be called until SCHEMA_VERSION increases
        // Here we can upgrade the database e.g. add more tables
    }

    // Read all records from userinfo_table
    public Cursor getAll() {
        return (getReadableDatabase().rawQuery("SELECT _id, date, height, weight, age, weightlossgoal FROM userinfo_table", null));
    }

    public long insertTrainingDate(String start_date, String end_date) {
        ContentValues cv = new ContentValues();
        cv.put("start_date", start_date);  // Add date to ContentValues
        cv.put("end_date", end_date);
        long userId = getWritableDatabase().insert("trainingschedule_table", "null", cv);

        getWritableDatabase().close();
        return userId;
    }
    
    public long insertTrainingPlans(String exercise, double distance, String intensity,String date, boolean status) {
        ContentValues cv = new ContentValues();
        cv.put("exercise", exercise);
        cv.put("distance", distance);
        cv.put("intensity", intensity);
        cv.put("status", status);
        cv.put("date", date);
        long userId = getWritableDatabase().insert("trainingplans_table", null, cv);

        getWritableDatabase().close();
        return userId;
    }

    public void updateTrainingPlansByDate(String date, boolean status, double dist, String exercise) {
        ContentValues cv = new ContentValues();
        String[] args = {date};
        cv.put("exercise", exercise);
        cv.put("distance", dist);
        cv.put("status", status);
        getWritableDatabase().update("trainingplans_table", cv, "date = ?", args);
    }

    public void updateTrainingPlans(String id, String exercise, double distance, String intensity,String date, boolean status) {
        ContentValues cv = new ContentValues();
        String[] args = {id};
        cv.put("exercise", exercise);
        cv.put("distance", distance);
        cv.put("intensity", intensity);
        cv.put("status", status);
        getWritableDatabase().update("trainingplans_table", cv, "_ID = ?", args);
    }

    public void deleteTraining() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM trainingplans_table");
            db.execSQL("DELETE FROM trainingschedule_table");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }


    public void updateTrainingDate(String start_date, String end_date) {
        ContentValues cv = new ContentValues();
        String[] args = {"1"};
        cv.put("start_date", start_date);
        cv.put("end_date", end_date);
        getWritableDatabase().update("trainingschedule_table", cv, "_ID = ?", args);
    }

    public boolean isPlansSet() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT _id, start_date, end_date FROM trainingschedule_table", null);
        boolean hasData = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return hasData;
    }


    public Cursor getAllTrainingPlans() {
        return (getReadableDatabase().rawQuery("SELECT _id, exercise, distance, intensity, status, date FROM trainingplans_table", null));
    }

    public Cursor getAllTrainingDates() {
        return (getReadableDatabase().rawQuery("SELECT _id, start_date, end_date FROM trainingschedule_table", null));
    }

    public String getSportFromDate(String date) {
        String[] args = {date};
        String ex = null;
        Cursor cursor = null; // Declare cursor

        try {
            cursor = getReadableDatabase().rawQuery(
                    "SELECT exercise FROM trainingplans_table WHERE date = ?", args); // Use correct column name

            if (cursor != null && cursor.moveToFirst()) {
                ex = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print error to log
        } finally {
            if (cursor != null) {
                cursor.close(); // Always close cursor
            }
        }
        return ex;
    }

    public double getDistFromDate(String date) {
        String[] args = {date};
        double ex = -1;
        Cursor cursor = null; // Declare cursor

        try {
            cursor = getReadableDatabase().rawQuery(
                    "SELECT distance FROM trainingplans_table WHERE date = ?", args); // Use correct column name

            if (cursor != null && cursor.moveToFirst()) {
                ex = cursor.getDouble(0);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print error to log
        } finally {
            if (cursor != null) {
                cursor.close(); // Always close cursor
            }
        }
        return ex;
    }

    public int getStatusFromDate(String date) {
        String[] args = {date};
        int ex = -1;
        Cursor cursor = null; // Declare cursor

        try {
            cursor = getReadableDatabase().rawQuery(
                    "SELECT status FROM trainingplans_table WHERE date = ?", args); // Use correct column name

            if (cursor != null && cursor.moveToFirst()) {
                ex = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print error to log
        } finally {
            if (cursor != null) {
                cursor.close(); // Always close cursor
            }
        }
        return ex;
    }


    public boolean ifPromptsDone() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT _id, height, weight, age, weightlossgoal FROM userinfo_table", null);
        if (cursor != null && cursor.moveToFirst()) return true;
        return false;
    }

    public Cursor getById(String id) {
        String[] args = {id};

        return (getReadableDatabase().rawQuery(
                "SELECT _id, date, height, weight, age, weightlossgoal FROM userinfo_table WHERE _ID = ?", args));
    }

    // Write a record into userinfo_table
    public long insert(String date, double height, double weight, double age, String weightlossgoal) {
        ContentValues cv = new ContentValues();
        cv.put("date", date);  // Add date to ContentValues
        cv.put("height", height);
        cv.put("weight", weight);
        cv.put("age", age);
        cv.put("weightlossgoal", weightlossgoal);
        long userId = getWritableDatabase().insert("userinfo_table", "null", cv);

        getWritableDatabase().close();
        return userId;
    }

    public void update(String id, String date, double height, double weight, double age, String weightlossgoal) {
        ContentValues cv = new ContentValues();
        String[] args = {id};
        cv.put("date", date);  // Add date to ContentValues
        cv.put("height", height);
        cv.put("weight", weight);
        cv.put("age", age);
        cv.put("weightlossgoal", weightlossgoal);
        getWritableDatabase().update("userinfo_table", cv, "_ID = ?", args);
    }
    public Cursor getLastRecord() {
        // Select the last record by sorting by _id in descending order and limiting the result to 1
        return getReadableDatabase().rawQuery(
                "SELECT _id, date, height, weight, age, weightlossgoal FROM userinfo_table ORDER BY _id DESC LIMIT 1", null);
    }

    public Cursor getAllBMIRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT date, weight, height FROM userinfo_table", null);
    }

    public String getID(Cursor c) { return (c.getString(0)); }
    public String getDate(Cursor c) {return c.getString(1);}  // Assuming 'date' is the second column (_id is first)

    public double getHeight(Cursor c) {
        return (c.getDouble(2));
    }

    public double getWeight(Cursor c) {
        return (c.getDouble(3));
    }

    public double getAge(Cursor c) {
        return (c.getDouble(4));
    }
    public String getWeightLossGoal(Cursor c) {
        return (c.getString(5));
    }
}
