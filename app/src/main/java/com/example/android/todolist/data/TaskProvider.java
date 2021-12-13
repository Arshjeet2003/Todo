package com.example.android.todolist.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.android.todolist.data.TaskContract;

public class TaskProvider extends ContentProvider {

    public static final String LOG_TAG = TaskProvider.class.getSimpleName();

    private static final int TASKS = 100;
    private static final int TASK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // static initializer
    static {

        sUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_TASKS, TASKS);
        sUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_TASKS + "/#", TASK_ID);


    }


    //Database helper object
    private TaskDbHelper mDbHelper;


    @Override
    public boolean onCreate() {

        mDbHelper = new TaskDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {

            case TASKS:
                cursor = database.query(TaskContract.TaskEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case TASK_ID:

                selection = TaskContract.TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(TaskContract.TaskEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot Query unknown uri " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                return insertPets(uri, contentValues);
            default:
                throw new IllegalArgumentException("Inserting is not supported for" + uri);

        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPets(Uri uri, ContentValues values) {

        //check the name is not null
        String name = values.getAsString(TaskContract.TaskEntry.COLUMN_TASK_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Task requires a name");
        }

        //gender is valid or not
        Integer priority = values.getAsInteger(TaskContract.TaskEntry.COLUMN_TASK_PRIORITY);
        if (priority == null || !TaskContract.TaskEntry.isValidPriority(priority)) {
            throw new IllegalArgumentException("Requires valid gender");
        }

        // If the time is provided, check that it's greater than or equal to 0 kg
        Integer time = values.getAsInteger(TaskContract.TaskEntry.COLUMN_TASK_TIME);
        if (time != null && time < 0) {
            throw new IllegalArgumentException("Requires valid time");

        }
        // No need to check the breed, any value is valid (including null).


        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "failed to insert new row" + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri,null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);

    }


    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                return update(uri, contentValues, selection, selectionArgs);

            case TASK_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = TaskContract.TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateTask(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("update is not Supported for this" + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateTask(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(TaskContract.TaskEntry.COLUMN_TASK_NAME)) {
            String name = values.getAsString(TaskContract.TaskEntry.COLUMN_TASK_NAME);
            if (name == null) {
                throw new IllegalArgumentException("task requires name");
            }

        }
        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(TaskContract.TaskEntry.COLUMN_TASK_PRIORITY)) {
            Integer priority = values.getAsInteger(TaskContract.TaskEntry.COLUMN_TASK_PRIORITY);
            if (priority == null || !TaskContract.TaskEntry.isValidPriority(priority)) {
                throw new IllegalArgumentException(" Valid priority required");
            }
        }
        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(TaskContract.TaskEntry.COLUMN_TASK_TIME)) {
            Integer time = values.getAsInteger(TaskContract.TaskEntry.COLUMN_TASK_TIME);
            if (time != null && time < 0) {
                throw new IllegalArgumentException("Task requires valid time");
            }
        }
        if (values.size() == 0) {
            return 0;
        }

        // No need to check the breed, any value is valid (including null).
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowUpdated = database.update(TaskContract.TaskEntry.TABLE_NAME,values,selection,selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has change
        if(rowUpdated != 0)
        {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {

            case TASKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(TaskContract.TaskEntry.TABLE_NAME,selection,selectionArgs);
                break;

            case TASK_ID:
                // Delete a single row given by the ID in the URI
                selection = TaskContract.TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted =database.delete(TaskContract.TaskEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for" + uri);

        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted!=0)
        {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case TASKS:
                return TaskContract.TaskEntry.CONTENT_LIST_TYPE;
            case TASK_ID:
                return TaskContract.TaskEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri" + uri + "with match" +match);
        }
    }
}
