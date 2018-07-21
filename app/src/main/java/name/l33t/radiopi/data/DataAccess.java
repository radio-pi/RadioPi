package name.l33t.radiopi.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataAccess extends SQLiteOpenHelper {

    public DataAccess(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private SQLiteDatabase GetDB()
    {
        SQLiteDatabase db;
        db = getWritableDatabase();
        return db;
    }

    // Name und Version der Datenbank
    private static final String DATABASE_NAME = "radio.db";
    private static final int DATABASE_VERSION = 7;

    // Radio station list
    private static final String TABLE_NAME_STATIONS = "StationList";

    private static final String TABLE_RADIOSTATION_ENTITY_CREATE = "CREATE TABLE " + TABLE_NAME_STATIONS +
            " (" +
            RadioStationItem._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            RadioStationItem.STATION_NAME      + " STRING," +
            RadioStationItem.STATION_URL       + " STRING," +
            RadioStationItem.STATION_ORDERID   + " INTEGER" +
            ");";

    private static final String TABLE_RADIOSTATION_ENTITY_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME_STATIONS;

    // settings for rpi devices
    private static final String TABLE_NAME_RADIOPI_DEVICE = "RadioPiDevices";

    private static final String TABLE_RADIOPI_DEVICE_ENTITY_CREATE = "CREATE TABLE " + TABLE_NAME_RADIOPI_DEVICE +
            " (" +
            SettingRadioPiItem._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SettingRadioPiItem.SETTING_RADIOPI_NAME      + " STRING," +
            SettingRadioPiItem.SETTING_RADIOPI_URL       + " STRING" +
            ");";

    private static final String TABLE_RADIOPI_DEVICE_ENTITY_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME_RADIOPI_DEVICE;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_RADIOSTATION_ENTITY_CREATE);
        insert("Hardbase", "http://listen.hardbase.fm/tunein-mp3-pls", 1, db);
        insert("Technobase", "http://listen.technobase.fm/tunein-mp3-asx", 2, db);
        insert("Radio 24", "http://icecast.radio24.ch/radio24", 10, db);
        insert("Radio SRF 1", "http://stream.srg-ssr.ch/m/drs1/mp3_128", 3, db);
        insert("Radio SRF 2", "http://stream.srg-ssr.ch/m/drs2/mp3_128", 4, db);
        insert("Radio SRF 3", "http://stream.srg-ssr.ch/m/drs3/mp3_128", 5, db);
        insert("Radio Swiss Jazz", "http://stream.srg-ssr.ch/m/rsj/mp3_128", 6, db);

        db.execSQL(TABLE_RADIOPI_DEVICE_ENTITY_CREATE);
        insertRadioPIDevice("radio pi", "radio-pi.l33t.network",db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO: write a proper upgrade path
        db.execSQL(TABLE_RADIOSTATION_ENTITY_DROP);
        db.execSQL(TABLE_RADIOPI_DEVICE_ENTITY_DROP);
        onCreate(db);
    }

    public void clearRadioStationList(){
        SQLiteDatabase db = GetDB();
        db.execSQL(TABLE_RADIOSTATION_ENTITY_DROP);
        db.execSQL(TABLE_RADIOSTATION_ENTITY_CREATE);
        db.close();
    }

    public long insertRadioPIDevice(String name, String url, SQLiteDatabase db) {
        long rowId = -1;
        try {
            // die zu speichernden Werte
            ContentValues values = new ContentValues();
            values.put(SettingRadioPiItem.SETTING_RADIOPI_NAME, name);
            values.put(SettingRadioPiItem.SETTING_RADIOPI_URL, url);

            // in die Tabelle  einfuegen
            rowId = db.insert(TABLE_NAME_RADIOPI_DEVICE, null, values);
        } catch (SQLiteException e) {
            //do something, something smart perhaps
        }
        return rowId;
    }

    public long replaceRadioPIDevice(String name, String url, Integer orderId) {
        SQLiteDatabase db = GetDB();
        long rowId = -1;
        try {
            // die zu speichernden Werte
            ContentValues values = new ContentValues();
            values.put(RadioStationItem.STATION_NAME, name);
            values.put(RadioStationItem.STATION_URL, url);
            values.put(RadioStationItem.STATION_ORDERID, orderId);

            Cursor cursor = db.query(TABLE_NAME_STATIONS, new String[] {RadioStationItem.STATION_URL},  RadioStationItem.STATION_URL + "=?", new String[] { url }, null, null, null);
            if(cursor.getCount() == 0){
                // new entry
                rowId = db.insert(TABLE_NAME_STATIONS, null, values);
                Log.d("syncStationListDA", "create "+ url);
            }
            else {
                // update
                cursor.moveToFirst();
                db.update(TABLE_NAME_STATIONS, values, RadioStationItem._ID + "=?", new String[]{ String.valueOf(cursor.getInt(0))});
                Log.d("syncStationListDA", "update "+ url);
            }
            cursor.close();
            db.close();
        } catch (SQLiteException e) {
            //do something, something smart perhaps
        }
        return rowId;
    }

    public SettingRadioPiItem getFirstRadioPIDevice() {
        return getFirstRadioPIDevice(GetDB());
    }
    public SettingRadioPiItem getFirstRadioPIDevice(SQLiteDatabase db) {
        //query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
        Cursor cursor =  db.query(TABLE_NAME_RADIOPI_DEVICE,
                new String[] {SettingRadioPiItem._ID, SettingRadioPiItem.SETTING_RADIOPI_NAME, SettingRadioPiItem.SETTING_RADIOPI_URL},
                null, null, null, null, null);

        cursor.moveToFirst();
        SettingRadioPiItem setting = new SettingRadioPiItem(-1, "", "");
        if (!cursor.isAfterLast()) {
            setting = new SettingRadioPiItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
        }
        return setting;
    }

    public int updateRadioPIDevice(Integer _id, String name, String url) {
        return updateRadioPIDevice(_id, name, url, GetDB());
    }
    public int updateRadioPIDevice(Integer _id, String name, String url, SQLiteDatabase db) {

        ContentValues values = new ContentValues();
        values.put(SettingRadioPiItem.SETTING_RADIOPI_NAME, name);
        values.put(SettingRadioPiItem.SETTING_RADIOPI_URL, url);

        //int update(String table, ContentValues values, String whereClause, String[] whereArgs)
        return db.update(TABLE_NAME_RADIOPI_DEVICE, values, SettingRadioPiItem._ID + " = " + _id, null);
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
            rowId = db.insert(TABLE_NAME_STATIONS, null, values);
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
        return db.query(TABLE_NAME_STATIONS,
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
        Cursor cursor =  db.query(TABLE_NAME_STATIONS, new String[] {RadioStationItem.STATION_URL},  RadioStationItem._ID + "=?", new String[] { id.toString() }, null, null, null);

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
        Cursor cursor =  db.query(TABLE_NAME_STATIONS, new String[] {RadioStationItem.STATION_NAME},  RadioStationItem._ID + "=?", new String[] { id.toString() }, null, null, null);

        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            stationname = cursor.getString(0);
        }
        return stationname;
    }
}
