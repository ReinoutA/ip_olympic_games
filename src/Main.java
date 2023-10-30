import gurobi.*;

public class Main {
    public Main() throws GRBException {
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
    }

    GRBModel gurobi = new GRBModel(new GRBEnv());

}