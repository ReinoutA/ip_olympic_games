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

    

}
