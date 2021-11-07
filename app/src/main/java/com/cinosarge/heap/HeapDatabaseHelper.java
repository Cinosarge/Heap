package com.cinosarge.heap;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class HeapDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "HeapDatabase";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "ENTRY";

    public static final String CREATE_TABLE_ENTRY =
            "CREATE TABLE ENTRY" +
                    "(" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "WORD TEXT, " +
                    "DEFINITION TEXT, " +
                    "SOURCE TEXT" +
                    ");";

    public HeapDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ENTRY);

        ContentValues contentValues = new ContentValues();
        contentValues.put("WORD", "hooded");
        contentValues.put("DEFINITION", "having or wearing a hood");
        contentValues.put("SOURCE", "Podcast - Nightvale - ep1");
        db.insert("ENTRY",
                null,
                contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
