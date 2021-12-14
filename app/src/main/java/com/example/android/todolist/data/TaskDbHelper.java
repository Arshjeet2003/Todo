package com.example.android.todolist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "Item_list.db";

    private static final int DATABASE_VERSION = 1;

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + TaskContract.TaskEntry.TABLE_NAME + " ("
                + TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TaskContract.TaskEntry.COLUMN_TASK_NAME + " TEXT NOT NULL, "
                + TaskContract.TaskEntry.COLUMN_TASK_DESCRIPTION + " TEXT, "
                + TaskContract.TaskEntry.COLUMN_TASK_PRIORITY + " INTEGER NOT NULL, "
                + TaskContract.TaskEntry.COLUMN_TASK_TIME + " INTEGER NOT NULL DEFAULT 0,"
                + TaskContract.TaskEntry.COLUMN_TASK_STATUS + " TEXT NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

