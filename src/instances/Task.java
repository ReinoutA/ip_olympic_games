package instances;

import java.util.*;

public class Task {
    private String id;
    private String locationId;
    private int demand;
    private int days;
    private String taskTypeId;
    private List<SkillRequirement> skillrequirements;

    public Task(String id, String locationId, int demand, int days, String taskTypeId, List<SkillRequirement> skillrequirements){
        this.id = id;
        this.locationId = locationId;
        this.demand = demand;
        this.days = days;
        this.taskTypeId = taskTypeId;
        this.skillrequirements = skillrequirements;
    }

    public String getId(){
        return id;
    }

    public String getLocationId(){
        return locationId;
    }

    public int getDemand(){
        return demand;
    }

    public String getTaskTypeId(){
        return taskTypeId;
    }

    public List<SkillRequirement> getSkillRequirements(){
        return skillrequirements;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setLocationId(String locationId){
        this.locationId = locationId;
    }

    public void setDemand(int demand){
        this.demand = demand;
    }

    public void setTaskTypeId(String taskTypeId){
        this.taskTypeId = taskTypeId;
    }

    public void setSkillrequirements(List<SkillRequirement> skillrequirements){
        this.skillrequirements = skillrequirements;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", locationId='" + locationId + '\'' +
                ", demand=" + demand +
                ", days=" + days +
                ", taskTypeId='" + taskTypeId + '\'' +
                ", skillrequirements=" + skillrequirements +
                '}';
    }

    public void addSkillRequirement(SkillRequirement skillRequirement){
        skillrequirements.add(skillRequirement);
    }
}

