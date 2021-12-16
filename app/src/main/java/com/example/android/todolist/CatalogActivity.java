package com.example.android.todolist;

import androidx.appcompat.app.AppCompatActivity;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.todolist.data.TaskContract.TaskEntry;
import com.example.android.todolist.data.TaskDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_LOADER = 0;

    TaskCursorAdapter mCursorAdapter;

    private TaskDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        // Find the ListView which will be populated with the pet data
        ListView taskListView= (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        taskListView.setEmptyView(emptyView);

        mCursorAdapter = new TaskCursorAdapter(this ,null);
        taskListView.setAdapter(mCursorAdapter);
        taskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences_key),MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.status_key),getString(R.string.done));
                editor.apply();
                Toast.makeText(getApplicationContext(),"Press save to complete task.",Toast.LENGTH_LONG).show();
                taskListView.performItemClick(view,position,id);
                return false;
            }
        });
//        setup the item click listener
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentPetUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, id);
                //setting the URI on the data field of the intent
                intent.setData(currentPetUri);
                intent.putExtra("id",position);
                startActivity(intent);
            }
        });

        //kick off the loader
        getLoaderManager().initLoader(TASK_LOADER,null,this);

    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets()
    {
        int rowsDeleted = getContentResolver().delete(TaskEntry.CONTENT_URI,null,null);
        Log.v("Catalog Activity" , rowsDeleted + " rows deleted from pet database");
        SharedPreferences preferences = getSharedPreferences("sharedpreferences_key2",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); editor.apply();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        // Respond to a click on the "Delete all entries" menu option
        if (item.getItemId() == R.id.action_delete_all_entries) {
            deleteAllPets();
            cancel_all_notifications();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                TaskEntry._ID,
                TaskEntry.COLUMN_TASK_NAME,
                TaskEntry.COLUMN_TASK_PRIORITY,
                TaskEntry.COLUMN_TASK_STATUS
        };
        return new CursorLoader(this,
                TaskEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
    private void cancel_all_notifications(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}