package name.l33t.radiopi;

import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity implements RemotePlayer.RemotePlayerCallbackClass {


    private RemotePlayer rplayer;
    private DataAccess db;
    private Integer selectedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rplayer = new RemotePlayer(getApplicationContext());
        rplayer.registerCallback(this);
        db = new DataAccess(this);
        new AsyncListTask().execute();
        SeekBar bar = (SeekBar) findViewById(R.id.seekBar);
        bar.setOnSeekBarChangeListener(new seekbarOnChange());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void play(View view) {
        String url = db.getUrl(selectedIndex);
        String stationname = db.getStationName(selectedIndex);

        if(null != url)
        {
            rplayer.playUrl(url, stationname);
        }
        Log.d("playbutton", "pressed play");
    }

    public void stop(View view) {
        rplayer.stop();
        Log.d("stopbutton", "pressed stop");
    }

    @Override
    public void remotePlayerCallback(String message) {
        Log.d("callback", message);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private class seekbarOnChange implements SeekBar.OnSeekBarChangeListener {
        private Integer seekValue = 0;
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            seekValue = progress;
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d("seekbarOnChange", "End Progress is: " + seekValue);
            rplayer.volume(seekValue);
        }

    }

    public class OnItemClickListenerListViewItem implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TextView textViewItem = ((TextView) view.findViewById(R.id.itemText));
            selectedIndex = (Integer)textViewItem.getTag();
        }
    }

    private class AsyncListTask extends AsyncTask<Void, Void, RadioStationItem[]> {

        @Override
        protected RadioStationItem[] doInBackground(Void... Void) {
            List<RadioStationItem> radiostationList =  getAllTrackingModel();
            RadioStationItem[] stationArray = radiostationList.toArray(new RadioStationItem[radiostationList.size()]);
            Log.d("stationArray", Arrays.deepToString(stationArray));
            return stationArray;
        }

        @Override
        protected void onPostExecute(RadioStationItem[] result) {
            ListView listview = (ListView) findViewById(R.id.stationListView);
            listview.setOnItemClickListener(new OnItemClickListenerListViewItem());
            listview.setAdapter(new RadioStationArrayAdapter(MainActivity.this, R.layout.list_item_radio_station, result));
        }

        private List<RadioStationItem> getAllTrackingModel() {
            List<RadioStationItem> stationModels = new ArrayList<RadioStationItem>();
            Cursor cursor = db.query();

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                stationModels.add(new RadioStationItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3)));
                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();
            return stationModels;
        }
    }
}
