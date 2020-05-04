
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.*;

/**
 * Game class stores lists of Players, Viewers, Donations and viewing times
 * @author DAVID
 */
public class Game {
    /* Collections stored within Game object */
    private ArrayList<Player> players;
    private ArrayList<Viewer> viewers;
    private ArrayList<Donation> donations;
    private HashMap<Player,ArrayList<Long>> times;
    DecimalFormat round = new DecimalFormat("0.00");

    /* Properties of the Game */
    private String name;
    private boolean running;
    private double totalDonationsRecordedByGame;
    //private long totalViewingTimeRecordedByGame;
    private AtomicLong totalViewingTimeRecordedByGame;
    //private int totalNumberViewing;
    private AtomicInteger totalNumberViewing;

    private Random random;
    private GUI gui;

    /* Game construtor */
    public Game(GUI gui, String name){
        this.gui = gui;
        this.name =name;
        this.players = new ArrayList();
        this.viewers = new ArrayList();
        this.donations = new ArrayList();
        this.times = new HashMap();
        this.totalDonationsRecordedByGame = 0;
        this.random = new Random();

        this.totalViewingTimeRecordedByGame = new AtomicLong(0);
        this.totalNumberViewing = new AtomicInteger(0);

        initialise();
    }

    /* Method to assign players to Game, called by constructor */
    public void initialise(){
        players.add(new Player("Scarlett"));
        players.add(new Player("David"));
        players.add(new Player("Thor"));
        players.add(new Player("Cleo"));
        System.out.println("Game" + players);
        for(Player p: players) times.put(p, new ArrayList());
        running = true;
    }

    /* User by viewers when switching Players they are viewing */
    public Player getRandomPlayer(){
        return players.get(random.nextInt(players.size()));
    }

    /* processes a donation that comes from a Viewer */
    public synchronized void processDonation(Donation donation){
        totalDonationsRecordedByGame += donation.getAmount();
        donations.add(donation);
        donation.getPlayer().addDonation(donation);
    }

    /* processes a record of time spent viewing a Player that comes from a Viewer */
    //public void recordViewingTime(Player p, long time){
    //    totalViewingTimeRecordedByGame += time;
    //    times.get(p).add(time);
    //}

    //thread safe option
    public void recordViewingTime(Player p, long time){
        while(true){
            long existingValue = getTotalViewingTimeRecordedByGame();
            long newValue = existingValue + time;
            if(totalViewingTimeRecordedByGame.compareAndSet(existingValue, newValue)){
                times.get(p).add(time);
                return;
            }
        }
    }

    /* Basic methods for accessing Game properties */
    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<String> getPlayersNames() {
        ArrayList<String> names = new ArrayList();
        for(Player p: players) names.add(p.getPlayerName());
        return names;
    }

    public ArrayList<Viewer> getViewers() {
        return viewers;
    }

    public boolean isRunning() {
        return running;
    }

    public void stopGame(){
        this.running = false;
    }

    public void startGame(){
        this.running = true;
    }

    public ArrayList<Donation> getDonations() {
        return donations;
    }

    public double getTotalDonationsRecordedByGame() {
        return totalDonationsRecordedByGame;
    }

    //public long getTotalViewingTimeRecordedByGame() {
    //return totalViewingTimeRecordedByGame;
    //}

    //thread safe option
    public long getTotalViewingTimeRecordedByGame(){
        return totalViewingTimeRecordedByGame.get();
    }

    public HashMap<Player, ArrayList<Long>> getTimes() {
        return times;
    }

    //public int getTotalNumberViewing() {
    //    return totalNumberViewing;
    //}

    //public void viewerJoins(Viewer v) {
    //    totalNumberViewing++;
    //    viewers.add(v);
    //}

    //public void viewerLeaves(Viewer v) {
    //    totalNumberViewing--;
    //    viewers.remove(v);
    //}

    //thread safe option
    public int getTotalNumberViewing(){
        return totalNumberViewing.get();
    }

    public void viewerJoins(Viewer v){
        while(true){
            int existingValue = getTotalNumberViewing();
            int newValue = existingValue + 1;
            if(totalNumberViewing.compareAndSet(existingValue, newValue)){
                viewers.add(v);
                return;
            }
        }
    }

    public void viewerLeaves(Viewer v){
        while(true){
            int existingValue = getTotalNumberViewing();
            int newValue = existingValue - 1;
            if(totalNumberViewing.compareAndSet(existingValue, newValue)){
                viewers.remove(v);
                return;
            }
        }
    }


    /* Method that checks for consistency of viewing time data
     * Totals of recorded running total in Game, total of times in HashMap, and
     * total of times sent by Viewers should all agree if all data is processed safely
     */
    public void countTimes(){
        String timeResults = "";
        long sumOfPlayerTotalTimes = 0;
        for(Player p: times.keySet()){
            long totalTimeForPlayer = 0;
            for(Long time: times.get(p)){
                totalTimeForPlayer += time;
                sumOfPlayerTotalTimes += time;
            }
            timeResults += p + " and had " + totalTimeForPlayer/1000000 + "ms of views" +"\n";
        }

        timeResults += "\n Total viewing times recorded by Players: "
                + sumOfPlayerTotalTimes/1000000 + "ms \n" +
                "Total viewing times recorded by Game:    "
                + totalViewingTimeRecordedByGame.get()/1000000 + "ms\n" +
                "Total viewing times recorded by Viewers: "
                + Viewer.getTimeViewers()/1000000 + "ms\n";
        System.out.println(timeResults);
        gui.updateReport(timeResults);
    }

    /* Method that checks for consistency of donations
     * Totals of recorded donations in Game, total of donations held by Player, and
     * total of donations sent by Viewers should all agree if all data is processed safely
     */
    public void checkDonations(){
        String report = "";
        /* Donations counted by the Game class */
        double totalOfDonations = 0;
        for(Donation d: donations){
            totalOfDonations += d.getAmount();
        }
        report = "Donations counted by Game:    Number = "
                + donations.size() + " Value = "
                + round.format(totalOfDonations) + "\n";

        /* Sum of Donations counted by the Players */
        int numberDonationsToPlayers = 0;
        double totalOfDonationsToPlayers = 0;
        for(Player p: players){
            numberDonationsToPlayers += p.getNumOfDonations();
            totalOfDonationsToPlayers += p.sumDonations();
        }
        report += "Donations counted by Players: Number = "
                + numberDonationsToPlayers + " Value = "
                + round.format(totalOfDonationsToPlayers) + "\n";

        /* Sum of Donations sent by the Viewer class */
        report += "Donations counted by Viewers: Number = "
                + Viewer.getTotalNumberOfDonations() + " Value = "
                + round.format(Viewer.getTotalValueOfDonations())+ "\n\n";

        System.out.println(report);
        gui.updateReport(report);
    }

}