package com.example.android.todolist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import android.os.Bundle;
import android.widget.Toast;

import com.example.android.todolist.data.TaskContract;
import com.example.android.todolist.data.TaskContract.TaskEntry;
import com.example.android.todolist.data.TaskDbHelper;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */
    private static final int EXISTING_TASK_LOADER = 0;

    /**
     * Content URI for the existing pet (null if it's a new pet)
     */
    private Uri mCurrentTaskUri;


    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mDescriptionEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mTimeEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mPrioritySpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mPriority = 0;

    /** Boolean flag that keeps track of whether the pet has been edited (true) or not (false) */
    private boolean mTaskHasChanged = false ;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mTaskHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one
        Intent intent = getIntent();
        mCurrentTaskUri = intent.getData();

        //If the intent DOES NOT contain a pet content URI , then we know that we
        //creating a new pet.
        if (mCurrentTaskUri == null) {
            setTitle(R.string.editor_activity_title_new_task);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_task));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_TASK_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mTimeEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mPrioritySpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mTimeEditText.setOnTouchListener(mTouchListener);
        mPrioritySpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter prioritySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_priority_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        prioritySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mPrioritySpinner.setAdapter(prioritySpinnerAdapter);

        // Set the integer mSelected to the constant values
        mPrioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.medium_priority))) {
                        mPriority = TaskEntry.PRIORITY_MEDIUM;
                    } else if (selection.equals(getString(R.string.high_priority))) {
                        mPriority = TaskEntry.PRIORITY_HIGH;
                    } else {
                        mPriority = TaskEntry.PRIORITY_LOW;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPriority = 0; // Unknown
            }
        });
    }

    //Get user input from editor and save pet into database.
    private void saveTask() {
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String timeString = mTimeEditText.getText().toString().trim();

        if( mCurrentTaskUri==null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(descriptionString)
                && TextUtils.isEmpty(timeString) && mPriority==TaskEntry.PRIORITY_LOW)
        {
            return;
        }


        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_TASK_NAME, nameString);
        values.put(TaskEntry.COLUMN_TASK_DESCRIPTION, descriptionString);
        values.put(TaskEntry.COLUMN_TASK_PRIORITY, mPriority);

        // If the time is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int time = 0;

        if(!TextUtils.isEmpty(timeString))
        {
            time = Integer.parseInt(timeString);
        }
        values.put(TaskEntry.COLUMN_TASK_TIME,time);

        if(mCurrentTaskUri==null)
        {
            // This is a NEW pet, so insert a new pet into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(TaskEntry.CONTENT_URI , values);

            // Show a toast message depending on whether or not the insertion was successful.
            if(newUri==null)
            {
                Toast.makeText(this, "Inserting new task failed",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"Inserting task successful",Toast.LENGTH_LONG).show();

            }
        }else{
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.

            int rowsAffected = getContentResolver().update(mCurrentTaskUri,values,null,null);

            // Show a toast message depending on whether or not the update was successful.
            if(rowsAffected == 0)
            {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this,"Inserting new task failed",Toast.LENGTH_SHORT).show();
            }
            else
            {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this,"Inserting new task successful",Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        // If this is a new pet, hide the "Delete" menu item.
        if(mCurrentTaskUri==null)
        {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // saving pet in database
                saveTask();
                //going back to catalog activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:

                if(!mTaskHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);

                            }
                        };


                showUnsavedChangesDialog(discardButtonClickListener);

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed()
    {
        // If the pet hasn't changed, continue with handling back button press
        if(!mTaskHasChanged)
        {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                TaskEntry._ID,
                TaskEntry.COLUMN_TASK_NAME,
                TaskEntry.COLUMN_TASK_DESCRIPTION,
                TaskEntry.COLUMN_TASK_PRIORITY,
                TaskEntry.COLUMN_TASK_TIME
        };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, //Parent activity content
                mCurrentTaskUri,      // Query the content URI for the current pet
                projection,          // Columns to include in the resulting Cursor
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToNext()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DESCRIPTION);
            int priorityColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_PRIORITY);
            int timeColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TIME);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(descriptionColumnIndex);
            int priority = cursor.getInt(priorityColumnIndex);
            int weight = cursor.getInt(timeColumnIndex);

            // Update the views on the screen with the values from the database

            mNameEditText.setText(name);
            mDescriptionEditText.setText(breed);
            mTimeEditText.setText(Integer.toString(weight));

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female)
            // Then call setSelection() so that option is displayed on screen as the current selection
            switch (priority) {
                case TaskEntry.PRIORITY_MEDIUM:
                    mPrioritySpinner.setSelection(1);
                    break;
                case TaskEntry.PRIORITY_HIGH:
                    mPrioritySpinner.setSelection(2);
                    break;
                case TaskEntry.PRIORITY_LOW:
                    mPrioritySpinner.setSelection(0);
                    break;
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields

        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mTimeEditText.setText("");
        mPrioritySpinner.setSelection(0);

    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener){
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changed_dialog_msg);
        builder.setPositiveButton(R.string.Discard,discardButtonClickListener );
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if(dialog != null)
                {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog()
    {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if(dialog!=null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet()
    {
        // Only perform the delete if this is an existing pet
        if(mCurrentTaskUri !=null)
        {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentTaskUri,null,null);

            // Show a toast message depending on whether or not the delete was successful.
            if(rowsDeleted==0)
            {
                // If no rows were deleted, then there was an error with the delete
                Toast.makeText(this,"Deleting task failed.",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this,"Task deleted",
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();

    }

}