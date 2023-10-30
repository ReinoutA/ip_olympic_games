import factories.LocationFactory;
import gurobi.*;
import instances.*;

import java.io.IOException;
import java.util.*;

public class Main {
    public Main() throws GRBException {
    }

    public static void main(String[] args) throws IOException {
        String PATH = "IP_Olympic_Games/resources/toy_problem.json";
        LocationFactory locationFactory = new LocationFactory();

        List<Location> locations = locationFactory.createLocationsFromJSON(PATH);

        for(Location l : locations){
            System.out.println(locations);
        }
    }

}