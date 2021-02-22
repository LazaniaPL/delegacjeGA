package main.models;

import java.security.SecureRandom;

public class Delegation {

    public Distance distance;
    public int days;
    public int mealsReduction;

    public Delegation(Distance distance, int days, int mealsReduction) {
        this.distance = distance;
        this.days = days;
        this.mealsReduction = mealsReduction;
    }

    public Delegation(Distance distance) {
        this.distance = distance;
        this.days = 0;
        this.mealsReduction = 0;
    }

    public double delegationCost() {
        return 2.0 * distance.kilometres * Prices.perKilometre + days * Prices.perDay - Prices.oneNightReduction
                - mealsReduction * Prices.perMeal;
    }

    /**
     * Checks if delegation is shorter than 2h journey if yes max days should be 1
     * else distance is ok
     * 
     * @param days
     */
    public void setDaysWithDistanceCheck(int days) {
        this.days = distance.duration.toHoursPart() < 2 ? 1 : days;
    }

    /**
     * Checks if current mealsReduction is lesser than maxMeals and 4*days and then
     * chooses new mealsReduction randomly between 0 and found max
     * 
     * @param maxMeals
     */
    public void setRandomMealReductionWithMaxCheck(int maxMeals) {
        SecureRandom rand = new SecureRandom();
        int max = 4 * days > maxMeals ? maxMeals + 1 : 4 * days + 1;
        mealsReduction = rand.nextInt(max);
    }

    /**
     * Checks if current mealsReduction is lesser than maxMeals and 4*days and then
     * checks if meals to set are lesser than found max if yes mealsReduction is set
     * to max else set meals
     * 
     * @param maxMeals
     * @param meals
     */
    public void setMealsReductionWithMaxCheck(int maxMeals, int meals) {
        int max = 4 * days > maxMeals ? maxMeals : 4 * days;
        meals = meals > max ? max : meals;
        mealsReduction = meals;
    }

    public String toString() {
        return "start: " + distance.startName + "; end: " + distance.endName + "; km: " + distance.kilometres
                + "; travel time: " + distance.duration.toHoursPart() + " h " + distance.duration.toMinutesPart()
                + " min" + "; days: " + days + "; meals: " + mealsReduction;
    }

}
