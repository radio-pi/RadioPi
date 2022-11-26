package name.l33t.radiopi;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import name.l33t.radiopi.database.AppDatabase;
import name.l33t.radiopi.database.RadioStation;

public class RadioStationListActivity extends AppCompatActivity {

    private static AppDatabase db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_station_list);

        db = AppDatabase.getInstance(getApplicationContext());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> startActivity(new Intent(v.getContext(), RadioStationEditActivity.class)));
        new UpdateListView().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        new UpdateListView().execute();
    }

    class UpdateListView extends AsyncTask<Void, String, List<RadioStation>> {
        @Override
        protected List<RadioStation> doInBackground(Void... voids) {
            return db.radioStationDao().getAll();
        }

        @Override
        protected void onPostExecute(List<RadioStation> radioStations) {
            super.onPostExecute(radioStations);
            RadioStationListArrayAdapter itemsAdapter = new RadioStationListArrayAdapter(getApplicationContext(), radioStations);
            ListView listView = findViewById(R.id.radioList);
            listView.setAdapter(itemsAdapter);
        }
    }

    class RadioStationListArrayAdapter extends ArrayAdapter<RadioStation>{
        private final static int resource = android.R.layout.simple_list_item_1;
        public RadioStationListArrayAdapter(Context context, List<RadioStation> radioStations) {
            super(context, resource, radioStations);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            RadioStation station = getItem(position);

            //Check if an existing view is being reused,
            // otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(this.resource, parent, false);
            }

            TextView stationNameView = convertView.findViewById(android.R.id.text1);
            stationNameView.setText(station.getName());
            stationNameView.setTag(position);

            stationNameView.setOnClickListener(v -> {
                Intent intend = new Intent(v.getContext(), MainActivity.class);
                intend.putExtra("RadioStationUrl", getItem((int)v.getTag()).getUrl());
                intend.setAction("name.l33t.radiopi.PLAY_STREAM");
                startActivity(intend);
            });

            /*stationNameView.setOnLongClickListener(v -> {
                public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                    return onLongListItemClick(v,pos,id);
                }
                Toast.makeText(v.getContext(), "xx" + v.getTag(), Toast.LENGTH_LONG).show();
                AlertDialog d = new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            // Continue with delete operation
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;
            });*/

            return convertView;
        }
    }


}
