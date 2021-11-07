package com.cinosarge.heap;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class InsertWordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_word);

        /*
         * We set a toolbar as the app bar. Check the Overridden methods
         * - onCreateOptionsMenu()
         * - onOptionsItemSelected()
         */
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_insert));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_insert, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
         * setEnabled(false) avoids query duplication. Notice that it is not necessary to
         * enable the menu item again at the end of the async task cause we're switching
         * to the previous activity
         */
        item.setEnabled(false);

        switch(item.getItemId()) {
            case R.id.action_done :
                boolean fieldsEmpty =
                        ((EditText) findViewById(R.id.insert_word)).getText().toString().equals("");

                if( fieldsEmpty ) {
                    item.setEnabled(true);// we haven't already added a word

                    Toast toast = Toast.makeText(InsertWordActivity.this,
                            getResources().getString(R.string.toast_warning_noword),
                            Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    new SearchAndStoreTask().execute();
                }
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Depending on if the definition is inserted or not, it serches de definition on wiktionary
     * and then insert the word inside the database
     */
    class SearchAndStoreTask extends AsyncTask<Void, Integer, Boolean> {

        private ContentValues valuesToInsert = null;
        private ProgressBar progressBar = null;


        @Override
        public void onPreExecute() {
            EditText word = (EditText) findViewById(R.id.insert_word);
            EditText definition = (EditText) findViewById(R.id.insert_definition);
            EditText source = (EditText) findViewById(R.id.insert_source);
            valuesToInsert = new ContentValues();
            valuesToInsert.put("WORD", word.getText().toString());
            valuesToInsert.put("DEFINITION", definition.getText().toString());
            valuesToInsert.put("SOURCE", source.getText().toString());

            progressBar = (ProgressBar) findViewById(R.id.insert_progress_bar);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... nada) {
            /*
             * Controlliamo la presenza di una connessione internet
             */
            boolean connectionAvailable = false;

            ConnectivityManager connectivityManager = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);

            if(connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null)
                    connectionAvailable = networkInfo.isConnected();
            }
            /*
             * In mancanza di una definizione viene cercata una definizione online usando
             * il Wiktionary di Wikipedia.
             */
            if(connectionAvailable && valuesToInsert.getAsString("DEFINITION").equals("")) {
                Document wiktionaryDoc = null;

                String URL = "https://en.wiktionary.org/wiki/"+
                        valuesToInsert.getAsString("WORD").toLowerCase();

                boolean urlIsValid = true;

                try {
                    // An invalid URL causes connect() to throw an IOException
                    wiktionaryDoc = Jsoup.connect(URL).get();
                }
                catch(IOException e) {
                    urlIsValid = false;
                }

                // Task completion: 30%
                for(int i = 0; i < 30; i++) {
                    publishProgress(i);
                }

                if(urlIsValid && wiktionaryDoc != null) {
                    String definition = "";
                    Element contentText =  wiktionaryDoc.getElementById("mw-content-text");
                    Element parserOutput = contentText.selectFirst(".mw-parser-output");
                    Element orderedList = parserOutput.getElementsByTag("ol").first();
                    Elements listItems = orderedList.getElementsByTag("li");

                    StringBuilder builder = new StringBuilder("");

                    for(int i = 0; i < listItems.size(); i++) {
                        builder.append( (i + 1) + ". " + listItems.get(i).text() + "\n");
                    }

                    definition = builder.toString();
                    valuesToInsert.put("DEFINITION", definition);
                }
            }

            // Task completion: 70%
            for(int i = 30; i < 70; i++) {
                publishProgress(i);
            }

            HeapDatabaseHelper databaseHelper = new HeapDatabaseHelper(InsertWordActivity.this);
            try{
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.insert("ENTRY", null, valuesToInsert);
                db.close();

                // Task completion: 100%
                for(int i = 70; i < 100; i++) {
                    publishProgress(i);
                }

                return true;
            }
            catch(SQLiteException e) {
                return false;
            }
        }

        @Override
        public void onProgressUpdate(Integer[] progress) {
                progressBar.setProgress(progress[0]);
        }

        @Override
        public void onPostExecute(Boolean success) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);

            if(!success) {
                Toast toast = Toast.makeText(InsertWordActivity.this,
                        getResources().getString(R.string.toast_error_database),
                        Toast.LENGTH_LONG);
                toast.show();
            }

            // back to MainActivity
            finish();
        }
    }// END OF ASYNCTASK
}
