package factories;

import instances.Skill;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SkillFactory {
    public List<Skill> createSkillsFromJSON(String json) throws IOException {
        List<Skill> skills = new ArrayList<>();

        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));
        // Converteer de JSON-string naar een JSON-object
        JSONObject jsonData = new JSONObject(jsonContent);

        // Haal de JSON-array "skills" op
        JSONArray skillsArray = jsonData.getJSONArray("skills");

        for (int i = 0; i < skillsArray.length(); i++) {
            String skillName = skillsArray.getString(i);
            Skill skill = new Skill(skillName);
            skills.add(skill);
        }

        return skills;
    }
}
