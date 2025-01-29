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
        db.execSQL("CREATE TABLE userinfo_table (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " height REAL, weight REAL, age REAL, weightlossgoal TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Will not be called until SCHEMA_VERSION increases
        // Here we can upgrade the database e.g. add more tables
    }

    // Read all records from userinfo_table
    public Cursor getAll() {
        return (getReadableDatabase().rawQuery("SELECT _id, height, weight, age, weightlossgoal FROM userinfo_table", null));
    }

    public boolean ifPromptsDone() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT _id, height, weight, age, weightlossgoal FROM userinfo_table", null);
        if (cursor != null && cursor.moveToFirst()) return true;
        return false;
    }

    public Cursor getById(String id) {
        String[] args = {id};

        return (getReadableDatabase().rawQuery(
                "SELECT _id, height, weight, age, weightlossgoal FROM userinfo_table WHERE _ID = ?", args));
    }

    // Write a record into userinfo_table
    public long insert(double height, double weight, double age, String weightlossgoal) {
        ContentValues cv = new ContentValues();
        cv.put("height", height);
        cv.put("weight", weight);
        cv.put("age", age);
        cv.put("weightlossgoal", weightlossgoal);
        long userId = getWritableDatabase().insert("userinfo_table", "null", cv);

        getWritableDatabase().close();
        return userId;
    }

    public void update(String id, double height, double weight, double age, String weightlossgoal) {
        ContentValues cv = new ContentValues();
        String[] args = {id};
        cv.put("height", height);
        cv.put("weight", weight);
        cv.put("age", age);
        cv.put("weightlossgoal", weightlossgoal);
        getWritableDatabase().update("userinfo_table", cv, "_ID = ?", args);
    }
    public Cursor getLastRecord() {
        // Select the last record by sorting by _id in descending order and limiting the result to 1
        return getReadableDatabase().rawQuery(
                "SELECT _id, height, weight, age, weightlossgoal FROM userinfo_table ORDER BY _id DESC LIMIT 1", null);
    }

    public String getID(Cursor c) { return (c.getString(0)); }
    public double getHeight(Cursor c) {
        return (c.getDouble(1));
    }

    public double getWeight(Cursor c) {
        return (c.getDouble(2));
    }

    public double getAge(Cursor c) {
        return (c.getDouble(3));
    }
    public String getWeightLossGoal(Cursor c) {
        return (c.getString(4));
    }
}
