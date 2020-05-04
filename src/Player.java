
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Player object stores player name, number of active viewers, 
 * running totals of number and values of donations from viewers
 * also stores a record of all donations made to Player
 *
 * Player class keeps running total of all submitted viewing times
 * @author DAVID
 */
public class Player{
    private final String playerName;    /* Player name */
    //private int numViewers;             /* current number of viewers */
    //private int numOfDonations;         /* running total number of donations */
    private double sumOfDonations;      /* running total of value of donations */

    private HashMap<Viewer, Double> donationsFromViewers; /* record of donations */

    //private static long allTime;    /* class-level running total of all viewing times */

    //thread safe option
    private AtomicInteger numViewers;
    private AtomicInteger numOfDonations;
    private static AtomicLong allTime = new AtomicLong(0);

    /* Player constructor */
    public Player(String name) {
        this.playerName = name;
        //this.numViewers = 0;
        donationsFromViewers = new HashMap();
        //sumOfDonations = 0;

        //thread safe option
        this.numViewers = new AtomicInteger();
        this.numOfDonations = new AtomicInteger();
        //allTime = new AtomicLong(0);
    }

    //thread safe option
    public int getNumViewers(){
        return numViewers.get();
    }

    public void gainOneViewer(){
        numViewers.incrementAndGet();
    }

    public void loseOneViewer(){
        numViewers.decrementAndGet();
    }

    /* method for processing donation from a Viewer into Player records */
    public synchronized void addDonation(Donation donation){
        // Increment count of donations
        numOfDonations.incrementAndGet();

        // Add value to sum of donations
        this.sumOfDonations += donation.getAmount();
        Viewer donor = donation.getViewer();
        if(donationsFromViewers.containsKey(donor)){
            donationsFromViewers.replace(donor, donationsFromViewers.get(donor) + donation.getAmount());
        }
        else{
            donationsFromViewers.put(donor, donation.getAmount());
        }
    }

    /* method to get total of all donations in Players record of donations */
    public synchronized double sumDonations(){
        int sum = 0;
        for(Double v: donationsFromViewers.values()){
            sum += v;
        }
        return sum;
    }

    @Override public String toString(){
        return playerName + " received " + this.numOfDonations + " donations, worth " + sumDonations() ;
    }

    /* Basic getter method for player name */
    public String getPlayerName() {
        return playerName;
    }

    /* method to return reference to HashMap of donation records made to this Player */
    public HashMap<Viewer, Double> getDonationsFromViewers() {
        return donationsFromViewers;
    }

    /* methods to access running totals of number and values of donations */
    //public int getNumOfDonations() {
    //    return numOfDonations;
    //}

    //thread safe option
    public int getNumOfDonations(){
        return numOfDonations.get();
    }

    public Double getSumOfDonations() {
        return sumOfDonations;
    }

    public static long getAllTime(){
        return allTime.get();
    }

    public static void addToAllTime(long allTimepar2){
        while(true){
            long existingValue = Player.allTime.get();
            long newValue = existingValue + allTimepar2;
            if(Player.allTime.compareAndSet(existingValue, newValue)){
                return;
            }
        }
    }
}