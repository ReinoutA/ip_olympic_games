package factories;

import instances.Task;
import instances.SkillRequirement;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TaskFactory {
    public List<Task> createTasksFromJSON(String json) throws IOException {
        List<Task> tasks = new ArrayList<>();

        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));
        JSONObject jsonData = new JSONObject(jsonContent);

        JSONArray tasksArray = jsonData.getJSONArray("tasks");

        for (int i = 0; i < tasksArray.length(); i++) {
            JSONObject taskData = tasksArray.getJSONObject(i);

            String taskId = taskData.getString("id");
            String locationId = taskData.getString("locationId");
            int demand = taskData.getInt("demand");
            int days = taskData.getInt("days");
            String taskTypeId = taskData.getString("taskTypeId");

            JSONArray skillRequirementsArray = taskData.getJSONArray("skillRequirements");
            List<SkillRequirement> skillRequirements = new ArrayList<>();

            for (int j = 0; j < skillRequirementsArray.length(); j++) {
                JSONObject skillData = skillRequirementsArray.getJSONObject(j);

                String skillId = skillData.getString("skillId");
                int minProficiency = skillData.getInt("minProficiency");
                boolean isHard = skillData.getBoolean("isHard");
                double proportion = skillData.getDouble("proportion");
                double weight = skillData.getDouble("weight");

                SkillRequirement skillRequirement = new SkillRequirement(skillId, minProficiency, isHard, proportion, weight);
                skillRequirements.add(skillRequirement);
            }

            Task task = new Task(taskId, locationId, demand, days, taskTypeId, skillRequirements);
            tasks.add(task);
        }

        return tasks;
    }
}
