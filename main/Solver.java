package main;

import main.models.*;
import java.util.*;
import java.security.SecureRandom;

public class Solver {

    /*
     * km > 0 days >=2 and days <=5 meals >=0 and meals <= 4*days
     */

    private final int populationSize = 20;
    private final int tournamentContestants = 5;

    private ArrayList<Delegation> bestSolution;
    private double bestFitness;
    private ArrayList<ArrayList<Delegation>> population = new ArrayList<>();
    private int maxDelegations = 0;
    private int maxMeals;

    private double optimalTotalCost;
    private double[][] distances;
    private int[][] durations;
    private String[] citiesStart;
    private String[] citiesEnd;

    public Solver(double optimalTotalCost, double[][] distances, int[][] durations, String[] citiesStart,
            String[] citiesEnd, int meals) {
        this.optimalTotalCost = optimalTotalCost;
        this.distances = distances;
        this.durations = durations;
        this.citiesStart = citiesStart;
        this.citiesEnd = citiesEnd;
        bestFitness = optimalTotalCost;
        maxMeals = meals;

        if (optimalTotalCost <= 500) {
            maxDelegations = 1;
        } else if (optimalTotalCost <= 1000) {
            maxDelegations = 2;
        } else if (optimalTotalCost <= 1500) {
            maxDelegations = 3;
        } else if (optimalTotalCost <= 2000) {
            maxDelegations = 4;
        } else if (optimalTotalCost <= 2500) {
            maxDelegations = 5;
        } else {
            maxDelegations = 6;
        }

        generateInitialPopulation();
    }

    public ArrayList<ArrayList<Delegation>> getProposedSolutions() {
        return population;
    }

    public ArrayList<Delegation> getBestSolution() {
        return bestSolution;
    }

    /**
     * Generate list of available Distances from given matrix and city names
     * 
     * @return List of all Distances
     */
    private ArrayList<Distance> generateDistancesList() {
        ArrayList<Distance> distancesList = new ArrayList<>();

        for (int i = 0; i < citiesStart.length; i++) {
            for (int j = 0; j < citiesEnd.length; j++) {
                if (i == j)
                    continue;
                distancesList.add(new Distance(distances[i][j], durations[i][j], i, j, citiesStart[i], citiesEnd[j]));
            }
        }

        return distancesList;
    }

    /**
     * Generate a few example answers for the algorithm to work with
     */
    private void generateInitialPopulation() {
        SecureRandom rand = new SecureRandom();
        ArrayList<Distance> distancesList = generateDistancesList();
        Collections.sort(distancesList);

        for (int i = 0; i < populationSize / 2; i++) {
            ArrayList<Delegation> proposedSolutionDeterministic = new ArrayList<>();
            ArrayList<Delegation> proposedSolutionRandom = new ArrayList<>();
            ArrayList<Integer> usedPoints = new ArrayList<>();

            // Generate random solution
            double previousFitness = optimalTotalCost;
            double currentFitness = checkFitness(proposedSolutionRandom);

            // Generate Delegations with distinct distances and stop when fitness drops
            // beneath 200 or starts to grow again (optimalTotalCost check for the first
            // Delegation because
            // we're starting from empty list and delegations amount check to not generate
            // too many)
            int delCount = 0;
            while (currentFitness <= previousFitness && (currentFitness > 200 || currentFitness == optimalTotalCost)
                    && delCount < maxDelegations) {
                int days = rand.nextInt(4) + 2;
                int distanceIndex = rand.nextInt(distancesList.size());
                days = distancesList.get(distanceIndex).duration.toHoursPart() < 2 ? 1 : days;
                int reduction = 4 * days > maxMeals ? maxMeals : 4 * days;

                if (!usedPoints.contains(distanceIndex)) {
                    usedPoints.add(distanceIndex);
                } else {
                    continue;
                }

                proposedSolutionRandom
                        .add(new Delegation(distancesList.get(distanceIndex), days, rand.nextInt(reduction + 1)));

                previousFitness = currentFitness;
                currentFitness = checkFitness(proposedSolutionRandom);
                delCount++;
            }
            population.add(proposedSolutionRandom);

            // Generate deterministic solution
            int subDistanceListSize = distancesList.size() / maxDelegations;
            usedPoints.clear();

            // Generate Delegations where number of them is decided by how many mean-sized
            // trips can worker make based on optimal cost
            // Then each Delegation is taken from another partition of availableDistances eg
            // (shortTrip, mediumLenghtTrip, longTrip)
            for (int j = 0; j < maxDelegations; j++) {
                int days = rand.nextInt(4) + 2;
                int reduction = 4 * days > maxMeals ? maxMeals : 4 * days;
                int distanceIndex = rand.nextInt(subDistanceListSize) + j * subDistanceListSize;
                days = distancesList.get(distanceIndex).duration.toHoursPart() < 2 ? 1 : days;
                proposedSolutionDeterministic
                        .add(new Delegation(distancesList.get(distanceIndex), days, rand.nextInt(reduction + 1)));
            }
            population.add(proposedSolutionDeterministic);
        }
    }

    /**
     * Calculate cost of all delegations and how close it is to optimal cost
     * 
     * @param delegations
     * @return Absolute error of the cost of delegations and optimal cost
     */
    private double checkFitness(ArrayList<Delegation> delegations) {
        double currentCost = 0;

        for (Delegation delegation : delegations) {
            currentCost += delegation.delegationCost();
        }

        return Math.abs(currentCost - optimalTotalCost);
    }

    /**
     * Calculate fitness function for every solution in population and update best
     * to date solution if needed
     * 
     * @return Array containing resuts of fitness function for every solution
     */
    private double[] calculateFitnesses() {
        double[] newFitnesses = new double[populationSize];
        ArrayList<Distance> distanceMonitor = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            newFitnesses[i] = checkFitness(population.get(i));

            // Penalty for repeating delegation
            for (Delegation it : population.get(i)) {
                if (!distanceMonitor.contains(it.distance)) {
                    distanceMonitor.add(it.distance);
                } else {
                    newFitnesses[i] += 10000;
                }
            }

            distanceMonitor.clear();

            if (newFitnesses[i] < bestFitness) {
                bestFitness = newFitnesses[i];
                bestSolution = population.get(i);
            }
        }

        return newFitnesses;
    }

    /**
     * Select best solution from the set number of randomly selected solutions from
     * current population
     * 
     * @return Index of best solution found
     */
    private int linearTournament(double[] fitnesses) {
        SecureRandom rand = new SecureRandom();

        int bestIndex = rand.nextInt(populationSize);

        for (int i = 0; i < tournamentContestants; i++) {
            int candidate = rand.nextInt(populationSize);

            if (fitnesses[candidate] < fitnesses[bestIndex]) {
                bestIndex = candidate;
            }
        }

        return bestIndex;
    }

    /**
     * Swap one delegation from first solution with one from the second one
     * 
     * @param delegation1
     * @param delegation2
     * @return Two element arrayList containing changed values
     */
    private ArrayList<ArrayList<Delegation>> delegationCrossover(ArrayList<Delegation> delegation1,
            ArrayList<Delegation> delegation2) {
        ArrayList<ArrayList<Delegation>> results = new ArrayList<ArrayList<Delegation>>(2);
        SecureRandom rand = new SecureRandom();

        int maxSize = delegation1.size() >= delegation2.size() ? delegation2.size() : delegation1.size();

        int swapIndex = rand.nextInt(maxSize);
        Delegation swap = delegation1.get(swapIndex);

        delegation1.set(swapIndex, delegation2.get(swapIndex));
        delegation2.set(swapIndex, swap);

        results.add(delegation1);
        results.add(delegation2);

        return results;
    }

    /**
     * Swap duration of delegation between two random delegations from both
     * solutions
     * 
     * @param delegation1
     * @param delegation2
     * @return Two element arrayList containing changed values
     */
    private ArrayList<ArrayList<Delegation>> daysCrossover(ArrayList<Delegation> delegation1,
            ArrayList<Delegation> delegation2) {
        ArrayList<ArrayList<Delegation>> results = new ArrayList<ArrayList<Delegation>>(2);
        SecureRandom rand = new SecureRandom();

        int maxSize = delegation1.size() >= delegation2.size() ? delegation2.size() : delegation1.size();
        int swapIndex = rand.nextInt(maxSize);

        Delegation swap = delegation1.get(swapIndex);
        int daysSwap = swap.days;
        swap.days = swap.distance.duration.toHoursPart() < 2 ? 1 : delegation2.get(swapIndex).days;
        int reduction = 4 * swap.days > maxMeals ? maxMeals : 4 * swap.days;
        swap.mealsReduction = swap.mealsReduction > reduction ? reduction : swap.mealsReduction;
        delegation1.set(swapIndex, swap);

        swap = delegation2.get(swapIndex);
        swap.days = swap.distance.duration.toHoursPart() < 2 ? 1 : daysSwap;
        reduction = 4 * swap.days > maxMeals ? maxMeals : 4 * swap.days;
        swap.mealsReduction = swap.mealsReduction > reduction ? reduction : swap.mealsReduction;
        delegation2.set(swapIndex, swap);

        results.add(delegation1);
        results.add(delegation2);

        return results;
    }

    /**
     * Swap meal reductions of delegation between two random delegations from both
     * solutions
     * 
     * @param delegation1
     * @param delegation2
     * @return Two element arrayList containing changed values
     */
    private ArrayList<ArrayList<Delegation>> mealsCrossover(ArrayList<Delegation> delegation1,
            ArrayList<Delegation> delegation2) {
        ArrayList<ArrayList<Delegation>> results = new ArrayList<ArrayList<Delegation>>(2);
        SecureRandom rand = new SecureRandom();

        int maxSize = delegation1.size() >= delegation2.size() ? delegation2.size() : delegation1.size();
        int swapIndex = rand.nextInt(maxSize);

        Delegation swap = delegation1.get(swapIndex);
        int mealsSwap = swap.mealsReduction;
        int reduction = 4 * swap.days > maxMeals ? maxMeals : 4 * swap.days;
        swap.mealsReduction = delegation2.get(swapIndex).mealsReduction > reduction ? reduction
                : delegation2.get(swapIndex).mealsReduction;
        delegation1.set(swapIndex, swap);

        swap = delegation2.get(swapIndex);
        reduction = 4 * swap.days > maxMeals ? maxMeals : 4 * swap.days;
        swap.mealsReduction = mealsSwap > reduction ? reduction : mealsSwap;
        delegation2.set(swapIndex, swap);

        results.add(delegation1);
        results.add(delegation2);

        return results;
    }

    /**
     * Randomly change amount of company funded meals in one delegation
     * 
     * @param delegation
     * @return Mutated delegation
     */
    private Delegation mealsMutation(Delegation delegation) {
        SecureRandom rand = new SecureRandom();
        int reduction = 4 * delegation.days > maxMeals ? maxMeals : 4 * delegation.days;
        delegation.mealsReduction = rand.nextInt(reduction + 1);

        return delegation;
    }

    /**
     * Randomly change duration of one delegation
     * 
     * @param delegation
     * @return Mutated delegation
     */
    private Delegation daysMutation(Delegation delegation) {
        SecureRandom rand = new SecureRandom();
        delegation.days = delegation.distance.duration.toHoursPart() < 2 ? 1 : rand.nextInt(4) + 2;
        int reduction = 4 * delegation.days > maxMeals ? maxMeals : 4 * delegation.days;
        delegation.mealsReduction = delegation.mealsReduction > reduction ? reduction : delegation.mealsReduction;

        return delegation;
    }

    /**
     * Merges two delegations which have lowest distances
     * 
     * @param delegations
     * @return Mutated list of delegations
     */
    private ArrayList<Delegation> mergeMutation(ArrayList<Delegation> delegations) {

        ArrayList<Distance> availableDistances = generateDistancesList();
        ArrayList<Distance> takenDistances = new ArrayList<>();
        Delegation min = null;
        Delegation min2 = null;

        // Find already taken distances and find two shortest delegations
        for (Delegation delegation : delegations) {
            takenDistances.add(delegation.distance);
            if (min == null || min.distance.kilometres >= delegation.distance.kilometres) {
                min2 = min;
                min = delegation;
            } else if (min2 == null || min2.distance.kilometres >= delegation.distance.kilometres) {
                min2 = delegation;
            }
        }

        if (min2 == null)
            min2 = min;

        // Remove taken Distances
        for (Distance distance : takenDistances) {
            availableDistances.removeIf(d -> d.start == distance.start && d.end == distance.end);
        }

        // Sort availableDistances to easily get min max
        Collections.sort(availableDistances);

        // If all points are in use set new Delegation distance to min2.distance
        try {
            Distance max = availableDistances.get(availableDistances.size() - 1);
            final double minDistance = 2 * min.distance.kilometres;

            // Remove distances shorter than 2 times min
            availableDistances.removeIf(d -> d.kilometres <= minDistance);

            // Get max or shortest of remaining available distances
            Distance newDistance = availableDistances.isEmpty() ? max : availableDistances.get(0);

            // Merge delegations to one
            min.days = newDistance.duration.toHoursPart() < 2 ? 1 : min.days;
            int reduction = min2.mealsReduction > 4 * min.days ? 4 * min.days : min2.mealsReduction;
            reduction = reduction > maxMeals ? maxMeals : reduction;
            Delegation merged = new Delegation(newDistance, min.days, reduction);

            delegations.add(merged);
        } catch (IndexOutOfBoundsException e) {
            min.days = min2.distance.duration.toHoursPart() < 2 ? 1 : min.days;
            int reduction = min.mealsReduction > 4 * min.days ? 4 * min.days : min.mealsReduction;
            reduction = reduction > maxMeals ? maxMeals : reduction;
            Delegation merged = new Delegation(min2.distance, min.days, reduction);
            delegations.add(merged);
        }

        delegations.remove(min);
        delegations.remove(min2);

        return delegations;
    }

    /**
     * Split highest distance delegation into two
     * 
     * @param delegations
     * @return Mutated list of delegations
     */
    private ArrayList<Delegation> splitMutation(ArrayList<Delegation> delegations) {

        if (delegations.size() >= maxDelegations) {
            return delegations;
        }

        ArrayList<Distance> availableDistances = generateDistancesList();
        ArrayList<Distance> takenDistances = new ArrayList<>();
        Delegation max = null;

        // Find already taken distances and find maximum
        for (Delegation delegation : delegations) {
            takenDistances.add(delegation.distance);
            if (max == null || max.distance.kilometres <= delegation.distance.kilometres) {
                max = delegation;
            }
        }

        // Remove taken Distances
        for (Distance distance : takenDistances) {
            availableDistances.removeIf(d -> d.start == distance.start && d.end == distance.end);
        }

        // Sort availableDistances to easily get max
        Collections.sort(availableDistances);

        try {
            Distance min = availableDistances.get(0);
            Distance min2 = availableDistances.get(1);

            final double maxDistance = max.distance.kilometres / 2;

            // Remove distances longer than max distance by 2
            availableDistances.removeIf(d -> d.kilometres >= maxDistance);

            // Get min or two longest of remaining available distances
            Distance newDistance = availableDistances.isEmpty() ? min
                    : availableDistances.get(availableDistances.size() - 1);
            Distance newDistance2 = availableDistances.size() < 2 ? min2
                    : availableDistances.get(availableDistances.size() - 2);

            // Merge delegations to one
            int days1 = newDistance.duration.toHoursPart() < 2 ? 1 : max.days;
            int days2 = newDistance2.duration.toHoursPart() < 2 ? 1 : max.days;

            int mealsReduction1 = 4 * days1 > maxMeals ? maxMeals : 4 * days1;
            int mealsReduction2 = 4 * days2 > maxMeals ? maxMeals : 4 * days2;

            mealsReduction1 = max.mealsReduction > mealsReduction1 ? mealsReduction1 : max.mealsReduction;
            mealsReduction2 = max.mealsReduction > mealsReduction2 ? mealsReduction2 : max.mealsReduction;

            Delegation split = new Delegation(newDistance, max.days, mealsReduction1);
            Delegation split2 = new Delegation(newDistance2, max.days, mealsReduction2);
            delegations.add(split);
            delegations.add(split2);
        } catch (IndexOutOfBoundsException e) {
            return delegations;
        }

        delegations.remove(max);

        return delegations;
    }

    /**
     * Run genetic algorithm on generated population in search for better solution.
     * Stop when fitness < epsilon or time runs out.
     * 
     * Probabilities used:
     * 
     * -crossover: -- delegationCrossover = 30% -- daysCrossover = 35% --
     * mealsCrossover = 35%
     * 
     * -mutation: (15%) -- daysMutation = 40% -- mealsMutation = 40% --
     * mergeMutation = 10% -- splitMutation = 10%
     * 
     * @param milliseconds
     * @param epsilon
     * @return Best solution found
     */
    public ArrayList<Delegation> solve(int milliseconds, double epsilon) {

        SecureRandom rand = new SecureRandom();
        long startTime = System.nanoTime();
        long endTime = System.nanoTime();

        while ((endTime - startTime) / 1000000 < milliseconds + 1) {
            double[] fitnesses = calculateFitnesses();

            if (bestFitness < epsilon) {
                break;
            }

            ArrayList<ArrayList<Delegation>> newPopulation = new ArrayList<ArrayList<Delegation>>(populationSize);

            for (int i = 0; i < populationSize / 2; i++) {
                ArrayList<Delegation> parent1 = population.get(linearTournament(fitnesses));
                ArrayList<Delegation> parent2 = population.get(linearTournament(fitnesses));

                int crossover = rand.nextInt(100);
                ArrayList<ArrayList<Delegation>> children;

                if (crossover < 30) {
                    children = delegationCrossover(parent1, parent2);
                } else if (crossover >= 30 && crossover < 65) {
                    children = daysCrossover(parent1, parent2);
                } else {
                    children = mealsCrossover(parent1, parent2);
                }

                newPopulation.add(children.get(0));
                newPopulation.add(children.get(1));
            }

            int mutation = rand.nextInt(100);

            if (mutation < 15) {

                for (int i = rand.nextInt(5); i < 5; i++) {
                    mutation = rand.nextInt(100);

                    if (mutation < 10) {
                        mutation = rand.nextInt(populationSize);
                        newPopulation.set(mutation, splitMutation(newPopulation.get(mutation)));
                    } else if (mutation >= 10 && mutation < 50) {
                        mutation = rand.nextInt(populationSize);
                        ArrayList<Delegation> mutatingSolution = newPopulation.get(mutation);
                        int delegationIndex = rand.nextInt(mutatingSolution.size());
                        mutatingSolution.set(delegationIndex, daysMutation(mutatingSolution.get(delegationIndex)));
                        newPopulation.set(mutation, mutatingSolution);
                    } else if (mutation >= 50 && mutation < 90) {
                        mutation = rand.nextInt(populationSize);
                        ArrayList<Delegation> mutatingSolution = newPopulation.get(mutation);
                        int delegationIndex = rand.nextInt(mutatingSolution.size());
                        mutatingSolution.set(delegationIndex, mealsMutation(mutatingSolution.get(delegationIndex)));
                        newPopulation.set(mutation, mutatingSolution);
                    } else {
                        mutation = rand.nextInt(populationSize);
                        newPopulation.set(mutation, mergeMutation(newPopulation.get(mutation)));
                    }
                }
            }

            population = newPopulation;
            endTime = System.nanoTime();
        }

        return bestSolution;
    }

}