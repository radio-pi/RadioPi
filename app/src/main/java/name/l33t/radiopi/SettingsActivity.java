package name.l33t.radiopi;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import name.l33t.radiopi.data.DataAccess;
import name.l33t.radiopi.data.RadioStationItem;
import name.l33t.radiopi.data.SettingRadioPiItem;

public class SettingsActivity extends ActionBarActivity {

    private DataAccess db;
    private SettingRadioPiItem firstsettingRadioPiItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DataAccess(this);

        EditText name = (EditText) findViewById(R.id.setting_name);
        EditText ip = (EditText) findViewById(R.id.setting_ip);

        firstsettingRadioPiItem = db.getFirstRadioPIDevice();
        name.setText(firstsettingRadioPiItem.Name());
        ip.setText(firstsettingRadioPiItem.Url());
    }

    public void save_settings(View view) {
        EditText name = (EditText) findViewById(R.id.setting_name);
        EditText ip = (EditText) findViewById(R.id.setting_ip);

        db.updateRadioPIDevice(firstsettingRadioPiItem.Id(), name.getText().toString(), ip.getText().toString());
        Toast.makeText(getApplicationContext(), R.string.successful_save, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
