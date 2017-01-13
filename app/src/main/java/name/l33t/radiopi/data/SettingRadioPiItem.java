package name.l33t.radiopi.data;

import android.content.Intent;

public class SettingRadioPiItem {
    public static final String _ID = "_id";
    public static final String SETTING_RADIOPI_NAME = "setting_radio_pi_name";
    public static final String SETTING_RADIOPI_URL = "setting_radio_pi_url";

    private Integer _id;
    private String name;
    private String url;

    public SettingRadioPiItem(Integer _id, String name, String url) {
        this._id = _id;
        this.name = name;
        this.url = url;
    }

    public Integer Id()
    {
        return _id;
    }

    public String Name()
    {
        return name;
    }

    public String Url()
    {
        return url;
    }
}
