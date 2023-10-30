package instances;

public class TaskType {
    private String name;
    private int score;

    public TaskType(String name){
        this.name = name;
    }

    public TaskType(String name, int score){
        this.name = name;
        this.score = score;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public int getScore(){
        return score;
    }

    public void setScore(int score){
        this.score = score;
    }
}
