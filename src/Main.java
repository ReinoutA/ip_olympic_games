import factories.*;
import gurobi.*;
import instances.*;
import instances.wrappers.TaskSkillVolunteers;

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

        // 4. Volunteer can do task based on preferencelocation, taaktypegeschiktheid, beschikbaarheid
        Map<Task, List<Volunteer>> volunteers_that_can_do_task_map = new HashMap<>();

        for(Task t : tasks){
            List<Volunteer> volunteers_that_can_do_task = new ArrayList<>();
            for(Volunteer v : volunteers){
                // locatie OKE
                if(v.getPreferredLocationIds().contains(t.getLocationId())){
                    // beschikbaarheid OKE
                    if(v.getAvailableDays() >= t.getDays()){
                        if(v.getScoreOfTaskType(t.getTaskTypeId()) != 0){
                            volunteers_that_can_do_task.add(v);
                        }
                    }
                }
            }
            volunteers_that_can_do_task_map.put(t, volunteers_that_can_do_task);
        }

        // 5. Volunteer can do task based on preferencelocation, taaktypegeschiktheid, beschikbaarheid
        Map<Volunteer, List<Task>> tasks_that_can_be_done_by_volunteer_map = new HashMap<>();

        for(Volunteer v : volunteers){
            List<Task> tasks_that_can_be_done_by_volunteer = new ArrayList<>();
            for(Task t : tasks){
                if(v.getPreferredLocationIds().contains(t.getLocationId())){
                    if(v.getAvailableDays() >= t.getDays()){
                        if(v.getScoreOfTaskType(t.getTaskTypeId()) != 0){
                            tasks_that_can_be_done_by_volunteer.add(t);
                        }
                    }
                }
            }
            tasks_that_can_be_done_by_volunteer_map.put(v, tasks_that_can_be_done_by_volunteer);
        }

        // 6. and 7. Deelverzameling van vrijwilligers die (niet) voldoen aan het minimum bekwaamheidsniveau voor vaardigheid s van taak t
        List<TaskSkillVolunteers> volunteers_die_voldoen = new ArrayList<>();
        List<TaskSkillVolunteers> volunteers_die_niet_voldoen = new ArrayList<>();

   







        // 9. and 10.
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



