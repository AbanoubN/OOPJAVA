package it.polito.oop.vaccination;

import java.util.List;

public class Person {
    private String first;
    private String lastName;
    private String ssn;
    private int year;

    Interval agegroup;
    boolean allocated;
    String hupAssigend;
    int dayAssigned;

    public Interval getAgegroup(List<Interval> intervals) {
        int age = Vaccines.CURRENT_YEAR - year;
        for (Interval i:
             intervals) {
            if (i.isIn(age))
                agegroup = i;
        }
        return agegroup;
    }

    public void setAgegroup(Interval agegroup) {
        this.agegroup = agegroup;
    }

    public Person(String first, String lastName, String ssn, int year) {
        this.first = first;
        this.lastName = lastName;
        this.ssn = ssn;
        this.year = year;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return   ssn + ", " +
                lastName + ", "+
                first;
    }
}
