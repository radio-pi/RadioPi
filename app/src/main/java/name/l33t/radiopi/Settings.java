package name.l33t.radiopi;

public class Settings {
    private static Settings instance = null;
    protected Settings() {
        // Exists only to defeat instantiation.
    }
    public static Settings getInstance() {
        if(instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    private String radiopi_url = "192.168.17.174";
    public void setRadioPiUrl(String url){ url = radiopi_url; }
    public String getRadioPiUrl(){
        return radiopi_url;
    }

    private String radiopi_name = "development";
    public void setRadioPiName(String name){
        name = radiopi_url;
    }
    public String getRadioPiName(){
        return radiopi_name;
    }
}
