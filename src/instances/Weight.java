package instances;

public class Weight {
    private String name;
    private double weight;

    public Weight(String name, double weight){
        this.name = name;
        this.weight = weight;
    }

    public String getName(){
        return name;
    }

    public double getWeight(){
        return weight;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setWeight(double weight){
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Weight{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                '}';
    }
}
