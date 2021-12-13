package com.example.android.todolist;

import androidx.appcompat.app.AppCompatActivity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.example.android.todolist.data.TaskContract.TaskEntry;
import com.example.android.todolist.data.TaskDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER = 0;

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

//        setup the item click listener
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this , EditorActivity.class);

                Uri currentPetUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI,id);

                //setting the URI on the data field of the intent
                intent.setData(currentPetUri);
                startActivity(intent);
            }
        });

        //kick off the loader
        getLoaderManager().initLoader(PET_LOADER,null,this);

    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets()
    {
        int rowsDeleted = getContentResolver().delete(TaskEntry.CONTENT_URI,null,null);
        Log.v("Catalog Activity" , rowsDeleted + " rows deleted from pet database");

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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                TaskEntry._ID,
                TaskEntry.COLUMN_TASK_NAME,
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
}