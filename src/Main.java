import factories.*;
import gurobi.*;
import instances.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    public Main() throws GRBException {
    }

    public static void main(String[] args) throws IOException, GRBException {

        final String PATH = "IP_Olympic_Games/resources/toy_problem2.json";
        //final String PATH = "IP_Olympic_Games/resources/i0_200t_5000v.json";
        // final String PATH = "IP_Olympic_Games/resources/dummy.json";
        LocationFactory locationFactory = new LocationFactory();
        SkillFactory skillFactory = new SkillFactory();
        WeightFactory weightFactory = new WeightFactory();
        TaskTypeFactory taskTypeFactory = new TaskTypeFactory();
        TaskFactory taskFactory = new TaskFactory();
        VolunteerFactory volunteerFactory = new VolunteerFactory();
        Haversine haversine = new Haversine();

        List<Location> locations = locationFactory.createLocationsFromJSON(PATH);
        List<Skill> skills = skillFactory.createSkillsFromJSON(PATH);
        List<Weight> weights = weightFactory.createWeightsFromJSON(PATH);
        List<TaskType> taskTypes = taskTypeFactory.createTaskTypesFromJSON(PATH);
        List<Task> tasks = taskFactory.createTasksFromJSON(PATH);
        List<Volunteer> volunteers = volunteerFactory.createVolunteersFromJSON(PATH);

        double w_dist = 0;
        double w_gend = 0;
        double w_type = 0;
        for (Weight w : weights) {
            if (w.getName().equals("travelDistanceWeight")) {
                w_dist = w.getWeight();
            } else if (w.getName().equals("genderBalanceWeight")) {
                w_gend = w.getWeight();
            } else if (w.getName().equals("taskTypeAdequacyWeight")) {
                w_type = w.getWeight();
            }
        }


        // 1. Presourced deelverzameling
        List<Volunteer> presourcedVolunteers = new ArrayList<>();
        for (Volunteer volunteer : volunteers) {
            if (volunteer.isPresourced()) {
                presourcedVolunteers.add(volunteer);
            }
        }

        System.out.println("1. Aantal presourced volunteers: " + presourcedVolunteers.size());

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

        System.out.println("2. Aantal mannen: " + maleVolunteers.size());
        System.out.println("3. Aantal vrouwen: " + femaleVolunteers.size());

        // 4. Deelverzameing van vrijwilligers die toegewezen kunnen worden aan taak t
        // op basis van voorkeurslocaties, beschikbaarheid en taaktypegeschiktheid
        for (Task t : tasks) {
            t.createCanBeDoneByVolunteersList(volunteers);
        }

        // 5. Deelverzameling van taken waaraan vrijwilliger v kan toegewezen worden op
        // basis van voorkeurslocaties, beschikbaarheid en taaktypegeschiktheid
        for (Volunteer v : volunteers) {
            v.addCanDoTasks(tasks);
        }

        // 6. en 7 Deelverzameling van vrijwilligers die wel/niet voldoen aan het
        // minimumbekwaakheidsniveau voor skill s van taak t
        for (Task t : tasks) {
            t.createVolunteersThatFullFillMinimumProficiencyForSkillRequirement(volunteers);
        }

        // 8. en 9. Deelverzameling van vaardigheden waar taak t een harde, zachte eis
        // voor heeft
        for (Task t : tasks) {
            t.createSkillRequirementsSoftHardConstraintsLists();
        }

        GRBEnv env = new GRBEnv("gurobi.log");
        env.set(GRB.IntParam.LogToConsole, 1);
        env.set(GRB.IntParam.OutputFlag, 1);
        env.start();
        GRBModel model = new GRBModel(env);
        model.set(GRB.IntAttr.ModelSense, GRB.MAXIMIZE);

        // Definieer de beslissingsvariabelen x_vt
        GRBVar[][] x_vt = new GRBVar[volunteers.size()][tasks.size()];
        for (int v = 0; v < volunteers.size(); v++) {
            for (int t = 0; t < tasks.size(); t++) {
                x_vt[v][t] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_" + v + "_" + t);
            }
        }

        // Definieer de beslissingsvariabele y
        GRBVar y = model.addVar(0, volunteers.size(), 0.0, GRB.INTEGER, "y");


        // Constraint 1
        for (int v = 0; v < volunteers.size(); v++) {
            GRBLinExpr assignmentConstraint = new GRBLinExpr();
            for (int t = 0; t < tasks.size(); t++) {
                assignmentConstraint.addTerm(1.0, x_vt[v][t]);
            }
            model.addConstr(assignmentConstraint, GRB.LESS_EQUAL, 1.0, "AssignmentConstraint_" + v);
        }


        // Constraint 2
        for (int v = 0; v < volunteers.size(); v++) {
            GRBLinExpr constraint = new GRBLinExpr();
            for (int t = 0; t < tasks.size(); t++) {
                if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))
                        && presourcedVolunteers.contains(volunteers.get(v))) {
                    // Voeg alleen een term toe als de voorwaarden zijn voldaan
                    constraint.addTerm(1.0, x_vt[v][t]);
                }
            }
            if (constraint.size() > 0) {
                // Voeg de constraint alleen toe als er daadwerkelijk presourced vrijwilligers
                // zijn
                // anders creëer je altijd infeasibility
                model.addConstr(constraint, GRB.EQUAL, 1.0, "Constraint2_" + v);
            }
        }


        // Constraint 3
        for (int t = 0; t < tasks.size(); t++) {
            GRBLinExpr constraint = new GRBLinExpr();
            for (int v = 0; v < volunteers.size(); v++) {
                //if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))) {
                constraint.addTerm(1.0, x_vt[v][t]);
                //}
            }
            model.addConstr(constraint, GRB.LESS_EQUAL, tasks.get(t).getDemand(), "Constraint3_" + t);
        }


        // Constraint 4
        for (int t = 0; t < tasks.size(); t++) {
            for (SkillRequirement skillRequirement : tasks.get(t).getSkillrequirementsWithHardConstraints()) {
                String skillId = skillRequirement.getSkillid();
                GRBLinExpr exprLeft = new GRBLinExpr();
                GRBLinExpr exprRight = new GRBLinExpr();
                double fraction = tasks.get(t).getSkillRequirement(skillId).getProportion();
                for (int v = 0; v < volunteers.size(); v++) {
                    if (tasks.get(t).getVolunteersThatFullFillMinimumProficiencyForSkillRequirement().containsKey(skillRequirement)) {
                        List<Volunteer> volunteersThatFullFillMinProf = tasks.get(t).getVolunteersThatFullFillMinimumProficiencyForSkillRequirement().get(skillRequirement);
                        if (volunteersThatFullFillMinProf.contains(volunteers.get(v))) {
                            exprLeft.addTerm(1.0, x_vt[v][t]);
                        }
                    }
                }
                for (int v = 0; v < volunteers.size(); v++) {
                    if (volunteers.get(v).getCanDoTasks().contains(tasks.get(t))) {
                        exprRight.addTerm(fraction, x_vt[v][t]);
                    }
                }
                model.addConstr(exprLeft, GRB.GREATER_EQUAL, exprRight, "Constraint4_" + t);
            }
        }

        // Constraint 5
        for (int t = 0; t < tasks.size(); t++) {
            GRBLinExpr exprLeft = new GRBLinExpr();
            GRBLinExpr exprRight = new GRBLinExpr();

            for (int v = 0; v < volunteers.size(); v++) {
                if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))) {
                    exprLeft.addTerm(0.45, x_vt[v][t]);
                }
            }

            for (int v = 0; v < volunteers.size(); v++) {
                if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))
                        && maleVolunteers.contains(volunteers.get(v))) {
                    exprRight.addTerm(1.0, x_vt[v][t]);
                }
            }
            model.addConstr(exprLeft, GRB.LESS_EQUAL, exprRight, "Constraint5_" + t);
        }


        // Constraint 6
        for (int t = 0; t < tasks.size(); t++) {
            GRBLinExpr exprLeft = new GRBLinExpr();
            GRBLinExpr exprRight = new GRBLinExpr();

            for (int v = 0; v < volunteers.size(); v++) {
                if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))) {
                    exprLeft.addTerm(0.55, x_vt[v][t]);
                }
            }

            for (int v = 0; v < volunteers.size(); v++) {
                if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))
                        && maleVolunteers.contains(volunteers.get(v))) {
                    exprRight.addTerm(1.0, x_vt[v][t]);
                }
            }
            model.addConstr(exprLeft, GRB.GREATER_EQUAL, exprRight, "Constraint6_" + t);
        }


        // Constraint 7
        for (int t = 0; t < tasks.size(); t++) {

            GRBLinExpr exprRight1 = new GRBLinExpr();
            GRBLinExpr exprRight2 = new GRBLinExpr();

            for (int v = 0; v < volunteers.size(); v++) {
                if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))
                        && femaleVolunteers.contains(volunteers.get(v))) {
                    exprRight1.addTerm(1.0, x_vt[v][t]);
                }
            }

            for (int v = 0; v < volunteers.size(); v++) {
                if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))
                        && maleVolunteers.contains(volunteers.get(v))) {
                    exprRight2.addTerm(1.0, x_vt[v][t]);
                }
            }
            // Maak een nieuwe variabele voor het verschil
            GRBVar differenceVar = model.addVar(-GRB.INFINITY, GRB.INFINITY, 0.0, GRB.INTEGER, "differenceVar");

            // Voeg een constraint toe voor het verschil
            model.addConstr(differenceVar, GRB.GREATER_EQUAL, exprRight1, "DifferenceConstraintRight1_" + t);
            model.addConstr(differenceVar, GRB.GREATER_EQUAL, exprRight2, "DifferenceConstraintRight2_" + t);

            model.addGenConstrAbs(y, differenceVar, "Constraint7_" + t);
        }

        // Constraint EXTRA

        // Constraint om ervoor te zorgen dat x_vt 0 is als een vrijwilliger niet
        // beschikbaar, taaktypegeschiktheid 0 heeft, niet die locatie als voorkeurslocatie heeft..

        for (int v = 0; v < volunteers.size(); v++) {
            for (int t = 0; t < tasks.size(); t++) {
                Task task = tasks.get(t);
                Volunteer volunteer = volunteers.get(v);
                if (!task.getCanBeDoneByVolunteers().contains(volunteer)) {
                    model.addConstr(x_vt[v][t], GRB.EQUAL, 0, "CONSTR_EXTR");
                }
            }
        }

        // Doelfunctie 1
        GRBLinExpr objectiveFunction1 = new GRBLinExpr();
        for (int v = 0; v < volunteers.size(); v++) {
            for (int t = 0; t < tasks.size(); t++) {
                if (volunteers.get(v).getCanDoTasks().contains(tasks.get(t))) {
                    objectiveFunction1.addTerm(1.0, x_vt[v][t]);
                }
            }
        }

        // Doelfunctie 2
        GRBLinExpr objectiveFunction2 = new GRBLinExpr();
        GRBLinExpr expr1 = new GRBLinExpr();
        GRBLinExpr expr2 = new GRBLinExpr();
        GRBLinExpr expr3 = new GRBLinExpr();


        // Expr 1
        for (int v = 0; v < volunteers.size(); v++) {
            for (int t = 0; t < tasks.size(); t++) {
                Task task = tasks.get(t);
                Volunteer volunteer = volunteers.get(v);

                Location taskLocation = null;
                Location volunteerLocation = null;
                for (Location l : locations) {
                    if (l.getId().equals(task.getLocationId())) {
                        taskLocation = l;
                    }
                }
                for (Location l : locations) {
                    if (l.getId().equals(volunteer.getLocationId())) {
                        volunteerLocation = l;
                    }
                }

               if (task.getCanBeDoneByVolunteers().contains(volunteer)) {
                    int f_vt = haversine.calculateDistance(volunteerLocation.getLon(), volunteerLocation.getLat(), taskLocation.getLon(), taskLocation.getLat());
                    String taskTypeId = task.getTaskTypeId();
                    int q_vnt = volunteer.getScoreOfTaskType(taskTypeId);
                    expr1.addTerm(w_dist * f_vt, x_vt[v][t]);
                    expr1.addTerm(-w_type * q_vnt, x_vt[v][t]);
               }
            }
        }

        // Expr 2

        for (int t = 0; t < tasks.size(); t++) {
            for (int v = 0; v < volunteers.size(); v++) {
                Task task = tasks.get(t);
                Volunteer volunteer = volunteers.get(v);
                if (task.getCanBeDoneByVolunteers().contains(volunteer)) {
                    for (SkillRequirement skillRequirement : task.getSkillrequirementsWithSoftConstraints()) {
                        Map<SkillRequirement, List<Volunteer>> m = task.getVolunteersThatDontFullFillMinimumProficiencyForSkillRequirement();
                        List<Volunteer> volunteerList = m.get(skillRequirement);
                        if (volunteerList.contains(volunteer)) {
                            System.out.println("Added to expr2");
                            // TODO: In de file zijn deze gewichten negatief dus ga na of dat zo hoort
                            expr2.addTerm(skillRequirement.getWeight(), x_vt[v][t]);
                        }
                    }

                }
            }
        }


        // Expr 3
        expr3.addTerm(w_gend, y);

        objectiveFunction2.add(expr1);
        objectiveFunction2.add(expr2);
        objectiveFunction2.add(expr3);

        // TODO GA NA OF INDEX EN PRIORITY ARGUMENT JUIST ZIJN
        // Doelfunctie 1 (maximaliseren met prioriteit 1)
        model.setObjectiveN(objectiveFunction1, 0, 0, 1.0, 0, 0, "Objective1");

        // Doelfunctie 2 (minimaliseren met prioriteit 2)
        model.setObjectiveN(objectiveFunction2, 1, 1, -1.0, 0, 0, "Objective2");

        model.update();
        model.optimize();


        System.out.println("Aantal Constraints: " + List.of(model.getConstrs()).size());
        // Controleer of het model infeasible is
        if (model.get(GRB.IntAttr.Status) == GRB.INFEASIBLE) {
            // Bereken de IIS (Infeasible Inequalities and Subsystems)
            model.computeIIS();

            // Haal de lijst van incompatibele beperkingen op
            List<GRBConstr> iisConstraints = List.of(model.getConstrs());

            // Print de lijst van incompatibele beperkingen
            System.out.println("Incompatibele beperkingen (IIS):");
            for (GRBConstr constr : iisConstraints) {
                System.out.println(constr.get(GRB.StringAttr.ConstrName));
            }

            // Genereer het IIS-rapportbestand
            model.write("iisreport.ilp");
        } else {
            System.out.println("Model is feasible.");

            for (int v = 0; v < volunteers.size(); v++) {
                for (int t = 0; t < tasks.size(); t++) {
                    double val = x_vt[v][t].get(GRB.DoubleAttr.X);
                    Volunteer volunteer = volunteers.get(v);
                    Task task = tasks.get(t);
                    if (val == 1)
                        System.out.println("Vrijwilliger " + volunteer.getId() + " is toegewezen aan task " + task.getId());
                }
            }
        }

        // IO

        // Voeg eerst de velden in de juiste volgorde toe aan het resultaat JSON-object
        JSONObject resultJSON = new JSONObject();
        resultJSON.put("assignedVolunteers", model.getObjective(0).getValue());
        resultJSON.put("assignmentCost", model.getObjective(1).getValue());

        JSONArray assignmentsArray = new JSONArray();

        for (int v = 0; v < volunteers.size(); v++) {
            for (int t = 0; t < tasks.size(); t++) {
                if (x_vt[v][t].get(GRB.DoubleAttr.X) > 0.5) {
                    // Voeg de toewijzing toe aan de JSON-array
                    JSONObject assignmentJSON = new JSONObject();
                    assignmentJSON.put("volunteerId", volunteers.get(v).getId());
                    assignmentJSON.put("taskId", tasks.get(t).getId());
                    assignmentsArray.put(assignmentJSON);
                }
            }
        }

        resultJSON.put("assignments", assignmentsArray);
        // Converteer het JSON-object naar een JSON-tekst
        String resultJSONString = resultJSON.toString();

        try {
            // Schrijf de JSON-tekst naar een bestand met de naam "result.json"
            FileWriter fileWriter = new FileWriter("result.json");
            fileWriter.write(resultJSONString);
            fileWriter.close();
            System.out.println("Result JSON is opgeslagen in result.json");
        } catch (IOException e) {
            e.printStackTrace();
        }


        model.dispose();
        env.dispose();

    }

}
