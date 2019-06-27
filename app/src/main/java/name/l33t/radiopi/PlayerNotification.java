package name.l33t.radiopi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PlayerNotification extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //startForeground(1333, "LeId");
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
