package com.example.android.todolist;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.todolist.data.TaskContract;

/**
 * {@link TaskCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class TaskCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link TaskCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public TaskCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //find individual views that we want to modify in the list item view
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priority_color = (TextView) view.findViewById(R.id.priority_color);
        TextView number = (TextView) view.findViewById(R.id.number);
        int nameColumnIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME);
        int priority = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_PRIORITY);
        int status = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_STATUS);
        String taskStatus = cursor.getString(status);
        String taskPriority = cursor.getString(priority);
        String taskName = cursor.getString(nameColumnIndex);

        number.setText(String.valueOf(cursor.getPosition()+1)+")");
        nameTextView.setText(taskName);
        int color_priority = Integer.parseInt(taskPriority);
        if(taskStatus.equals(R.string.done)){
            nameTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }
        GradientDrawable background = (GradientDrawable) priority_color.getBackground();
        switch (color_priority){
            case 0: background.setColor(context.getResources().getColor(R.color.green_lowPriority));
            break;
            case 1: background.setColor(context.getResources().getColor(R.color.orange_mediumPriority));
            break;
            case 2: background.setColor(context.getResources().getColor(R.color.red_highPriority));
            break;
            default: priority_color.setBackgroundColor(context.getResources().getColor(R.color.white));
        }
    }
}