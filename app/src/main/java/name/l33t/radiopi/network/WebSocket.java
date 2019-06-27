package name.l33t.radiopi.network;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;


public class WebSocket extends WebSocketClient implements Callback.NowPlayingImplementation, Callback.TitleImplementation, Callback.VolumeImplementation {

    private Callback.NowPlaying nowPlayingCallback;
    private Callback.Title titleCallback;
    private Callback.Volume volumeCallback;

    public WebSocket(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Log.d("onOpen", "something went open");
        this.send("volume");
    }

    @Override
    public void onMessage(String msg) {
        Log.d("onMessage", "got a new message: " + msg);
        JSONObject jmsg = null;
        try {
            jmsg = new JSONObject(msg);


            Integer volume = jmsg.optInt("volume", -1337);
            if(null != volumeCallback && -1337 != volume){
                volumeCallback.updateVolume(volume);
            }

            String title = jmsg.optString("title");
            if(null != titleCallback && "" != title){
                titleCallback.updateTitle(title);
            }

            String streamkey = jmsg.optString("stream_key");
            if(null != nowPlayingCallback && "" != streamkey){
                nowPlayingCallback.updateNowPlaying(streamkey);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        Log.d("onClose", "something went closed " + s);
    }

    @Override
    public void onError(Exception e) {
        Log.d("onError", "something went wrong", e);
    }

    @Override
    public void registerNowPlayingCallback(Callback.NowPlaying callbackClass) {
        this.nowPlayingCallback = callbackClass;
    }

    @Override
    public void registerTitleCallback(Callback.Title callbackClass) {
        this.titleCallback = callbackClass;
    }

    @Override
    public void registerVolumeCallback(Callback.Volume callbackClass) {
        this.volumeCallback = callbackClass;
    }
}
