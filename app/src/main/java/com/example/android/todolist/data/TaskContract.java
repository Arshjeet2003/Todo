package com.example.android.todolist.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class TaskContract {

    private TaskContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.android.todolist";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TASKS = "tasks";

    public static final class TaskEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_TASKS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASKS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.ANY_CURSOR_ITEM_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASKS;

        public final static String TABLE_NAME = "tasks";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_TASK_NAME = "name";
        public final static String COLUMN_TASK_DESCRIPTION= "description";
        public final static String COLUMN_TASK_PRIORITY = "priority";
        public final static String COLUMN_TASK_TIME = "time";


        public static final int PRIORITY_LOW = 0;
        public static final int PRIORITY_MEDIUM = 1;
        public static final int PRIORITY_HIGH = 2;

        public static boolean isValidPriority(int priority)
        {
            if(priority==PRIORITY_LOW || priority==PRIORITY_MEDIUM || priority == PRIORITY_HIGH)
            {
                return true;
            }
            return false;
        }

    }
}
