package factories;

import instances.*;
import org.json.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;


public class TaskTypeFactory {
    public List<TaskType> createTaskTypesFromJSON(String json) throws IOException {
        List<TaskType> taskTypes = new ArrayList<>();

        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));
        JSONObject jsonData = new JSONObject(jsonContent);

        JSONArray taskTypesArray = jsonData.getJSONArray("taskTypes");

        for (int i = 0; i < taskTypesArray.length(); i++) {
            String taskTypeName = taskTypesArray.getString(i);
            TaskType taskType = new TaskType(taskTypeName);
            taskTypes.add(taskType);
        }

        return taskTypes;
    }
}
