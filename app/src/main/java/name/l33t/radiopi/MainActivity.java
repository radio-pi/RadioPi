package name.l33t.radiopi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import name.l33t.radiopi.database.AppDatabase;
import name.l33t.radiopi.database.RadioStation;
import name.l33t.radiopi.network.Callback;
import name.l33t.radiopi.network.WebApi;
import name.l33t.radiopi.network.WebSocket;

public class MainActivity extends AppCompatActivity implements Callback.Volume, Callback.Title, Callback.NowPlaying {

    private static final String CHANNEL_ID = "PlayingRadioStation";
    private static final int NOTIFICATION_ID = 1313;
    private static final int NOTIFICATION_PENDING_INTENT_ID = 14;

    private static AppDatabase db = null;
    private static WebSocket ws = null;
    private static WebApi api = null;
    private static boolean isPlaying = false;
    private static boolean isMuted = false;
    private static int volumeBeforeMuting;
    private static String currentlyPlayingStationUrl;
    private static String currentlyPlayingStationTitle;
    private static int currentVolume;
    private static SharedPreferences defaultSharedPreferences = null;

    @Override
    protected void onStart() {
        super.onStart();
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String ip = defaultSharedPreferences.getString("ip", "");
        if (ip != null && ip.equals("")) {
            Snackbar.make(findViewById(R.id.titleTextView), getString(R.string.ip_not_set), Snackbar.LENGTH_INDEFINITE)
                    .show();
        } else {
            new ConnectToWebSocketTask().execute(this);
        }
        setPlayStopButton();

        if(currentlyPlayingStationTitle != null && !currentlyPlayingStationTitle.equals("")) {
            setTitleTextView(currentlyPlayingStationTitle);
            createNotification(currentlyPlayingStationTitle);
        }

        if(currentlyPlayingStationUrl != null && !currentlyPlayingStationUrl.equals("")) {
            new SetRadioStationImageTask(findViewById(R.id.imageView)).execute(currentlyPlayingStationUrl);
        }

        if(currentVolume > 0) {
            setVolumeSeekBar(currentVolume);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = AppDatabase.getInstance(getApplicationContext());

        String ip = PreferenceManager.getDefaultSharedPreferences(this).getString("ip", "");
        new ConnectToApiTask().execute(ip);
        createNotificationChannel();
        if(getIntent() != null && getIntent().getExtras() != null &&
                getIntent().getAction() != null && getIntent().getAction().equals("name.l33t.radiopi.PLAY_STREAM")){
            String newPlayingStationUrl = getIntent().getExtras().getString("RadioStationUrl");
            Log.d("Main", "got intent " + getIntent().toString() + " with extras " + newPlayingStationUrl);

            if(newPlayingStationUrl != null && !newPlayingStationUrl.equals("")) {
                currentlyPlayingStationUrl = newPlayingStationUrl;
                new SwitchChannelTask().execute(currentlyPlayingStationUrl);
                new SetRadioStationImageTask(findViewById(R.id.imageView)).execute(currentlyPlayingStationUrl);
            }
        }

        new Thread(() -> {
            String oldTitle = "";
            while (true) {
                if (currentlyPlayingStationTitle != null)
                    oldTitle = currentlyPlayingStationTitle;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (currentlyPlayingStationTitle != null && !oldTitle.equals(currentlyPlayingStationTitle)) {
                    runOnUiThread(() -> setTitleTextView(currentlyPlayingStationTitle));
                }
            }
        }).start();

        SeekBar volumeSeekBar = findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int volume;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {volume = progress;}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new SetVolumeTask().execute(volume);
                isMuted = volume == 0;
                currentVolume = volume;
                setMuteButton();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cancelNotification();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRadioStationClick(View view) {
        Intent intent = new Intent(this, RadioStationListActivity.class);
        startActivity(intent);
    }

    public void onPlayPauseClick(View view) {
        switchPlayStop();
    }

    public void onMuteClick(View view) {
        switchMuted();
    }

    //region Websocket Callbacks

    @Override
    public void updateVolume(int volume) {
        Log.d("MainActivity", "Update Volume: " + volume);
        if(volume >= 0) {
            isMuted = volume == 0;
            currentVolume = volume;

            runOnUiThread(() -> {
                setVolumeSeekBar(volume);
                setMuteButton();
            });
        } else {
            isPlaying = false;
        }
    }

    @Override
    public void updateTitle(String title) {
        Log.d("MainActivity", "Update title: " + title);
        currentlyPlayingStationTitle = title;
        createNotification(title);
        //runOnUiThread(() -> setTitleTextView(title));
        //runOnUiThread(() -> setTitleTextView(title));
    }

    @Override
    public void updateNowPlaying(String streamkey) {
        Log.d("MainActivity", "Update Now playing stream key: " + streamkey);
        isPlaying = !streamkey.equals(""); // If streamkey is empty, no stream is playing.
        currentlyPlayingStationUrl = streamkey;
        new SetRadioStationImageTask(findViewById(R.id.imageView)).execute(currentlyPlayingStationUrl);
        runOnUiThread(this::setPlayStopButton);
        runOnUiThread(this::setPlayStopButton);
    }

    //endregion

    private void setTitleTextView(String title) {
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText(title);
    }

    private static class SetRadioStationImageTask extends AsyncTask<String, Void, Bitmap> {
        private WeakReference radioStationImageView;

        SetRadioStationImageTask(final View radioStationImageView) {
            this.radioStationImageView = new WeakReference<>(radioStationImageView);
        }

        @Override
        protected Bitmap doInBackground(String... stationUrls) {
            String imageUri = null;
            Bitmap stationImage = null;
            List<RadioStation> stations = db.radioStationDao().getAll();
            for(RadioStation station : stations) {
                if (station.getUrl().equals(stationUrls[0])) {
                    imageUri = station.getImg();
                }
            }
            if(imageUri == null)
            {
                return null; // early return for empty url
            }

            try {
                URL url = new URL(imageUri);
                InputStream stream = url.openStream();
                stationImage = BitmapFactory.decodeStream(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stationImage;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView view = ((ImageView)radioStationImageView.get());
            if (bitmap != null && view != null) {
                view.setImageBitmap(bitmap);
            } else if (bitmap == null && view != null) {
                view.setImageResource(R.mipmap.ic_launcher);
            }
        }
    }

    private void switchMuted() {
        MuteTask muteTask = new MuteTask();
        View view = findViewById(R.id.muteButton);
        if(isMuted) {
            //Unmute
            isMuted = false;
            view.setBackgroundResource(R.drawable.volume);
            muteTask.execute(false);
            setVolumeSeekBar(volumeBeforeMuting);
        } else {
            //Mute
            isMuted = true;
            view.setBackgroundResource(R.drawable.volume_off);
            muteTask.execute(true);
            setVolumeSeekBar(0);
        }
    }

    private void switchPlayStop() {
        View view = findViewById(R.id.playStopImageButton);
        if (isPlaying) {
            //Stop
            isPlaying = false;
            view.setBackgroundResource(R.drawable.play);
            new StopPlayingTask().execute();
            cancelNotification();
        } else {
            //Play
            isPlaying = true;
            view.setBackgroundResource(R.drawable.stop);
            new StartPlayingTask(view).execute(currentlyPlayingStationUrl);
            if(currentlyPlayingStationTitle != null && !currentlyPlayingStationTitle.equals("")) {
                createNotification(currentlyPlayingStationTitle);
            }
        }
    }

    private void setPlayStopButton() {
        View view = findViewById(R.id.playStopImageButton);
        if (isPlaying) {
            view.setBackgroundResource(R.drawable.stop);
        } else {
            view.setBackgroundResource(R.drawable.play);
        }
    }

    private void setMuteButton() {
        View view = findViewById(R.id.muteButton);
        if (isMuted) {
            view.setBackgroundResource(R.drawable.volume_off);
        } else {
            view.setBackgroundResource(R.drawable.volume);
        }
    }

    private void setVolumeSeekBar(int volume) {
        SeekBar volumeSeekBar = findViewById(R.id.volumeSeekBar);
        int progress = volumeSeekBar.getProgress();
        if (progress == volume) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            volumeSeekBar.setProgress(volume, true);
        } else {
            volumeSeekBar.setProgress(volume);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel_name";
            String description = "channel_description";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviours after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification(String stationTitle) {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, NOTIFICATION_PENDING_INTENT_ID, mainActivityIntent, PendingIntent.FLAG_MUTABLE);

        Notification notification =  new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("RadioPi Player")
                .setTicker("RadioPi Player")
                .setContentText(stationTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setSmallIcon(R.drawable.play)
                .setChannelId(CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void cancelNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private static class StopPlayingTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            ensureApiAvailable();

            api.stop();
            return null;
        }
    }

    private static class StartPlayingTask extends AsyncTask<String, Void, Boolean> {
        private WeakReference startStopButton;

        StartPlayingTask(final View startStopButton) {
            this.startStopButton = new WeakReference<>(startStopButton);
        }

        @Override
        protected Boolean doInBackground(String... radioStreamUrls) {
            ensureApiAvailable();

            return api.play(radioStreamUrls[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                isPlaying = false;
                if(startStopButton != null) {
                    View startStopImageButton = (View) startStopButton.get();
                    if (startStopImageButton != null) {
                        startStopImageButton.setBackgroundResource(R.drawable.stop);
                    }
                }
            }
        }
    }

    private static class SwitchChannelTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... radioStreamUrls) {
            ensureApiAvailable();

            return api.play(radioStreamUrls[0]);
        }
    }

    private static class SetVolumeTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... volumes) {
            ensureApiAvailable();

            api.setVolume(volumes[0]);
            return null;
        }
    }

    private static class MuteTask extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... mute) {
            if(mute[0]) {
                try {
                    volumeBeforeMuting = api.getVolume();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new SetVolumeTask().execute(0);
            } else {
                new SetVolumeTask().execute(volumeBeforeMuting);
            }
            return null;
        }
    }

    /**
     * Ensures that the api is available. Never call from the ui thread.
     */
    private static void ensureApiAvailable() {
        String ip = defaultSharedPreferences.getString("ip", "");
        if(api == null || api.getVolume() == -1) {
            api = new WebApi("http://" + ip + ":8000");
        }
    }

    private static class ConnectToApiTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            if (urls[0] != null && !urls[0].equals("")) {
                if (api == null || api.getVolume() == -1) {
                    api = new WebApi("http://" + urls[0] + ":8000");
                }
            }
            return null;
        }
    }

    private static class ConnectToWebSocketTask extends AsyncTask<MainActivity, Void, Void> {
        @Override
        protected Void doInBackground(MainActivity... callBackContexts) {
            if(ws == null || !ws.isOpen()) {
                try {
                    String ip = defaultSharedPreferences.getString("ip", "");
                    ws = new WebSocket(new URI("ws://" + ip + ":8000" + "/ws"));
                } catch (URISyntaxException e) {
                    Log.e("ConnectToWebSocketTask", "Connection to WebSocket failed.", e);
                    ws = null;
                }
                if (ws != null) {
                    ws.connect();
                    ws.registerVolumeCallback(callBackContexts[0]);
                    ws.registerTitleCallback(callBackContexts[0]);
                    ws.registerNowPlayingCallback(callBackContexts[0]);
                }
            }
            return null;
        }
    }
}
