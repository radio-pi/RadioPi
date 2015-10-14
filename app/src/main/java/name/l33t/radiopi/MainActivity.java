package name.l33t.radiopi;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity implements RemotePlayer.RemotePlayerMessageCallbackClass, RemotePlayer.RemotePlayerVolumeCallbackClass, VolumeWebSocket.WebSocketVolumeCallbackClass {


    private RemotePlayer rplayer;
    private DataAccess db;
    private Integer selectedIndex;
    private VolumeWebSocket ws = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rplayer = new RemotePlayer(getApplicationContext());
        rplayer.registerMessageCallback(this);
        rplayer.registerVolumeCallback(this);
        //rplayer.getvolume(); // using now websockets

        db = new DataAccess(this);
        new AsyncListTask().execute();
        SeekBar bar = (SeekBar) findViewById(R.id.seekBar);
        bar.setOnSeekBarChangeListener(new seekbarOnChange());

        if ("google_sdk".equals( Build.PRODUCT )) {
            /*No one using this ipv6 anyway xD*/
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(ws == null)
        {
            try {
                ws = new VolumeWebSocket(new URI("ws://192.168.17.174:9000"));
                ws.registerVolumeCallback(this);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if(!ws.isOpen()){
            ws.connect();
        }
    }

    @Override
    protected void onStop(){
        if(ws != null && ws.isOpen()){
            ws.close();
            ws = null;
        }
        super.onStop();
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

        switch (id){
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
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
    public void remotePlayerMessageCallback(String message) {
        Log.d("callback", message);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void registerVolumeCallback(Integer vol) {
        SeekBar bar = (SeekBar) findViewById(R.id.seekBar);
        bar.setProgress(vol);
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
