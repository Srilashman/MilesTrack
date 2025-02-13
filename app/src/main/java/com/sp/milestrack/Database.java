package com.sp.milestrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "userinfo.db";
    private static final int SCHEMA_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Will be called once when the database is not created
        db.execSQL("CREATE TABLE userinfo_table (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " date TEXT, height REAL, weight REAL, age REAL, weightlossgoal REAL);");
        db.execSQL("CREATE TABLE record_table (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " date TEXT, distance REAL, duration TEXT, calories REAL, activity TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Will not be called until SCHEMA_VERSION increases
        // Here we can upgrade the database e.g. add more tables
    }

    // Read all records from userinfo_table
    public Cursor getAll() {
        return (getReadableDatabase().rawQuery("SELECT * FROM record_table ORDER BY _id DESC", null));
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
    public long insert(String date, double height, double weight, double age, double weightlossgoal) {
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

    public long insertrecord(String date, double distance, String duration, double calories, String activity) {
        ContentValues cv = new ContentValues();
        cv.put("date", date);  // Add date to ContentValues
        cv.put("distance", distance);  // Add date to ContentValues
        cv.put("duration", duration);
        cv.put("calories", calories);
        cv.put("activity", activity);
        long userId = getWritableDatabase().insert("record_table", "null", cv);

        getWritableDatabase().close();
        return userId;
    }


    public void update(String id, String date, double height, double weight, double age, double weightlossgoal) {
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

    // Validate if Weight Loss Goal is either a number or 'nil'
    public static boolean isValidWeightLossGoal(String input) {
        return input.matches("\\d+(\\.\\d+)?") || input.equals("nil");
    }

    // Convert 'nil' to 0 or parse the numeric input
    public static double parseWeightLossGoal(String input) {
        if (input.equals("nil")) {
            return 0.0;
        }
        return Double.parseDouble(input);
    }

    public void deleteRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("record_table", "_id = ?", new String[]{String.valueOf(id)});
        db.close();
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
    public double getWeightLossGoal(Cursor c) {return (c.getDouble(5));}
}
