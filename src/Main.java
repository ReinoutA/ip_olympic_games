import factories.*;
import gurobi.*;
import instances.*;


import java.io.IOException;
import java.util.*;

public class Main {
    public Main() throws GRBException {
    }

    public static void main(String[] args) throws IOException, GRBException {

        final String PATH = "IP_Olympic_Games/resources/toy_problem.json";

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
        for(Weight w : weights){
            if(w.getName().equals("travelDistanceWeight")){
                w_dist = w.getWeight();
            }else if(w.getName().equals("genderBalanceWeight")){
                w_gend = w.getWeight();
            }else if(w.getName().equals("taskTypeAdequacyWeight")){
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


        GRBEnv env = new GRBEnv("gurobi.log");
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


        /*
        // Constraint 1
        for(int v = 0; v < volunteers.size(); v++){
            GRBLinExpr constraint = new GRBLinExpr();
            for(int t = 0 ; t < tasks.size(); t++){
                if(tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v)) && !presourcedVolunteers.contains(volunteers.get(v))){
                    constraint.addTerm(1.0, x_vt[v][t]);
                }
            }
            if(constraint.size() > 0){
                model.addConstr(constraint, GRB.LESS_EQUAL, 1.0, "Constraint1_" + v);
            }
        }
*/

        // Voeg deze constraint toe na de definitie van x_vt-variabelen
        for (int v = 0; v < volunteers.size(); v++) {
            GRBLinExpr assignmentConstraint = new GRBLinExpr();
            for (int t = 0; t < tasks.size(); t++) {
                assignmentConstraint.addTerm(1.0, x_vt[v][t]);
            }
            model.addConstr(assignmentConstraint, GRB.LESS_EQUAL, 1.0, "AssignmentConstraint_" + v);
        }



        // Constraint 2
        for(int v = 0; v < volunteers.size(); v++){
            GRBLinExpr constraint = new GRBLinExpr();
            for(int t = 0; t < tasks.size(); t++){
                if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v)) && presourcedVolunteers.contains(volunteers.get(v))) {
                    // Voeg alleen een term toe als de voorwaarden zijn voldaan
                    constraint.addTerm(1.0, x_vt[v][t]);
                }
            }
            if (constraint.size() > 0) {
                // Voeg de constraint alleen toe als er daadwerkelijk presourced vrijwilligers zijn
                // anders creëer je altijd infeasibility
                model.addConstr(constraint, GRB.EQUAL, 1.0, "Constraint2_" + v);
            }
        }

        // Constraint 3
        for(int t = 0; t < tasks.size(); t++){
            GRBLinExpr constraint = new GRBLinExpr();
            for(int v = 0; v < volunteers.size(); v++){
                if(tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))){
                    constraint.addTerm(1.0, x_vt[v][t]);
                }
            }
            model.addConstr(constraint, GRB.LESS_EQUAL, tasks.get(t).getDemand(), "Constraint3_" + t);
        }

        // Constraint 4
        for(int t = 0; t < tasks.size(); t++){
            for(SkillRequirement skillRequirement : tasks.get(t).getSkillrequirementsWithHardConstraints()) {
                String skillId = skillRequirement.getSkillid();
                GRBLinExpr exprLeft = new GRBLinExpr();
                GRBLinExpr exprRight = new GRBLinExpr();
                double fraction = tasks.get(t).getSkillRequirement(skillId).getProportion();
                for(int v = 0; v < volunteers.size(); v++){
                    if(tasks.get(t).getVolunteersThatFullFillMinimumProficiencyForSkillRequirement().containsKey(skillRequirement)){
                        List<Volunteer> volunteersThatFullFillMinProf = tasks.get(t).getVolunteersThatFullFillMinimumProficiencyForSkillRequirement().get(skillRequirement);
                        if(volunteersThatFullFillMinProf.contains(volunteers.get(v))){
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
        for(int t = 0; t < tasks.size(); t++){
            GRBLinExpr exprLeft = new GRBLinExpr();
            GRBLinExpr exprRight = new GRBLinExpr();

            for(int v = 0; v < volunteers.size(); v++){
                if(tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))){
                    exprLeft.addTerm(0.45, x_vt[v][t]);
                }
            }

            for(int v = 0; v < volunteers.size(); v++){
                if(tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v)) && maleVolunteers.contains(volunteers.get(v))){
                    exprRight.addTerm(1.0, x_vt[v][t]);
                }
            }
            model.addConstr(exprLeft, GRB.LESS_EQUAL, exprRight, "Constraint5_" + t);
        }

        // Constraint 6
        for(int t = 0; t < tasks.size(); t++){
            GRBLinExpr exprLeft = new GRBLinExpr();
            GRBLinExpr exprRight = new GRBLinExpr();

            for(int v = 0; v < volunteers.size(); v++){
                if(tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v))){
                    exprLeft.addTerm(0.55, x_vt[v][t]);
                }
            }

            for(int v = 0; v < volunteers.size(); v++){
                if(tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v)) && maleVolunteers.contains(volunteers.get(v))){
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
                if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v)) && femaleVolunteers.contains(volunteers.get(v))) {
                    exprRight1.addTerm(1.0, x_vt[v][t]);
                }
            }

            for (int v = 0; v < volunteers.size(); v++) {
                if (tasks.get(t).getCanBeDoneByVolunteers().contains(volunteers.get(v)) && maleVolunteers.contains(volunteers.get(v))) {
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



        // Doelfunctie 1
        GRBLinExpr objExpr = new GRBLinExpr();
        for (int v = 0; v < volunteers.size(); v++) {
            for (int t = 0; t < tasks.size(); t++) {
                if(volunteers.get(v).getCanDoTasks().contains(tasks.get(t))) {
                    objExpr.addTerm(1.0, x_vt[v][t]);
                }
            }
        }
        model.setObjective(objExpr, GRB.MAXIMIZE);

        // Doelfunctie 2
        GRBLinExpr objExpr2 = new GRBLinExpr();
        GRBLinExpr expr1 = new GRBLinExpr();
        GRBLinExpr expr2 = new GRBLinExpr();

        for(int v = 0; v < volunteers.size(); v++){
            for(int t = 0; t < tasks.size(); t++){
                Task task = tasks.get(t);
                Volunteer volunteer = volunteers.get(v);

                Location taskLocation = null;
                Location volunteerLocation = null;
                for(Location l : locations){
                    if(l.getId().equals(task.getLocationId())){
                        taskLocation = l;
                    }
                }
                for(Location l : locations){
                    if(l.getId().equals(volunteer.getLocationId())){
                        volunteerLocation = l;
                    }
                }

                if(task.getCanBeDoneByVolunteers().contains(volunteer)){
                    int distance = haversine.calculateDistance(volunteerLocation.getLon(), volunteerLocation.getLat(), taskLocation.getLon(), taskLocation.getLat());
             
                }
            }
        }





        model.optimize();

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

            for(int v = 0; v < volunteers.size(); v++){
                for(int t = 0; t < tasks.size(); t++){
                    double val = x_vt[v][t].get(GRB.DoubleAttr.X);
                    //System.out.println("x_vt[" + v + "][" + t + "] = " + val);
                    Volunteer volunteer = volunteers.get(v);
                    Task task = tasks.get(t);
                    if(val == 1) System.out.println("Vrijwilliger " + volunteer.getId() + " is toegewezen aan task " + task.getId());
                }
            }
        }
    }




}



