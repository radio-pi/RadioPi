package name.l33t.radiopi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DataAccess extends SQLiteOpenHelper {

    private SQLiteDatabase GetDB()
    {
        SQLiteDatabase db;
        db = getWritableDatabase();
        return db;
    }

    // Name und Version der Datenbank
    private static final String DATABASE_NAME = "radio.db";
    private static final int DATABASE_VERSION = 5;

    //Database Variables Tracking
    private static final String TABLE_NAME = "StationList";

    private static final String TABLE_TRACKING_ENTITY_CREATE = "CREATE TABLE " + TABLE_NAME +
            " (" +
            RadioStationItem._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            RadioStationItem.STATION_NAME      + " STRING," +
            RadioStationItem.STATION_URL       + " STRING," +
            RadioStationItem.STATION_ORDERID   + " INTEGER" +
            ");";

    private static final String TABLE_TRACKING_ENTITY_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;


    DataAccess(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_TRACKING_ENTITY_CREATE);
        insert("Hardbase", "http://listen.hardbase.fm/tunein-mp3-pls", 1, db);
        insert("Technobase", "http://listen.technobase.fm/tunein-mp3-asx", 2, db);
        insert("Radio 24", "http://icecast.radio24.ch/radio24", 10, db);
        insert("Radio SRF 1", "http://stream.srg-ssr.ch/m/drs1/mp3_128", 3, db);
        insert("Radio SRF 2", "http://stream.srg-ssr.ch/m/drs2/mp3_128", 4, db);
        insert("Radio SRF 3", "http://stream.srg-ssr.ch/m/drs3/mp3_128", 5, db);
        insert("Radio Swiss Jazz", "http://stream.srg-ssr.ch/m/rsj/mp3_128", 6, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO: write a proper upgrade path
        db.execSQL(TABLE_TRACKING_ENTITY_DROP);
        onCreate(db);
    }
    public long insert(String name, String url, Integer orderId)
    {
        return insert(name, url, orderId, GetDB());
    }
    public long insert(String name, String url, Integer orderId, SQLiteDatabase db) {
        long rowId = -1;
        try {
            // die zu speichernden Werte
            ContentValues values = new ContentValues();
            values.put(RadioStationItem.STATION_NAME, name);
            values.put(RadioStationItem.STATION_URL, url);
            values.put(RadioStationItem.STATION_ORDERID, orderId);

            // in die Tabelle  einfuegen
            rowId = db.insert(TABLE_NAME, null, values);
        } catch (SQLiteException e) {
            //do something, something smart perhaps
        }
        return rowId;
    }

    public Cursor query() {
        return query(GetDB());
    }

    public Cursor query(SQLiteDatabase db) {
        //query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
        return db.query(TABLE_NAME,
                new String[] {RadioStationItem._ID, RadioStationItem.STATION_NAME, RadioStationItem.STATION_URL, RadioStationItem.STATION_ORDERID},
                null, null, null, null, RadioStationItem.STATION_ORDERID + " DESC");
    }

    public String getUrl(Integer id) {
        return getUrl(id, GetDB());
    }

    public String getUrl(Integer id, SQLiteDatabase db) {
        String url = null;
        GetDB();
        //query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
        Cursor cursor =  db.query(TABLE_NAME, new String[] {RadioStationItem.STATION_URL},  RadioStationItem._ID + "=?", new String[] { id.toString() }, null, null, null);

        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            url = cursor.getString(0);
        }
        return url;
    }


    public String getStationName(Integer id) {
        return getStationName(id, GetDB());
    }

    public String getStationName(Integer id, SQLiteDatabase db) {
        String stationname = null;
        GetDB();
        //query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
        Cursor cursor =  db.query(TABLE_NAME, new String[] {RadioStationItem.STATION_NAME},  RadioStationItem._ID + "=?", new String[] { id.toString() }, null, null, null);

        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            stationname = cursor.getString(0);
        }
        return stationname;
    }
}
