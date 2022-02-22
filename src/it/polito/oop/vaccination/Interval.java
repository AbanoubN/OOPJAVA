package it.polito.oop.vaccination;

public class Interval implements Comparable<Interval> {
    int start;
    int end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean  isIn(int age){
        if(age >= start && age< end)
            return true;
        return false;
    }

    @Override
    public int compareTo(Interval o) {
        return start - o.start;
    }

    @Override
    public String toString() {
        String  fend = String.format("%02d", end);
        if (end == Integer.MAX_VALUE)
            fend = "+";


        return "["+ String.valueOf(start) +
                "," + fend+
                ')';
    }
}
