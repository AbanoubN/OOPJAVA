package it.polito.oop.vaccination;

public class VacinationHup {
    String name;
    int countDoctors;
    int nNurses;
    int other;

    public VacinationHup(String name) {
        this.name = name;
    }

    public int getCountDoctors() {
        return countDoctors;
    }

    public void setCountDoctors(int countDoctors) {
        this.countDoctors = countDoctors;
    }

    public int getnNurses() {
        return nNurses;
    }

    public void setnNurses(int nNurses) {
        this.nNurses = nNurses;
    }

    public int getOther() {
        return other;
    }

    public void setOther(int other) {
        this.other = other;
    }

    public int capacity() {
        return Integer.min(Integer.min(10*countDoctors,12*nNurses),20*other );
    }

}
