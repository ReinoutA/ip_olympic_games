package factories;

import instances.*;

import org.json.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class SkillFactory {
    public List<Skill> createSkillsFromJSON(String json) throws IOException {
        List<Skill> skills = new ArrayList<>();

        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));

        JSONObject jsonData = new JSONObject(jsonContent);

        JSONArray skillsArray = jsonData.getJSONArray("skills");

        for (int i = 0; i < skillsArray.length(); i++) {
            String skillName = skillsArray.getString(i);
            Skill skill = new Skill(skillName);
            skills.add(skill);
        }

        return skills;
    }
}
