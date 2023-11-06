package factories;

import instances.*;
import org.json.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WeightFactory {
    public List<Weight> createWeightsFromJSON(String json) throws IOException {
        List<Weight> weights = new ArrayList<>();

        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));

        JSONObject jsonData = new JSONObject(jsonContent);

        JSONObject weightsObject = jsonData.getJSONObject("weights");

        for (String weightName : weightsObject.keySet()) {
            double weightValue = weightsObject.getDouble(weightName);
            Weight weight = new Weight(weightName, weightValue);
            weights.add(weight);
        }

        return weights;
    }
}
