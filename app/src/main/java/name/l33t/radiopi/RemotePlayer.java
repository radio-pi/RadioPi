package name.l33t.radiopi;

import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class RemotePlayer {

    /*Setup for callback*/
    interface RemotePlayerCallbackClass{
        void remotePlayerCallback(String message);
    }

    RemotePlayerCallbackClass callback;

    void registerCallback(RemotePlayerCallbackClass callbackClass){
        callback = callbackClass;
    }

    private static final String BASE_URL = "http://radio-pi.l33t.lan:3000/";
    private static AsyncHttpClient client;

    public RemotePlayer()
    {
        client = new AsyncHttpClient();
    }

    public boolean playUrl(String url, final String stationname) throws UnsupportedEncodingException {
        Log.d("playUrl", "post to " + getAbsoluteUrl("play") + "with the url: " + url);

        RequestParams params = new RequestParams();
        params.add("url", url);

        post("play", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.d("playUrl", response.toString());
                callback.remotePlayerCallback("Playing: " + stationname);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.d("playUrl", "something went wrong", e);
                callback.remotePlayerCallback("Something went wrong!");
            }
        });
        return true;
    }

    public boolean stop()
    {
        Log.d("stop", "post to " + getAbsoluteUrl("stop"));
        post("stop", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.d("stop", response.toString());
                callback.remotePlayerCallback("Stopped");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.d("stop", "something went wrong", e);
                callback.remotePlayerCallback("Something went wrong!");
            }
        });
        return true;
    }

    private static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    private static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Content-Type","application/json");
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public boolean volume(Integer seekValue) {
        RequestParams params = new RequestParams();
        params.add("volume", String.valueOf(seekValue));

        post("volume", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.d("volume", response.toString());
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.d("volume", "something went wrong", e);
                callback.remotePlayerCallback("Something went wrong!");
            }
        });
        return true;
    }
}
