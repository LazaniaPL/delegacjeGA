package main.models;

public class Distance implements Comparable<Distance>{
    public double kilometres;
    public int start;
    public int end;
    public String startName;
    public String endName;

    public Distance(double kilometres, int start, int end, String startName, String endName){
        this.kilometres = kilometres;
        this.start = start;
        this.end = end;
        this.startName = startName;
        this.endName = endName;
    }

    @Override
    public int compareTo(Distance o) {
        return Double.compare(this.kilometres,o.kilometres);
    }

    @Override
    public boolean equals(Object obj) {
        return this.start == ((Distance)obj).start && this.end == ((Distance)obj).end;
    }

}
