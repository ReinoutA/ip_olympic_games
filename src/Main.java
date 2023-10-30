import factories.*;
import gurobi.*;
import instances.*;

import java.io.IOException;
import java.util.*;

public class Main {
    public Main() throws GRBException {
    }

    public static void main(String[] args) throws IOException {

        final String PATH = "IP_Olympic_Games/resources/toy_problem.json";

        LocationFactory locationFactory = new LocationFactory();
        SkillFactory skillFactory = new SkillFactory();
        WeightFactory weightFactory = new WeightFactory();
        TaskTypeFactory taskTypeFactory = new TaskTypeFactory();
        TaskFactory taskFactory = new TaskFactory();
        VolunteerFactory volunteerFactory = new VolunteerFactory();

        List<Location> locations = locationFactory.createLocationsFromJSON(PATH);
        List<Skill> skills = skillFactory.createSkillsFromJSON(PATH);
        List<Weight> weights = weightFactory.createWeightsFromJSON(PATH);
        List<TaskType> taskTypes = taskTypeFactory.createTaskTypesFromJSON(PATH);
        List<Task> tasks = taskFactory.createTasksFromJSON(PATH);
        List<Volunteer> volunteers = volunteerFactory.createVolunteersFromJSON(PATH);

        // 1. Presourced
        List<Volunteer> presourcedVolunteers = new ArrayList<>();
        for(Volunteer volunteer : volunteers){
            if(volunteer.isPresourced()){
                presourcedVolunteers.add(volunteer);
            }
        }

        // 2. and 3. Male-Female
        List<Volunteer> maleVolunteers = new ArrayList<>();
        List<Volunteer> femaleVolunteers = new ArrayList<>();
        for(Volunteer volunteer : volunteers){
            if(volunteer.isMale()){
                maleVolunteers.add(volunteer);
            } else {
                femaleVolunteers.add(volunteer);
            }
        }


        // 8.
        Map<Task, List<SkillRequirement>> task_skills_is_hard_map = new HashMap<>();
        Map<Task, List<SkillRequirement>> task_skills_is_soft_map = new HashMap<>();

        for(Task t : tasks){
            List<SkillRequirement> task_skills_is_soft = new ArrayList<>();
            List<SkillRequirement> task_skills_is_hard = new ArrayList<>();
            for(SkillRequirement s : t.getSkillRequirements()){
                if(s.isHard()){
                    task_skills_is_hard.add(s);
                }else{
                    task_skills_is_soft.add(s);
                }
            }
            task_skills_is_hard_map.put(t, task_skills_is_hard);
            task_skills_is_soft_map.put(t, task_skills_is_soft);
        }
    }

}



