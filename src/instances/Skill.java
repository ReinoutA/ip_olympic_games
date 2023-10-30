package instances;

public class Skill {
    private String name;
    private int score;

    public Skill(String name){
        this.name = name;
    }

    public Skill(String name, int score){
        this.name = name;
        this.score = score;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getScore(){
        return score;
    }

    @Override
    public String toString() {
        return "Skill{" +
                "name='" + name + '\'' +
                ", score=" + score +
                '}';
    }

    public void setScore(int score){
        this.score = score;
    }
}
