package com.cinosarge.heap;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditWordActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "entryId";
    public static final String EXTRA_WORD = "entryWord";
    public static final String EXTRA_DEFINITION = "entryDefinition";
    public static final String EXTRA_SOURCE = "entrySource";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_word);

        /*
         * We set a toolbar as the app bar. Check the Overridden methods
         * - onCreateOptionsMenu()
         * - onOptionsItemSelected()
         */
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_update));
        ( (EditText) findViewById(R.id.update_word)).setText(getIntent().getExtras().get(EXTRA_WORD).toString());
        ( (EditText) findViewById(R.id.update_definition)).setText(getIntent().getExtras().get(EXTRA_DEFINITION).toString());
        ( (EditText) findViewById(R.id.update_source)).setText(getIntent().getExtras().get(EXTRA_SOURCE).toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
         * setEnabled(false) avoids query duplication. Notice that it is not necessary to
         * enable the menu item again at the end of the async task cause we're switching
         * to the previous activity. Anyway query duplication would not produce database
         * inconsistencies this time since it would apply many times the same update.
         */
        item.setEnabled(false);

        switch(item.getItemId()) {
            case R.id.action_update_done :
                boolean fieldsEmpty =
                        ((EditText) findViewById(R.id.update_word)).getText().toString().equals("");

                if( fieldsEmpty ) {
                    item.setEnabled(true);// we haven't already added a word

                    Toast toast = Toast.makeText(EditWordActivity.this,
                            getResources().getString(R.string.toast_warning_noword),
                            Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    new EditWordActivity.UpdateEntryAsyncTask().execute();
                }
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Update the word inside the database
     */
    private class UpdateEntryAsyncTask extends AsyncTask<Void, Void, Boolean> {

        ContentValues valuesToUpdate = null;

        @Override
        public void onPreExecute() {
            EditText word = (EditText) findViewById(R.id.update_word);
            EditText definition = (EditText) findViewById(R.id.update_definition);
            EditText source = (EditText) findViewById(R.id.update_source);
            valuesToUpdate = new ContentValues();
            valuesToUpdate.put("WORD", word.getText().toString());
            valuesToUpdate.put("DEFINITION", definition.getText().toString());
            valuesToUpdate.put("SOURCE", source.getText().toString());
        }

        @Override
        protected Boolean doInBackground(Void... nada) {
            HeapDatabaseHelper databaseHelper = new HeapDatabaseHelper(EditWordActivity.this);
            try{
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.update("ENTRY",
                        valuesToUpdate,
                        "_id = ?",
                        new String[] {getIntent().getExtras().get(EXTRA_ENTRY_ID).toString()});
                db.close();
                return true;
            }
            catch(SQLiteException e) {
                return false;
            }
        }

        public void onPostExecute(Boolean success) {
            if(success) {
                Toast toast = Toast.makeText(EditWordActivity.this,
                        getResources().getString(R.string.toast_message_update),
                        Toast.LENGTH_LONG);
                toast.show();
            }
            else {
                Toast toast = Toast.makeText(EditWordActivity.this,
                        getResources().getString(R.string.toast_error_database),
                        Toast.LENGTH_LONG);
                toast.show();
            }

            // back to WordDetailActivity
            finish();
        }
    }// END OF ASYNCTASK
}
