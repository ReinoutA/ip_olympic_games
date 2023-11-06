package factories;

import instances.Location;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class LocationFactory {
    public List<Location> createLocationsFromJSON(String json) throws IOException {
        List<Location> locations = new ArrayList<>();

        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));
        JSONObject jsonData = new JSONObject(jsonContent);

        JSONArray locationsArray = jsonData.getJSONArray("locations");

        for (int i = 0; i < locationsArray.length(); i++) {
            JSONObject locationData = locationsArray.getJSONObject(i);

            String id = locationData.getString("id");
            double lat = locationData.getDouble("lat");
            double lon = locationData.getDouble("lon");

            Location location = new Location(id, lat, lon);
            locations.add(location);
        }

        return locations;
    }
}
