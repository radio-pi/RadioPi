package name.l33t.radiopi.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

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