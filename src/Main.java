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

        // 1. Presourced deelverzameling
        List<Volunteer> presourcedVolunteers = new ArrayList<>();
        for (Volunteer volunteer : volunteers) {
            if (volunteer.isPresourced()) {
                presourcedVolunteers.add(volunteer);
            }
        }

        // 2. en 3. Male-Female deelverzamelingen
        List<Volunteer> maleVolunteers = new ArrayList<>();
        List<Volunteer> femaleVolunteers = new ArrayList<>();
        for (Volunteer volunteer : volunteers) {
            if (volunteer.isMale()) {
                maleVolunteers.add(volunteer);
            } else {
                femaleVolunteers.add(volunteer);
            }
        }

        // 4. Deelverzameing van vrijwilligers die toegewezen kunnen worden aan taak t op basis van voorkeurslocaties, beschikbaarheid en taaktypegeschiktheid
        for (Task t : tasks) {
            t.createCanBeDoneByVolunteersList(volunteers);
        }

        // 5. Deelverzameling van taken waaraan vrijwilliger v kan toegewezen worden op basis van voorkeurslocaties, beschikbaarheid en taaktypegeschiktheid
        for (Volunteer v : volunteers) {
            v.addCanDoTasks(tasks);
        }

        // 6. en 7 Deelverzameling van vrijwilligers die wel/niet voldoen aan het minimumbekwaakheidsniveau voor skill s van taak t
        for(Task t : tasks){
            t.createVolunteersThatFullFillMinimumProficiencyForSkillRequirement(volunteers);
        }

        // 8. en 9. Deelverzameling van vaardigheden waar taak t een harde, zachte eis voor heeft
        for (Task t : tasks) {
            t.createSkillRequirementsSoftHardConstraintsLists();
        }
    }



}



