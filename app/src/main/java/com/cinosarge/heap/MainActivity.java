package com.cinosarge.heap;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase db = null;
    private Cursor cursor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * We set a toolbar as the app bar. Check the Overridden methods
         * - onCreateOptionsMenu()
         * - onOptionsItemSelected()
         */
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main));

        /*
         * Background creation of a Cursor for use with a SimpleCursorAdapter.
         * Adapter set to the ListView.
         */
        ( new FetchWordsAsyncTask() ).execute();

        // We have to add an OnItemClickListener upon the word list
        ListView wordList = (ListView) findViewById(R.id.word_list);

        /*
         * Here we go, the OnItemClickListener is meant to call an activity to show details
         * for the word the use clicked upon
         */
        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, WordDetailActivity.class);
                intent.putExtra(WordDetailActivity.WORD_ID, id);
                startActivity(intent);
            }
        };

        wordList.setOnItemClickListener(onItemClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;

        switch(item.getItemId()) {
            case R.id.action_add_entry :
                intent = new Intent(this, InsertWordActivity.class);
                break;
            case R.id.action_import_export:
                intent = new Intent(this, ImportExportActivity.class);
                break;
            default :
                // NOTHING TO DO
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            cursor = db.query("ENTRY",
                    new String[]{"_id", "WORD"},
                    null,
                    null,
                    null, null, "WORD ASC");
        }
        catch(SQLiteException e) {
            Toast toast = Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.toast_error_database),
                    Toast.LENGTH_LONG);
        }
        ( (SimpleCursorAdapter) ( (ListView) findViewById(R.id.word_list) ).getAdapter() ).changeCursor(cursor);
    }

    /*
     * Meant to fetch the whole list of words and save into a cursor
     */
    private class FetchWordsAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            HeapDatabaseHelper databaseHelper = new HeapDatabaseHelper(MainActivity.this);
            try{
                db = databaseHelper.getReadableDatabase();
                cursor = db.query("ENTRY",
                        new String[]{"_id", "WORD"},
                        null,
                        null,
                        null, null, "WORD ASC");
                return true;
            }
            catch(SQLiteException e) {
                return false;
            }
        }

        public void onPostExecute(Boolean success) {
            if(!success) {
                Toast toast = Toast.makeText(MainActivity.this,
                        getResources().getString(R.string.toast_error_database),
                        Toast.LENGTH_LONG);
                toast.show();
            }
            else {
                // We have to add an Adapter and an OnItemClickListener to this ListView
                ListView wordList = (ListView) findViewById(R.id.word_list);

                // Simple Cursor Adapter to fetch the words
                SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(MainActivity.this,
                        android.R.layout.simple_list_item_1,
                        cursor,
                        new String[]{"WORD"},
                        new int[]{android.R.id.text1},
                        0);

                wordList.setAdapter(cursorAdapter);
            }
        }
    }// END OF ASYNCTASK

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }
}
