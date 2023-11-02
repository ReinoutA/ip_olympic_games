package instances;

import java.util.*;

public class Task {
    private String id;
    private String locationId;
    private int demand;
    private int days;
    private String taskTypeId;
    private List<SkillRequirement> skillrequirements;
    private List<Volunteer> canBeDoneByVolunteers;
    private List<SkillRequirement> skillrequirementsWithSoftConstraints;
    private List<SkillRequirement> skillrequirementsWithHardConstraints;
    private Map<SkillRequirement, List<Volunteer>> volunteersThatFullFillMinimumProficiencyForSkillRequirement;
    private Map<SkillRequirement, List<Volunteer>> volunteersThatDontFullFillMinimumProficiencyForSkillRequirement;
    public Task(String id, String locationId, int demand, int days, String taskTypeId, List<SkillRequirement> skillrequirements){
        this.id = id;
        this.locationId = locationId;
        this.demand = demand;
        this.days = days;
        this.taskTypeId = taskTypeId;
        this.skillrequirements = skillrequirements;
        this.canBeDoneByVolunteers = new ArrayList<>();
        this.skillrequirementsWithSoftConstraints = new ArrayList<>();
        this.skillrequirementsWithHardConstraints = new ArrayList<>();
        this.volunteersThatFullFillMinimumProficiencyForSkillRequirement = new HashMap<>();
        this.volunteersThatDontFullFillMinimumProficiencyForSkillRequirement = new HashMap<>();
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

    public int getDays(){return days;}
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

    public List<SkillRequirement> getSkillrequirementsWithSoftConstraints(){
        return skillrequirementsWithSoftConstraints;
    }

    public List<SkillRequirement> getSkillrequirementsWithHardConstraints(){
        return skillrequirementsWithHardConstraints;
    }

    public void createCanBeDoneByVolunteersList(List<Volunteer> volunteers){
        for(Volunteer v : volunteers){
            // locatie OKE
            if(v.getPreferredLocationIds().contains(locationId)){
                // beschikbaarheid OKE
                if(v.getAvailableDays() >= days){
                    if(v.getScoreOfTaskType(taskTypeId) != 0){
                        canBeDoneByVolunteers.add(v);
                    }
                }
            }
        }
    }

    public void createSkillRequirementsSoftHardConstraintsLists(){
        for(SkillRequirement s : skillrequirements){
            if(s.isHard()){
                skillrequirementsWithHardConstraints.add(s);
            }else{
                skillrequirementsWithSoftConstraints.add(s);
            }
        }
    }

    public void createVolunteersThatFullFillMinimumProficiencyForSkillRequirement(List<Volunteer> volunteers){
        for(SkillRequirement skillRequirement : skillrequirements) {
            List<Volunteer> volunteersThatMeetRequirement = new ArrayList<>();
            List<Volunteer> volunteersThatDontMeetRequirement = new ArrayList<>();
            for (Volunteer v : volunteers) {
                for(Skill skill : v.getSkills()){
                    if(skill.getSkillId().equals(skillRequirement.getSkillid())){
                        if(skill.getScore() >= skillRequirement.getMinProficiency()) {
                            volunteersThatMeetRequirement.add(v);
                        }else{
                            volunteersThatDontMeetRequirement.add(v);
                        }
                    }
                }
            }
            volunteersThatFullFillMinimumProficiencyForSkillRequirement.put(skillRequirement, volunteersThatMeetRequirement);
            volunteersThatDontFullFillMinimumProficiencyForSkillRequirement.put(skillRequirement, volunteersThatDontMeetRequirement);
        }
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
                ", canBeDoneByVolunteers=" + canBeDoneByVolunteers +
                '}';
    }

    public void addSkillRequirement(SkillRequirement skillRequirement){
        skillrequirements.add(skillRequirement);
    }
}

