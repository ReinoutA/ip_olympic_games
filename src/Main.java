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

        List<Volunteer> presourcedVolunteers = new ArrayList<>();
        for(Volunteer volunteer : volunteers){
            if(volunteer.isPresourced()){
                presourcedVolunteers.add(volunteer);
            }
        }





    }

}