package factories;

import instances.*;
import org.json.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class VolunteerFactory {

    public static List<Volunteer> createVolunteersFromJSON(String json) throws IOException {

        List<Volunteer> volunteers = new ArrayList<>();
        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));

        JSONObject jsonData = new JSONObject(jsonContent);
        JSONArray volunteersArray = jsonData.getJSONArray("volunteers");

        for (int i = 0; i < volunteersArray.length(); i++) {
            JSONObject volunteerObject = volunteersArray.getJSONObject(i);
            String id = volunteerObject.getString("id");
            boolean isMale = volunteerObject.getBoolean("isMale");
            boolean isPresourced = volunteerObject.getBoolean("isPresourced");
            String locationId = volunteerObject.getString("locationId");
            JSONArray preferredLocationIdsArray = volunteerObject.getJSONArray("preferredLocationIds");
            List<String> preferredLocationIds = new ArrayList<>();
            for (int j = 0; j < preferredLocationIdsArray.length(); j++) {
                preferredLocationIds.add(preferredLocationIdsArray.getString(j));
            }
            int availableDays = volunteerObject.getInt("availableDays");

            JSONObject skillsObject = volunteerObject.getJSONObject("skills");
            List<Skill> skills = new ArrayList<>();
            for (String skillId : skillsObject.keySet()) {
                int proficiency = skillsObject.getInt(skillId);
                skills.add(new Skill(skillId, proficiency));
            }

            JSONObject taskTypesObject = volunteerObject.getJSONObject("taskTypes");
            List<TaskType> taskTypes = new ArrayList<>();
            for (String taskTypeId : taskTypesObject.keySet()) {
                int weight = taskTypesObject.getInt(taskTypeId);
                taskTypes.add(new TaskType(taskTypeId, weight));
            }

            volunteers.add(new Volunteer(id, isMale, isPresourced, locationId, preferredLocationIds, availableDays,
                    skills, taskTypes));
        }

        return volunteers;
    }

}
