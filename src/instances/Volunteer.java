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

    public Volunteer (String id, boolean isMale, boolean isPresourced, String locationId, List<String> preferredLocationIds, int availableDays, List<Skill> skills, List<TaskType> taskTypes){
        this.id = id;
        this.isMale = isMale;
        this.isPresourced = isPresourced;
        this.locationId = locationId;
        this.preferredLocationIds = preferredLocationIds;
        this.availableDays = availableDays;
        this.skills = skills;
        this.taskTypes = taskTypes;
    }
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

    @Override
    public String toString() {
        return "Volunteer{" +
                "id='" + id + '\'' +
                ", isMale=" + isMale +
                ", isPresourced=" + isPresourced +
                ", locationId='" + locationId + '\'' +
                ", preferredLocationIds=" + preferredLocationIds +
                ", availableDays=" + availableDays +
                ", skills=" + skills +
                ", taskTypes=" + taskTypes +
                '}';
    }



}
