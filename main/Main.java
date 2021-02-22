package main;

import java.util.ArrayList;

import main.models.Delegation;

public class Main {
    public static void main(String args[]) {

        double optimalCost = Double.parseDouble(args[0]);
        int timeMillis = Integer.parseInt(args[1]);
        double epsilon = Double.parseDouble(args[2]);
        int meals = Integer.parseInt(args[3]);

        String[] citiesStarting = { "Wroclaw" };
        String[] citiesEnding = { "Wroclaw", "Bialystok", "Stargard", "Slupsk", "Torun", "Radom", "Zgorzelec",
                "Drezdenko", "Brzeg", "Rybnik", "Rzeszow", "Gdynia", "Czestochowa", "Miedzylesie", "Walbrzych",
                "Tarnow", "Lodz", "Ostrow Wielkopolski", "Zielona Gora", "Kedzierzyn-Kozle", "Wielun", "Olsztyn" };

        double[][] distances = { { 0.0, 535.0, 376.0, 443.0, 334.0, 357.0, 164.0, 256.0, 42.5, 188.0, 437.0, 506.0,
                200.0, 127.0, 82.4, 363.0, 217.0, 102.0, 187.0, 135.0, 125.0, 507.0 } };

        int[][] durations = { { 0, 19620, 14520, 20160, 13800, 15000, 6600, 11280, 3420, 7680, 15660, 19800, 9660, 7320,
                4500, 13500, 8820, 5760, 7560, 6240, 5700, 21000 } };
        

        Solver optimalDelegations = new Solver(optimalCost, distances, durations, citiesStarting, citiesEnding, meals);

        ArrayList<Delegation> optimal = optimalDelegations.solve(timeMillis, epsilon);

        double total = 0;
        System.out.println("---------------------------------------");
        for(Delegation delegation : optimal){
            total += delegation.delegationCost();
            System.out.println("cost: " + delegation.delegationCost() + "; "+ delegation.toString());
        }
        System.out.println("TOTAL: " + total);
        System.out.println("---------------------------------------");
        /*
        System.out.println("---------------------------------------");

        

        
        for(ArrayList<Delegation> delegations : optimalDelegations.getProposedSolutions()){
            total = 0;
            for(Delegation delegation : delegations){
                total += delegation.delegationCost();
                System.out.println("cost: " + delegation.delegationCost() + "; "+ delegation.toString());
            }
            System.out.println("TOTAL: " + total);
            System.out.println("---------------------------------------");
        }
        */
    }
}
