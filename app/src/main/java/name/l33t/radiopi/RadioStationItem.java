package name.l33t.radiopi;

public class RadioStationItem {
    public static final String _ID = "_id";
    public static final String STATION_NAME = "name";
    public static final String STATION_URL = "url";
    public static final String STATION_ORDERID = "orderid";

    private Integer _id;
    private String name;
    private String url;
    private Integer orderId;

    public RadioStationItem(Integer Id, String name, String url, Integer orderId)
    {
        this._id = Id;
        this.name = name;
        this.url = url;
        this.orderId = orderId;
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
