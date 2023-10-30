package instances;

import java.util.*;

public class Volunteer {
    private String id;
    private boolean isMale;
    private boolean isPresourced;
    private String locationId;
    private List<String> preferredLocationIds;
    private int availableDays;
    private List<Skill> skills;
    private List<TaskType> taskTypes;

    public void addPreferedLocationId(String preferredLocationId){
        preferredLocationIds.add(preferredLocationId);
    }

    public void addSkill(Skill skill){
        skills.add(skill);
    }

    public void addTaskType(TaskType taskType){
        taskTypes.add(taskType);
    }

    public String getId(){
        return id;
    }

    public boolean getIsMale(){
        return isMale;
    }

    public boolean getIsPresourced(){
        return isPresourced;
    }

    public String getLocationId(){
        return locationId;
    }

    public List<String> getPreferredLocationIds(){
        return preferredLocationIds;
    }

    public int getAvailableDays(){
        return availableDays;
    }

    public List<Skill> getSkills(){
        return skills;
    }

    public List<TaskType> getTaskTypes(){
        return taskTypes;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setIsMale(boolean isMale){
        this.isMale = isMale;
    }

    public void setIsPresourced(boolean isPresourced){
        this.isPresourced = isPresourced;
    }

    public void setLocationId(String locationId){
        this.locationId = locationId;
    }

    public void setPreferredLocationIds(List<String> preferredLocationIds){
        this.preferredLocationIds = preferredLocationIds;
    }

    public void setAvailableDays(int availableDays){
        this.availableDays = availableDays;
    }

    public void setSkills(List<Skill> skills){
        this.skills = skills;
    }

    public void setTaskTypes(List<TaskType> taskTypes){
        this.taskTypes = taskTypes;
    }





}
