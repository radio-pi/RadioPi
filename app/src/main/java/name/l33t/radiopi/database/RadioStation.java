package name.l33t.radiopi.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.UUID;

@Entity(tableName = "radio_station")
public class RadioStation {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "img")
    private String img;

    @ColumnInfo(name = "orderId")
    private Integer orderId;

    @Ignore
    public RadioStation(String name, String url, String img, Integer orderId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.url = url;
        this.img = img;
        this.orderId = orderId;
    }

    public RadioStation(String id, String name, String url, String img, Integer orderId) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.img = img;
        this.orderId = orderId;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public String getImg() {
        return this.img;
    }

    public Integer getOrderId() {
        return this.orderId;
    }

    public static RadioStation[] prepopulateData() {
        return new RadioStation[] {
                new RadioStation("Radio 24", "http://icecast.radio24.ch/radio24", "https://upload.wikimedia.org/wikipedia/de/thumb/3/33/Radio_24_Logo.svg/1084px-Radio_24_Logo.svg.png", 2),
                new RadioStation("SRF 3", "http://livestream.srg-ssr.ch/m/drs3/mp3_128", "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ee/Radio_SRF_3.svg/833px-Radio_SRF_3.svg.png", 1),
                new RadioStation("SRF 1", "http://livestream.srg-ssr.ch/m/drs1/mp3_128", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d3/Radio_SRF_1.svg/833px-Radio_SRF_1.svg.png", 0),
                new RadioStation("Energy", "https://energyzuerich.ice.infomaniak.ch/energyzuerich-high.mp3", "https://upload.wikimedia.org/wikipedia/commons/f/f0/Energy_Zürich_Logo.png", 3),
        };
    }
}