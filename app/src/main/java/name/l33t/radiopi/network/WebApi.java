package name.l33t.radiopi.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebApi {
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;
    private RadioStationService radioStationService;
    private String baseURL;

    /**
     * Create a new WebAPI Wrapper.
     * The wrapper allows easy access to the
     * API of a RadioPI backend.
     * @param baseURL of a RadioPI backend
     */
    public WebApi(String baseURL){
        Log.d("WebApi", "Creating a WebAPI with the baseUrl: "+ baseURL);
        this.client = new OkHttpClient();

        //Remove last / in url
        if(baseURL.endsWith("/")){
            baseURL = baseURL.substring(0, baseURL.length() -1);
        }
        this.baseURL = baseURL;

        // Build retrofit client
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(this.baseURL)
                .build();

        radioStationService = retrofit.create(RadioStationService.class);
    }

    /**
     * Start to play a stream.
     * If a stream is already playing this
     * will stop and replace the stream.
     * @param streamURL of the stream which gets started.
     * @return boolean that indicates if the api call was successful.
     */
    public boolean play(String streamURL){
        Log.d("WebApi", "Playing streamurl: "+ streamURL);
        String msg = String.format("{\"url\": \"%s\"}", streamURL);
        Request req = buildPostRequest("/play", msg);

        try {
            return client.newCall(req).execute().isSuccessful();
        } catch (IOException e) {
            Log.d("WebApi", "Error: playing stream", e);
        }
        return false;
    }

    /**
     * Stop all running streams.
     * Does nothing if no stream is running.
     */
    public void stop(){
        Log.d("WebApi", "Stopping stream");
        Request req = buildPostRequest("/stop", "");

        try {
            client.newCall(req).execute();
        } catch (IOException e) {
            Log.d("WebApi", "Error: stopping stream", e);
        }
    }

    /**
     * Get the current stream volume level.
     * Can return values like -200.
     * @return the volume of the currently playing stream.
     */
    public int getVolume(){
        Log.d("WebApi", "Getting Volume");
        String url = this.baseURL + "/volume";
        Request req = new Request.Builder()
                                 .url(url)
                                 .get()
                                 .build();

        try {
            Response response = client.newCall(req).execute();
            if(response.isSuccessful()){
                JSONObject json = new JSONObject(response.body().string());
                Integer volume = json.optInt("volume", 0);

                Log.d("WebApi", "Getting Volume returned: " + volume);
                return  volume;
            }
        } catch (IOException e) {
            Log.d("WebApi", "Error (IOException): stopping stream", e);
        } catch (JSONException e) {
            Log.d("WebApi", "Error (JSONException): stopping stream", e);
        }
        return -1;
    }

    /**
     * Set the volume level of the current stream.
     * Will do nothing if no stream is running.
     * @param volume of the current stream.
     */
    public void setVolume(Integer volume){
        Log.d("WebApi", "Setting Volume to: " + volume.toString());
        String msg = String.format("{\"volume\": %d}", volume);
        Request req = buildPostRequest("/volume", msg);

        try {
            client.newCall(req).execute();
        } catch (IOException e) {
            Log.d("WebApi", "Error: stopping stream", e);
        }
    }

    /**
     * Get a list with streams.
     *
     * @return a list with RadioStationModel.
     */
    public List<RadioStationModel> getRadioStationList(){
        Log.d("WebApi", "Get a list with all radio stations");
        try {
            retrofit2.Response<List<RadioStationModel>> response =
                    this.radioStationService.getRadioStations().execute();
            if (response.isSuccessful()){
                return response.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("WebApi", "Error: getting a list with all radio stations");
        }
        // On error case return a empty list
        return new ArrayList<>();
    }

    //TODO(FN): implement
    public void setSleepTimer(){
        Log.d("WebApi", "Setup a sleep timer");

    }

    //TODO(FN): implement
    public int getSleepTimer(){
        Log.d("WebApi", "Check sleep timer");
        return 0;
    }

    private Request buildPostRequest(String site, String jsonMessage){
        String url = this.baseURL + site;
        RequestBody body = RequestBody.create(JSON, jsonMessage);

        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }
}
