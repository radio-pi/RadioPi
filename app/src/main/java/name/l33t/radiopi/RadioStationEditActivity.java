package name.l33t.radiopi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import name.l33t.radiopi.database.AppDatabase;
import name.l33t.radiopi.database.RadioStation;

public class RadioStationEditActivity extends AppCompatActivity {
    private AppDatabase db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_station_edit);
        db = AppDatabase.getInstance(getApplicationContext());
    }

    public void onSaveClick(MenuItem item) {
        EditText url_edittext = findViewById(R.id.input_url);
        EditText name_edittext = findViewById(R.id.input_name);
        String name = name_edittext.getText().toString();
        String url = url_edittext.getText().toString();
        new Save().execute(new RadioStation(name, url, "", 1));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_radio_station_edit, menu);
        return true;
    }

    class Save extends AsyncTask<RadioStation, String, Void> {
        @Override
        protected Void doInBackground(RadioStation... radioStations) {
            db.radioStationDao().insertAll(radioStations);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }
}
