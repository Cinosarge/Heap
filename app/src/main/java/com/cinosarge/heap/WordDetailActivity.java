package com.cinosarge.heap;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.HeaderViewListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class WordDetailActivity extends AppCompatActivity {

    public static final String WORD_ID = "wordId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);

        /*
         * We set a toolbar as the app bar. Check the Overridden methods
         * - onCreateOptionsMenu()
         * - onOptionsItemSelected()
         */
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_detail));
    }

    @Override
    protected void onStart() {
        super.onStart();
        int drinkId = (int) getIntent().getExtras().getLong(WORD_ID);

        // Creating a cursor on a background task and showsing the stuff inside the text views
        (new ShowDetailsAsyncTask()).execute(drinkId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_edit_entry:
                Intent intent = new Intent(this, EditWordActivity.class);
                intent.putExtra(EditWordActivity.EXTRA_ENTRY_ID, Long.toString(getIntent().getExtras().getLong(WORD_ID)));
                intent.putExtra(EditWordActivity.EXTRA_WORD, ((TextView) findViewById(R.id.word)).getText().toString());
                intent.putExtra(EditWordActivity.EXTRA_DEFINITION, ((TextView) findViewById(R.id.definition)).getText().toString());
                intent.putExtra(EditWordActivity.EXTRA_SOURCE, ((TextView) findViewById(R.id.source)).getText().toString());
                startActivity(intent);
                break;
            case R.id.action_delete_entry :

                /*
                 * Selecting the delete action causes an AlertDialog to appear; this is the listener
                 * for the the AlertDialog buttons.
                 */
                DialogInterface.OnClickListener deleteDialogListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch(which) {
                            case DialogInterface.BUTTON_POSITIVE :
                                // Delete the work
                                int drinkId = (int) getIntent().getExtras().getLong(WORD_ID);
                                ( new DeleteEntryAsyncTask() ).execute(drinkId); // The AsyncTask calls the finish() funcion then

                                /*
                                 * YOU FOUND A SPOOKY SECRET
                                 * There was a Toast here and it had not been shown.. why?
                                 */

                                break;
                            default:
                                ; // Nothing happens if the No button is clicked
                        }
                    }
                };

                /*
                 * Creating and showing the AlertDialog
                 */
                AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this);
                deleteDialogBuilder.setMessage("Are you sure?")
                        .setPositiveButton("Yes", deleteDialogListener)
                        .setNegativeButton("No", deleteDialogListener).show();

                break;
            default:
                // Unforeseen consequences... not really unforeseen since computer science is not an exact science..
                Toast toast = Toast.makeText(this,
                        getResources().getString(R.string.toast_unforeseen_error),
                        Toast.LENGTH_LONG);
                toast.show();
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * ASYNC TASK
     * Used to show the details of an entry
     */
    private class ShowDetailsAsyncTask extends AsyncTask<Integer, Void, Boolean> {

        String word = null;
        String description = null;
        String source = null;

        @Override
        protected Boolean doInBackground(Integer... drinkId) {
            SQLiteOpenHelper databaseHelper = new HeapDatabaseHelper(WordDetailActivity.this);
            try{
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("ENTRY",
                        new String[] {"WORD", "DEFINITION", "SOURCE"},
                        "_id = ?",
                        new String[] {Integer.toString(drinkId[0])} ,
                        null, null, null);
                if(cursor.moveToFirst()) {
                    word = cursor.getString(0);
                    description = cursor.getString(1);
                    source = cursor.getString(2);
                }
                cursor.close();
                db.close();
                return true;
            }
            catch(SQLiteException e) {
                return false;
            }
        }

        public void onPostExecute(Boolean success) {
            if(!success) {
                Toast toast = Toast.makeText(WordDetailActivity.this,
                        getResources().getString(R.string.toast_error_database),
                        Toast.LENGTH_LONG);
                toast.show();
            }
            else{
                TextView wordTextView = (TextView) findViewById(R.id.word);
                TextView descriptionTextView = (TextView) findViewById(R.id.definition);
                TextView sourceTextView = (TextView) findViewById(R.id.source);
                wordTextView.setText(word);
                descriptionTextView.setText(description);
                sourceTextView.setText(source);
            }
        }
    }// END OF ASYNC TASK

    /*
     * ASYNC TASK
     * Delete an entry in a background task
     */
    private class DeleteEntryAsyncTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... drinkId) {
            SQLiteOpenHelper databaseHelper = new HeapDatabaseHelper(WordDetailActivity.this);
            try {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.delete("ENTRY",
                        "_id = ?",
                        new String[] {Integer.toString(drinkId[0])});
                db.close();
                return true;
            }
            catch(SQLiteException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success) {
                Toast toast = Toast.makeText(WordDetailActivity.this, "Entry has been deleted",
                        Toast.LENGTH_LONG);
                toast.show();
            }
            else {
                Toast toast = Toast.makeText(WordDetailActivity.this, "Could not delete",
                        Toast.LENGTH_LONG);
                toast.show();
            }

            /*
             * Not suggested to go back to MainActivity if the AsyncTask has not yet completed
             * running; that's why the finish() method is called from here. The finish() method
             * allows to go back to the previous activity withou sending an intent.
             */
            finish();
        }
    }// END OF ASYNCTASK
}
