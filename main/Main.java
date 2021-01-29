package main;

import java.util.ArrayList;

import main.models.Delegation;

public class Main {
    public static void main(String args[]) {

        double optimalCost = Double.parseDouble(args[0]);
        int timeMillis = Integer.parseInt(args[1]);
        double epsilon = Double.parseDouble(args[2]);

        String[] citiesStarting = { "Wroclaw" };
        String[] citiesEnding = { "Wroclaw", "Bialystok", "Stargard", "Slupsk", "Torun", "Radom", "Zgorzelec",
                "Drezdenko", "Brzeg", "Rybnik", "Rzeszow", "Gdynia", "Czestochowa", "Miedzylesie", "Walbrzych",
                "Tarnow", "Lodz", "Ostrow Wielkopolski", "Zielona Gora", "Kedzierzyn-Kozle", "Wielun", "Olsztyn" };
        double[][] distances = { { 0.0, 542.0, 376.0, 443.0, 334.0, 357.0, 164.0, 256.0, 48.6, 188.0, 437.0, 506.0,
                200.0, 127.0, 82.4, 363.0, 224.0, 110.0, 187.0, 135.0, 132.0, 514.0 } };
        

        Solver optimalDelegations = new Solver(optimalCost, distances, citiesStarting, citiesEnding);
        
        ArrayList<Delegation> optimal = optimalDelegations.solve(timeMillis, epsilon);

        double total = 0;
        for(Delegation delegation : optimal){
            total += delegation.delegationCost();
            System.out.println("cost: " + delegation.delegationCost() + "; "+ delegation.toString());
        }
        System.out.println("TOTAL: " + total);
        

        /*
        for(ArrayList<Delegation> delegations : optimalDelegations.getProposedSolutions()){
            for(Delegation delegation : delegations){
                System.out.println("cost: " + delegation.delegationCost() + "; "+ delegation.toString());
            }
            System.out.println("---------------------------------------");
        }
        */
    }
}
