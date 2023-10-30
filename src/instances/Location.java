package instances;

public class Location {
    private String id;
    private double lat;
    private double lon;

    public Location(String id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    public String getId(){
        return id;
    }

    public double getLat(){
        return lat;
    }

    public double getLon(){
        return lon;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setLat(double lat){
        this.lat = lat;
    }
}


