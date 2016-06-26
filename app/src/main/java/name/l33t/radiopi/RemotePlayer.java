package name.l33t.radiopi;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class RemotePlayer implements Callback.VolumeImplementation, Callback.MessageImplementation {

    Callback.Volume volume_callback;
    Callback.Message message_callback;

    @Override
    public void registerVolumeCallback(Callback.Volume callbackClass) {
        volume_callback = callbackClass;
    }

    @Override
    public void registerMessageCallback(Callback.Message callbackClass) {
        message_callback = callbackClass;
    }

    //private static final String BASE_URL = "http://radio-pi.l33t.network:3000/";
    private static final String BASE_URL = "http://192.168.17.174:3000/";
    private static AsyncHttpClient client;
    private static Context static_context;

    public RemotePlayer(Context context)
    {
        client = new AsyncHttpClient();
        static_context = context;
    }

    public boolean playUrl(String url, final String stationname) {
        Log.d("playUrl", "post to " + getAbsoluteUrl("play") + "with the url: " + url);

        JSONObject params = new JSONObject();
        try {
            params.put("url", url);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        post("play", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.d("playUrl", response.toString());
                message_callback.displayToast("Playing: " + stationname);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.d("playUrl", "something went wrong", e);
                message_callback.displayToast("Something went wrong!");
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
                message_callback.displayToast("Stopped");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.d("stop", "something went wrong", e);
                message_callback.displayToast("Something went wrong!");
            }
        });
        return true;
    }

    private static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    private static void post(String url, JSONObject jsonBody, AsyncHttpResponseHandler responseHandler) {
        StringEntity entity = null;
        if (jsonBody != null) {
            try {
                entity = new StringEntity(jsonBody.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        client.post(static_context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public boolean volume(Integer seekValue) {
        JSONObject params = new JSONObject();
        try {
            params.put("volume", seekValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        post("volume", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.d("volume", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.d("volume", "something went wrong", e);
                message_callback.displayToast("Something went wrong!");
            }
        });
        return true;
    }

    public boolean getvolume() {

        get("volume", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Integer volume = 50;

                try {
                    JSONObject jRestponse = new JSONObject(new String(response));
                    volume = jRestponse.getInt("volume");
                    Log.d("volume", volume.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                volume_callback.setVolume(volume);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.d("volume", "something went wrong", e);
                message_callback.displayToast("Something went wrong!");
            }
        });
        return true;
    }
}
