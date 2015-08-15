package name.l33t.radiopi;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

public class VolumeWebSocket extends WebSocketClient {
    public VolumeWebSocket(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Log.d("onOpen", "something went open");
    }

    @Override
    public void onMessage(String s) {
        Log.d("onMessage", "something went message " + s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        Log.d("onClose", "something went closed " + s);
    }

    @Override
    public void onError(Exception e) {
        Log.d("onError", "something went wrong", e);
    }
}
