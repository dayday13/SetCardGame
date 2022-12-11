package bguspl.set.ex;

import java.util.ArrayList;
import java.util.LinkedList;
//Imported this classes
import java.util.List;
//Imported this classes
import java.util.Queue;

import bguspl.set.Env;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    //Added this classes
    private boolean changeAfterPenalty;
    private Dealer dealer;
    private Queue<Integer> cardsSelected;

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    private Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        
        //added this
        this.dealer = dealer;
        cardsSelected = new LinkedList<Integer>();
    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
        if (!human) createArtificialIntelligence();

        while (!terminate) {
            // TODO implement main player loop
            
        }
        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}
        System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very very smart AI (!)
        aiThread = new Thread(() -> {
            System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
            while (!terminate) {
                // TODO implement player key press simulator
                try {
                    synchronized (this) { wait(); }
                } catch (InterruptedException ignored) {}
            }
            System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        // TODO implement, changed it

        if (cardsSelected.contains(slot)){
            cardsSelected.remove(slot);
            table.removeToken(this.id,slot);
            
        }
        else {
            if (cardsSelected.size() < 3) {
                table.placeToken(this.id, slot);
                cardsSelected.add(slot);

            }
        }


    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        // TODO implement, changed it thinks im finished
        int ignored = table.countCards(); // this part is just for demonstration in the unit tests
        clearTokens();
        env.ui.setScore(id, ++score);
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        // TODO implement, changed it not finished

        
            long currentPenalty = System.currentTimeMillis();
            while (System.currentTimeMillis() - currentPenalty < 4000)
                env.ui.setFreeze(this.id, 4000-(System.currentTimeMillis() - currentPenalty));
            env.ui.setFreeze(this.id, 0);
    }

    public int getScore() {
        return score;
    }

    //Added this functions

    public void clearTokens() { //Clears the tokens on the table.

        for (int i = 0; i < cardsSelected.size(); i ++) {
            //System.out.print("There: " + cardsSelected.size() + " tokens left.\n"); //Checks if tokens really removed
            table.removeToken(this.id, cardsSelected.remove());
        }

    }

    public Queue<Integer> tokenToSlots(){
        return cardsSelected;
    }
}
