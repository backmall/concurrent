/**
 * Class representing Donations made by Viewers to Players
 * @author DAVID
 */
public class Donation {
    private final Viewer viewer;
    private final Player player;
    private final double amount;

    public Donation(Viewer from, Player to, double amount){
        this.viewer = from;
        this.player = to;
        this.amount = amount;
    }

    public Viewer getViewer() {
        return viewer;
    }

    public Player getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Donation of " + amount + " from " + viewer + " to " + player;
    }

}