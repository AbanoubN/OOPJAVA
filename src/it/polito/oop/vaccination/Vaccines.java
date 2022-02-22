package it.polito.oop.vaccination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Vaccines {

    Map<String,Person> persons = new TreeMap<>();
    Map<String,VacinationHup> vacinationHups = new TreeMap<>();
    List<Integer> noHours = new ArrayList<>();
    List<Interval> intervals = new ArrayList<>();
    Map<Integer,Person> allocationPlan = new TreeMap<>();
    BiConsumer<Integer, String> lsnr = null;



    public final static int CURRENT_YEAR = java.time.LocalDate.now().getYear();

    // R1
    /**
     * Add a new person to the vaccination system.
     *
     * Persons are uniquely identified by SSN (italian "codice fiscale")
     *
     * @param first first name
     * @param lastName last name
     * @param ssn italian "codice fiscale"
     * @param year birth year
     * @return {@code false} if ssn is duplicate,
     */
    public boolean addPerson(String first, String lastName, String ssn, int year) {
        if (persons.containsKey(ssn))
        return false;

        persons.put(ssn,new Person(first,lastName,ssn,year));
        return true;
    }

    /**
     * Count the number of people added to the system
     *
     * @return person count
     */
    public int countPeople() {

        return persons.size();
    }

    /**
     * Retrieves information about a person.
     * Information is formatted as ssn, last name, and first name
     * separate by {@code ','} (comma).
     *
     * @param ssn "codice fiscale" of person searched
     * @return info about the person
     */
    public String getPerson(String ssn) {

        return persons.get(ssn).toString();
    }

    /**
     * Retrieves of a person given their SSN (codice fiscale).
     *
     * @param ssn "codice fiscale" of person searched
     * @return age of person (in years)
     */
    public int getAge(String ssn) {

        return CURRENT_YEAR - persons.get(ssn).getYear();
    }

    /**
     * Define the age intervals by providing the breaks between intervals.
     * The first interval always start at 0 (non included in the breaks)
     * and the last interval goes until infinity (not included in the breaks).
     * All intervals are closed on the lower boundary and open at the upper one.
     * <p>
     * For instance {@code setAgeIntervals(40,50,60)}
     * defines four intervals {@code "[0,40)", "[40,50)", "[50,60)", "[60,+)"}.
     *
     * @param breaks the array of breaks
     */
    public void setAgeIntervals(int... breaks) {
        intervals.add(new Interval(0 ,breaks[0]));
        for (int i = 0; i < breaks.length - 1; i++) {
            intervals.add( new Interval(breaks[i],breaks[i+1]));
        }
        intervals.add(new Interval(breaks[breaks.length-1 ],Integer.MAX_VALUE));
    }


    /**
     * Retrieves the labels of the age intervals defined.
     *
     * Interval labels are formatted as {@code "[0,10)"},
     * if the upper limit is infinity {@code '+'} is used
     * instead of the number.
     *
     * @return labels of the age intervals
     */
    public Collection<String> getAgeIntervals() {
        return intervals.stream().map(interval -> interval.toString()).collect(Collectors.toList());
    }

    /**
     * Retrieves people in the given interval.
     *
     * The age of the person is computed by subtracting
     * the birth year from current year.
     *
     * @param interval age interval label
     * @return collection of SSN of person in the age interval
     */
    public Collection<String> getInInterval(String interval) {

       List<String> intervals = persons.values().stream()
               .filter(person -> person.getAgegroup(this.intervals).toString().equals(interval))
               .map(person -> person.getSsn())
               .collect(Collectors.toList());

        return intervals;
    }

    // R2
    /**
     * Define a vaccination hub
     *
     * @param name name of the hub
     * @throws VaccineException in case of duplicate name
     */
    public void defineHub(String name) throws VaccineException {
        if (vacinationHups.containsKey(name))
            throw new VaccineException("duplicate name");

        vacinationHups.put(name,new VacinationHup(name));
    }

    /**
     * Retrieves hub names
     *
     * @return hub names
     */
    public Collection<String> getHubs() {

        return vacinationHups.keySet();
    }

    /**
     * Define the staffing of a hub in terms of
     * doctors, nurses and other personnel.
     *
     * @param name name of the hub
     * @param countDoctors number of doctors
     * @param nNurses number of nurses
     * @param other number of other personnel
     * @throws VaccineException in case of undefined hub, or any number of personnel not greater than 0.
     */
    public void setStaff(String name, int countDoctors, int nNurses, int other) throws VaccineException {
        if (!vacinationHups.containsKey(name) || countDoctors  <1
        || nNurses < 1 || other < 1)
            throw new VaccineException("vac hup");
        vacinationHups.get(name).setCountDoctors(countDoctors);
        vacinationHups.get(name).setnNurses(nNurses);
        vacinationHups.get(name).setOther(other);
        return;
    }

    /**
     * Estimates the hourly vaccination capacity of a hub
     *
     * The capacity is computed as the minimum among
     * 10*number_doctor, 12*number_nurses, 20*number_other
     *
     * @param hub name of the hub
     * @return hourly vaccination capacity
     * @throws VaccineException in case of undefined or hub without staff
     */
    public int estimateHourlyCapacity(String hub) throws VaccineException {
        if (vacinationHups.get(hub) == null || vacinationHups.get(hub).capacity() ==0)
            throw new VaccineException();
        return vacinationHups.get(hub).capacity();
    }

    // R3
    /**
     * Load people information stored in CSV format.
     *
     * The header must start with {@code "SSN,LAST,FIRST"}.
     * All lines must have at least three elements.
     *
     * In case of error in a person line the line is skipped.
     *
     * @param people {@code Reader} for the CSV content
     * @return number of correctly added people
     * @throws IOException in case of IO error
     * @throws VaccineException in case of error in the header
     */
    public long loadPeople(Reader people) throws IOException, VaccineException {
        // Hint:
        BufferedReader br = new BufferedReader(people);
        if (br == null)
            throw new IOException();
        String s = br.readLine();
        if ( !(s.equals("SSN,LAST,FIRST,YEAR"))) {
            if(lsnr!= null) lsnr.accept(1,s);
            throw new VaccineException("error header");
        }
        String line ;
        int count = 1;
        while ((line = br.readLine()) != null){
            count++;
            List<String> lineRead = Arrays.stream(line.split(",")).collect(Collectors.toList());
            if (persons.containsKey(lineRead.get(0)) || lineRead.size() != 4) {
               if(lsnr!= null) lsnr.accept(count,line);
                continue;
            }
            persons.put(lineRead.get(0),new Person(lineRead.get(0),lineRead.get(1),lineRead.get(2),Integer.parseInt(lineRead.get(3))));
        }
        return count;
    }

    // R4
    /**
     * Define the amount of working hours for the days of the week.
     *
     * Exactly 7 elements are expected, where the first one correspond to Monday.
     *
     * @param hs workings hours for the 7 days.
     * @throws VaccineException if there are not exactly 7 elements or if the sum of all hours is less than 0 ore greater than 24*7.
     */
    public void setHours(int... hs) throws VaccineException {
        if (hs.length != 7 || Arrays.stream(hs).max().getAsInt() > 12)
            throw new VaccineException("wrong hours");
        for (int x:
             hs) {
            noHours.add(x);
        }
    }

    /**
     * Returns the list of standard time slots for all the days of the week.
     *
     * Time slots start at 9:00 and occur every 15 minutes (4 per hour) and
     * they cover the number of working hours defined through method {@link #setHours}.
     * <p>
     * Times are formatted as {@code "09:00"} with both minuts and hours on two
     * digits filled with leading 0.
     * <p>
     * Returns a list with 7 elements, each with the time slots of the corresponding day of the week.
     *
     * @return the list hours for each day of the week
     */
    public List<List<String>> getHours() {
        List<List<String>> timeSlots = new ArrayList<>();
        int cuhour = 9 ;
        for (int i = 0; i <7; i++) {
            List<String>  temp = new ArrayList<>();
            int hours = noHours.get(i);
            for (int j = 0; j < hours; j++) {
                String hs = "0";
                cuhour = 9+j;
                if (cuhour < 10)
                    hs = "0" + cuhour;
                else
                    hs = ""+ cuhour;
                temp.add(hs +":00");
                temp.add(hs + ":15");
                temp.add(hs + ":30");
                temp.add(hs+ ":45");
            }
            timeSlots.add(temp);
        }

        return timeSlots;
    }

    /**
     * Compute the available vaccination slots for a given hub on a given day of the week
     * <p>
     * The availability is computed as the number of working hours of that day
     * multiplied by the hourly capacity (see {@link #estimateCapacity} of the hub.
     *
     * @return
     */
    public int getDailyAvailable(String hub, int d) {
        return noHours.get(d)*vacinationHups.get(hub).capacity();
    }

    /**
     * Compute the available vaccination slots for each hub and for each day of the week
     * <p>
     * The method returns a map that associates the hub names (keys) to the lists
     * of number of available hours for the 7 days.
     * <p>
     * The availability is computed as the number of working hours of that day
     * multiplied by the capacity (see {@link #estimateCapacity} of the hub.
     *
     * @return
     */
    public Map<String, List<Integer>> getAvailable() {
        Map<String, List<Integer>> available = new TreeMap<>();
        for (String s : vacinationHups.keySet())
        {
            int temp = vacinationHups.get(s).capacity();
            available.put(s ,noHours.stream().map(o -> o * temp).collect(Collectors.toList()));
        }
        return available;
    }

    /**
     * Computes the general allocation plan a hub on a given day.
     * Starting with the oldest age intervals 40%
     * of available places are allocated
     * to persons in that interval before moving the the next
     * interval and considering the remaining places.
     * <p>
     * The returned value is the list of SSNs (codice fiscale) of the
     * persons allocated to that day
     * <p>
     * <b>N.B.</b> no particular order of allocation is guaranteed
     *
     * @param hub name of the hub
     * @param d day of week index (0 = Monday)
     * @return the list of daily allocations
     */
    public List<String> allocate(String hub, int d) {
        int noofSlots = getDailyAvailable(hub,d);
        int maxSlots = noofSlots;
        List<String> alocSSN = new ArrayList<>();
        intervals = intervals.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        for (Person p :
                persons.values()) {
            p.getAgegroup(intervals);
        }
        for (int i = 0; i < intervals.size(); i++) {
            alocSSN.addAll(allocateAgeGroup(intervals.get(i), (int) (noofSlots*0.4), hub,d));
            noofSlots = maxSlots-alocSSN.size();
        }
        alocSSN.addAll(allocateAgeGroup(intervals.get(0),noofSlots, hub,d));
        return alocSSN;
    }

    public List<String> allocateAgeGroup(Interval interval,int noofSlots,String hub,int d){
        List<String> alocSSN = persons.values().stream().filter(person -> person.getAgegroup(intervals) == interval && person.allocated == false).limit(noofSlots).map(Person::getSsn).collect(Collectors.toList());
        persons.values().stream().filter(p -> alocSSN.contains(p.getSsn())).forEach(p-> {p.allocated =true;p.hupAssigend = hub;p.dayAssigned=d;});
        return alocSSN;
    }
    /**
     * Removes all people from allocation lists and
     * clears their allocation status
     */
    public void clearAllocation() {
       persons.values().stream().forEach(person ->person.allocated = false);
    }

    /**
     * Computes the general allocation plan for the week.
     * For every day, starting with the oldest age intervals
     * 40% available places are allocated
     * to persons in that interval before moving the the next
     * interval and considering the remaining places.
     * <p>
     * The returned value is a list with 7 elements, one
     * for every day of the week, each element is a map that
     * links the name of each hub to the list of SSNs (codice fiscale)
     * of the persons allocated to that day in that hub
     * <p>
     * <b>N.B.</b> no particular order of allocation is guaranteed
     * but the same invocation (after {@link #clearAllocation}) must return the same
     * allocation.
     *
     * @return the list of daily allocations
     */
    public List<Map<String, List<String>>> weekAllocate() {
        for (String hubname: vacinationHups.keySet()) {
            for (int i = 0; i < 7 ; i++) {
                allocate(hubname,i); } }

        List<Map<String, List<String>>>  weeklyPlan= new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, List<String>> hupList = new TreeMap<>();
            for (String hubname:
                vacinationHups.keySet() ) {
            hupList.put(hubname, allocatedToHub(hubname,i));
            }
            weeklyPlan.add(hupList);
        }
        return weeklyPlan;
    }
    public List<String> allocatedToHub(String name , int d){
        return persons.values().stream()
                .filter(p -> p.allocated == true && p.hupAssigend.equals(name) && p.dayAssigned == d)
                .map(Person::getSsn).collect(Collectors.toList());
    }

    // R5
    /**
     * Returns the proportion of allocated people
     * w.r.t. the total number of persons added
     * in the system
     *
     * @return proportion of allocated people
     */
    public double propAllocated() {
        double count = 0;
        for (Person p :
                persons.values()) {
            if (p.allocated)
                count++;
        }
        return count/(double) countPeople();
    }

    /**
     * Returns the proportion of allocated people
     * w.r.t. the total number of persons added
     * in the system, divided by age interval.
     * <p>
     * The map associates the age interval label
     * to the proportion of allocates people in that interval
     *
     * @return proportion of allocated people by age interval
     */
    public Map<String, Double> propAllocatedAge() {
        Map<String, Double> propAlloc =new TreeMap<>();
        for (Interval i: intervals) {
            List<Person> c = persons.values().stream().filter(p->p.getAgegroup(intervals) == i).collect(Collectors.toList());
            double x = c.stream().filter(p->p.allocated==true).count();
            propAlloc.put(i.toString(),x/c.size() ); }
        return propAlloc;
    }
    /**
     * Retrieves the distribution of allocated persons
     * among the different age intervals.
     * <p>
     * For each age intervals the map reports the
     * proportion of allocated persons in the corresponding
     * interval w.r.t the total number of allocated persons
     *
     * @return
     */

    public Map<String, Double> distributionAllocated() {
        Map<String, Double> propAlloc =new TreeMap<>();
        for (Interval i: intervals) {
            double x = persons.values().stream().filter(p->p.getAgegroup(intervals) == i && p.allocated ==true).count();
            double y = persons.values().stream().filter(o->o.allocated ==true).count();
            propAlloc.put(i.toString(),x/y ); }
        return propAlloc;    }

    // R6
    /**
     * Defines a listener for the file loading method.
     * The {@ accept()} method of the listener is called
     * passing the line number and the offending line.
     * <p>
     * Lines start at 1 with the header line.
     *
     * @param lsnr the listener for load errors
     */
    public void setLoadListener(BiConsumer<Integer, String> lsnr) {
        this.lsnr = lsnr;
    }
}
