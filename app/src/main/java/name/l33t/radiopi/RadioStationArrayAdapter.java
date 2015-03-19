package name.l33t.radiopi;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RadioStationArrayAdapter extends ArrayAdapter<RadioStationItem> {

    // declaring our ArrayList of items
    private RadioStationItem[] objects;
    private Context context;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public RadioStationArrayAdapter(Context context, int textViewResourceId, RadioStationItem[] objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        if (null == convertView) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.list_item_radio_station, null);
        } else {
            row = convertView;
        }

        RadioStationItem rStation = getItem(position);

        TextView tv = (TextView) row.findViewById(R.id.itemText);
        tv.setText(rStation.Name());
        tv.setTag(rStation.Id());

        return row;
    }
}

