package main.models;

public class Delegation{

    public Distance distance;
    public int days;
    public int mealsReduction;

    public Delegation(Distance distance, int days, int mealsReduction){
        this.distance = distance;
        this.days = days;
        this.mealsReduction = mealsReduction;
    }

    public double delegationCost(){
        return 2.0*distance.kilometres*Prices.perKilometre + days*Prices.perDay - Prices.oneNightReduction - mealsReduction*Prices.perMeal;
    }

    public String toString(){
        return "start: " + distance.startName + "; end: " + distance.endName + "; km: " + distance.kilometres
                + "; days: " + days + "; meals: " + mealsReduction;
    }

}
