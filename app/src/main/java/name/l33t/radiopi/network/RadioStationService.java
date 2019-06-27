package name.l33t.radiopi.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RadioStationService {
    @GET("/streamurls")
    Call<List<RadioStationModel>> getRadioStations();
}
