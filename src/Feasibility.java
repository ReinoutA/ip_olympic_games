import factories.*;
import gurobi.*;
import instances.*;
import org.json.*;

import java.io.*;
import java.util.*;


// GROEPSLEDEN: REINOUT ANNAERT & BRECHT VAN DE SIJPE
// INFO IN README.md

public class Feasibility {
    public Feasibility() {
    }

    public static void main(String[] args) throws IOException, GRBException {

        final String PATH = "IP_Olympic_Games/resources/p0_200t_5000v.json";
        //final String PATH = "IP_Olympic_Games/resources/i0_200t_5000v.json";
       // final String PATH = "IP_Olympic_Games/resources/p2_781t_40000v.json";
        // =========================================
        // ============== FACTORIES ================
        // =========================================

        TaskFactory taskFactory = new TaskFactory();
        VolunteerFactory volunteerFactory = new VolunteerFactory();
        // =========================================
        // ============== INSTANCES ================
        // =========================================

        List<Task> tasks = taskFactory.createTasksFromJSON(PATH);
        List<Volunteer> volunteers = volunteerFactory.createVolunteersFromJSON(PATH);


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
        System.out.println("PREPROCESS 1 ");
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
        System.out.println("PREPROCESS 2-3 ");
        System.out.println("2. Aantal mannen: " + maleVolunteers.size());
        System.out.println("3. Aantal vrouwen: " + femaleVolunteers.size());

        // 4. Deelverzameing van vrijwilligers die toegewezen kunnen worden aan taak t
        // op basis van voorkeurslocaties, beschikbaarheid en taaktypegeschiktheid
        for (Task t : tasks) {
            t.createCanBeDoneByVolunteersList(volunteers);
        }
        System.out.println("PREPROCESS 4 ");
        // 5. Deelverzameling van taken waaraan vrijwilliger v kan toegewezen worden op
        // basis van voorkeurslocaties, beschikbaarheid en taaktypegeschiktheid
        for (Volunteer v : volunteers) {
            v.addCanDoTasks(tasks);
        }
        System.out.println("PREPROCESS 5 ");
        // 6. en 7 Deelverzameling van vrijwilligers die wel/niet voldoen aan het
        // minimumbekwaakheidsniveau voor skill s van taak t
        for (Task t : tasks) {
            t.createVolunteersThatFullFillMinimumProficiencyForSkillRequirement(volunteers);
        }
        System.out.println("PREPROCESS 6-7 ");
        // 8. en 9. Deelverzameling van vaardigheden waar taak t een harde, zachte eis
        // voor heeft
        for (Task t : tasks) {
            t.createSkillRequirementsSoftHardConstraintsLists();
        }
        System.out.println("PREPROCESS 8-9 ");
        // ===========================================
        // ========= INTEGER PROGRAMMING =============
        // ===========================================

        GRBEnv env = new GRBEnv("gurobi.log");
        env.set(GRB.IntParam.LogToConsole, 1);
        env.set(GRB.IntParam.OutputFlag, 1);
        env.start();
        GRBModel model = new GRBModel(env);
        //model.set(GRB.DoubleParam.Heuristics, 0.8);
        model.set(GRB.DoubleParam.MIPGap, 0.01);


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
        System.out.println("CONSTR 1 ");
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
        System.out.println("CONSTR 2 ");
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
        System.out.println("CONSTR 3 ");
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
        System.out.println("CONSTR 4 ");
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
        System.out.println("CONSTR 5-6 ");
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
        System.out.println("CONSTR 7");
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

        System.out.println("CONSTR EXTR ");


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
        resultJSON.put("assignedVolunteers", 0);
        resultJSON.put("assignmentCost", 0);

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
