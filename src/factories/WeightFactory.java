package factories;

import instances.Weight;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WeightFactory {
    public List<Weight> createWeightsFromJSON(String json) throws IOException {
        List<Weight> weights = new ArrayList<>();

        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));
        // Converteer de JSON-string naar een JSON-object
        JSONObject jsonData = new JSONObject(jsonContent);

        // Haal de JSON-object "weights" op
        JSONObject weightsObject = jsonData.getJSONObject("weights");

        // Itereer door de sleutels (de gewichtsnamen) in het JSON-object
        for (String weightName : weightsObject.keySet()) {
            double weightValue = weightsObject.getDouble(weightName);
            Weight weight = new Weight(weightName, weightValue);
            weights.add(weight);
        }

        return weights;
    }
}
