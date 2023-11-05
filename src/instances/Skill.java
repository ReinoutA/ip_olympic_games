package instances;

public class Skill {
    private String skillId;
    private int score;

    public Skill(String name) {
        this.skillId = name;
    }

    public Skill(String name, int score) {
        this.skillId = name;
        this.score = score;
    }

    public String getSkillId() {
        return skillId;
    }

    public void setName(String name) {
        this.skillId = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Skill{" +
                "name='" + skillId + '\'' +
                ", score=" + score +
                '}';
    }

}
