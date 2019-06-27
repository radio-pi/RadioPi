package name.l33t.radiopi.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface RadioStationDao {
    @Query("SELECT * FROM radio_station")
    List<RadioStation> getAll();

    @Insert
    void insertAll(RadioStation... stations);

    @Delete
    void delete(RadioStation station);
}