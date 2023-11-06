import factories.*;
import gurobi.*;
import instances.*;
import org.json.*;

import java.io.*;
import java.util.*;


// AUTHORS: REINOUT ANNAERT & BRECHT VAN DE SIJPE
// MORE INFO IN README.md

public class Main {
    public Main() {
    }

    public static void main(String[] args) throws IOException, GRBException {

        //final String PATH = "IP_Olympic_Games/resources/toy_problem2.json";
         final String PATH = "IP_Olympic_Games/resources/i0_200t_5000v.json";

        // =========================================
        // ============== FACTORIES ================
        // =========================================

        LocationFactory locationFactory = new LocationFactory();
        SkillFactory skillFactory = new SkillFactory();
        WeightFactory weightFactory = new WeightFactory();
        TaskTypeFactory taskTypeFactory = new TaskTypeFactory();
        TaskFactory taskFactory = new TaskFactory();
        VolunteerFactory volunteerFactory = new VolunteerFactory();
        Haversine haversine = new Haversine();

        // =========================================
        // ============== INSTANCES ================
        // =========================================

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
            switch (w.getName()) {
                case "travelDistanceWeight":
                    w_dist = w.getWeight();
                    break;
                case "genderBalanceWeight":
                    w_gend = w.getWeight();
                    break;
                case "taskTypeAdequacyWeight":
                    w_type = w.getWeight();
                    break;
                default:
                    System.out.println("Invalid Weight Found.");
                    break;
            }
        }

        // =========================================
        // ========= DEELVERZAMELINGEN =============
        // =========================================

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

        // ===========================================
        // ========= INTEGER PROGRAMMING =============
        // ===========================================

        GRBEnv env = new GRBEnv("gurobi.log");
        env.set(GRB.IntParam.LogToConsole, 1);
        env.set(GRB.IntParam.OutputFlag, 1);
        env.start();
        GRBModel model = new GRBModel(env);
        model.set(GRB.IntAttr.ModelSense, GRB.MAXIMIZE);

        // ================================================
        // ========= 1. BESLISSINGSVARIABELEN =============
        // ===============================================

        // Definieer de beslissingsvariabelen x_vt
        GRBVar[][] x_vt = new GRBVar[volunteers.size()][tasks.size()];
        for (int v = 0; v < volunteers.size(); v++) {
            for (int t = 0; t < tasks.size(); t++) {
                x_vt[v][t] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_" + v + "_" + t);
            }
        }

        // Definieer de beslissingsvariabele y
        GRBVar y = model.addVar(0, volunteers.size(), 0.0, GRB.INTEGER, "y");

        // ===============================================
        // =============== 2. CONSTRAINTS ================
        // ===============================================

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
                    constraint.addTerm(1.0, x_vt[v][t]);
                }
            }
            if (constraint.size() > 0) {
                model.addConstr(constraint, GRB.EQUAL, 1.0, "Constraint2_" + v);
            }
        }

        // Constraint 3
        for (int t = 0; t < tasks.size(); t++) {
            GRBLinExpr constraint = new GRBLinExpr();
            for (int v = 0; v < volunteers.size(); v++) {
                if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))) {
                    constraint.addTerm(1.0, x_vt[v][t]);
                }
            }
            model.addConstr(constraint, GRB.LESS_EQUAL, tasks.get(t).getDemand(), "Constraint3_" + t);
        }

        // Constraint 4
        for (int t = 0; t < tasks.size(); t++) {
            for (String skill : tasks.get(t).getSkillRequirementsSkillIds()) {
                GRBLinExpr exprL = new GRBLinExpr();
                GRBLinExpr exprR = new GRBLinExpr();
                double fraction = tasks.get(t).getSkillRequirement(skill).getProportion();
                for (int v = 0; v < volunteers.size(); v++) {
                    if (tasks.get(t).getVolunteersThatFullFillMinimumProficiencyForSkillRequirement().containsKey(tasks.get(t).getSkillRequirement(skill))) {
                        List<Volunteer> vol = tasks.get(t).getVolunteersThatFullFillMinimumProficiencyForSkillRequirement().get(tasks.get(t).getSkillRequirement(skill));
                        if (vol.contains(volunteers.get(v))) {
                            exprL.addTerm(1.0, x_vt[v][t]);
                        }
                        if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))) {
                            exprR.addTerm(fraction, x_vt[v][t]);
                        }
                    }

                }
                model.addConstr(exprL, GRB.GREATER_EQUAL, exprR, "Constraint4");
            }
        }

        // Constraint 5 and 6
        GRBLinExpr exprDiffer = new GRBLinExpr();
        GRBLinExpr ExprMale = new GRBLinExpr();
        GRBLinExpr exprHigh = new GRBLinExpr();
        GRBLinExpr exprLow = new GRBLinExpr();

        for (int v = 0; v < volunteers.size(); v++) {
            for (int t = 0; t < tasks.size(); t++) {
                if (maleVolunteers.contains(volunteers.get(v))) {
                    ExprMale.addTerm(1.0, x_vt[v][t]);
                    exprDiffer.addTerm(-1.0, x_vt[v][t]);
                }

                else
                    exprDiffer.addTerm(1.0, x_vt[v][t]);

                exprLow.addTerm(0.45, x_vt[v][t]);
                exprHigh.addTerm(0.55, x_vt[v][t]);
            }
        }
        model.addConstr(ExprMale, GRB.LESS_EQUAL, exprHigh, "CONSTRAINT6");
        model.addConstr(ExprMale, GRB.GREATER_EQUAL, exprLow, "CONSTRAINT7");

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

            GRBVar differenceVar = model.addVar(-GRB.INFINITY, GRB.INFINITY, 0.0, GRB.INTEGER, "differenceVar");

            model.addConstr(differenceVar, GRB.GREATER_EQUAL, exprRight1, "DifferenceConstraintRight1_" + t);
            model.addConstr(differenceVar, GRB.GREATER_EQUAL, exprRight2, "DifferenceConstraintRight2_" + t);

            model.addGenConstrAbs(y, differenceVar, "Constraint7_" + t);
        }

        // Constraint EXTRA
        // U heeft deze constraint niet nodig omdat u enkel beslissingsvariabelen
        // aanmaakt voor t element van T_v.
        // We hebben gebruik gemaakt van de hele set T, dus we hebben deze constraint nodig.
        for (int v = 0; v < volunteers.size(); v++) {
            for (int t = 0; t < tasks.size(); t++) {
                Task task = tasks.get(t);
                Volunteer volunteer = volunteers.get(v);
                if (!task.getCanBeDoneByVolunteers().contains(volunteer)) {
                    model.addConstr(x_vt[v][t], GRB.EQUAL, 0, "CONSTR_EXTR");
                }
            }
        }

        // ================================================
        // =============== 3. DOELFUNCTIES ================
        // ================================================

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
                    int f_vt = haversine.calculateDistance(volunteerLocation.getLon(), volunteerLocation.getLat(),
                            taskLocation.getLon(), taskLocation.getLat());
                    String taskTypeId = task.getTaskTypeId();
                    int q_vnt = volunteer.getScoreOfTaskType(taskTypeId);
                    expr1.addTerm(w_dist * f_vt * 2 * task.getDays(), x_vt[v][t]);
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
                        Map<SkillRequirement, List<Volunteer>> m = task
                                .getVolunteersThatDontFullFillMinimumProficiencyForSkillRequirement();
                        List<Volunteer> volunteerList = m.get(skillRequirement);
                        if (volunteerList.contains(volunteer)) {
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

        // Doelfunctie 1 (maximaliseren met prioriteit 1)
        model.setObjectiveN(objectiveFunction1, 0, 1, 1, 1e-6, 0, "Objective1");

        // Doelfunctie 2 (minimaliseren met prioriteit 0)
        model.setObjectiveN(objectiveFunction2, 1, 0, -1, 1e-6, 0, "Objective2");

        model.update();
        model.optimize();

        // ==========================================
        // =============== DEBUGGING ================
        // ==========================================

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

        // =======================================
        // ================ I/O ==================
        // =======================================

        JSONObject resultJSON = new JSONObject();
        resultJSON.put("assignedVolunteers", model.getObjective(0).getValue());
        resultJSON.put("assignmentCost", model.getObjective(1).getValue());

        JSONArray assignmentsArray = new JSONArray();

        for (int v = 0; v < volunteers.size(); v++) {
            for (int t = 0; t < tasks.size(); t++) {
                // 0.5 i.p.v. 1.0 om afrondingsfouten te vermijden
                if (x_vt[v][t].get(GRB.DoubleAttr.X) > 0.5) {
                    JSONObject assignmentJSON = new JSONObject();
                    assignmentJSON.put("volunteerId", volunteers.get(v).getId());
                    assignmentJSON.put("taskId", tasks.get(t).getId());
                    assignmentsArray.put(assignmentJSON);
                }
            }
        }

        resultJSON.put("assignments", assignmentsArray);
        String resultJSONString = resultJSON.toString();

        try {
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
