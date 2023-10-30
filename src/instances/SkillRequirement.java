package instances;

public class SkillRequirement {
    private String skillId;
    private int minProficiency;
    private boolean isHard;
    private double proportion;
    private double weight;

    public SkillRequirement(String skillId, int minProficiency, boolean isHard, double proportion, double weight ){
        this.skillId = skillId;
        this.minProficiency = minProficiency;
        this.isHard = isHard;
        this.proportion = proportion;
        this.weight = weight;
    }

    public String getSkillid(){
        return skillId;
    }

    public int getMinProficiency(){
        return minProficiency;
    }

    public boolean getIsHard(){
        return isHard;
    }

    public double getProportion(){
        return proportion;
    }

    public double getWeight(){
        return weight;
    }

    public void setSkillId(String skillid){
        this.skillId = skillid;
    }

    public void setMinProficiency(int minProficiency){
        this.minProficiency = minProficiency;
    }

    public void setIsHard(){
        this.isHard = isHard;
    }

    public void setProportion(){
        this.proportion = proportion;
    }

    public void setWeight(double weight){
        this.weight = weight;
    }
}
