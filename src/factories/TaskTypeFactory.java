package factories;

import instances.TaskType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TaskTypeFactory {
    public List<TaskType> createTaskTypesFromJSON(String json) throws IOException {
        List<TaskType> taskTypes = new ArrayList<>();

        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));
        // Converteer de JSON-string naar een JSON-object
        JSONObject jsonData = new JSONObject(jsonContent);

        // Haal de JSON-array "taskTypes" op
        JSONArray taskTypesArray = jsonData.getJSONArray("taskTypes");

        for (int i = 0; i < taskTypesArray.length(); i++) {
            String taskTypeName = taskTypesArray.getString(i);
            TaskType taskType = new TaskType(taskTypeName);
            taskTypes.add(taskType);
        }

        return taskTypes;
    }
}
