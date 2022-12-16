package bguspl.set.ex;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import bguspl.set.Env;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

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

    //AddedThis

    private boolean needsPenalty;
    private BlockingQueue<Integer> playersActions;

    



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

        //Added this
        this.playersActions = new LinkedBlockingQueue<Integer>(env.config.featureSize);
        this.needsPenalty = false;
    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        System.out.println("Thread start" + this.id);
        playerThread = Thread.currentThread();
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + "starting.");
        if (!human) createArtificialIntelligence();

        while (!terminate) {
            // TODO implement main player loop,changed it

            try
            {
                Integer nextAction = playersActions.take();
                if(table.getSlotsWithTokens(id).contains(nextAction)) //If the player already putted a token on this slot, than remove it.
                    this.removeToken(nextAction);
                else {
                    if(table.getNumbersOfTokens(id) < env.config.featureSize) {
                        this.addToken(nextAction);
                        if(table.getNumbersOfTokens(id) == env.config.featureSize && !needsPenalty)
                        {
                            try{
                                table.waitingPlayers.put(this.id);}
                            catch (InterruptedException ignored) {}
                            synchronized (this) { //The first player who entered the waitingPlayer will be treated first!
                                try {
                                    wait();
                                } catch (InterruptedException ignored) {}
                                if(needsPenalty)
                                    penalty();
                                else
                                    point();
                            }
                        }
                    }
                }

            }
            catch (InterruptedException ignored) {}


        }
        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " starting.");
            while (!terminate) {
                // TODO implement player key press simulator,changed it
                Random x = new Random();
                int randomSlot = x.nextInt(env.config.tableSize);
                try {
                    playersActions.put(randomSlot);
                } catch (InterruptedException ignored) {}
            }
            env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement,changed it
        playersActions.clear();
        terminate = true;
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        // TODO implement,changed it

        System.out.println(slot);
        if(playersActions.size() < env.config.featureSize)
        {
            try{
                playersActions.put(slot);}
            catch (InterruptedException ignored) {}
        }
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        // TODO implement,changed it

        int ignored = table.countCards(); // this part is just for demonstration in the unit tests
        score++;
        env.ui.setScore(id, score);

        for(int i=0; i < env.config.pointFreezeMillis/1000;i++) //Makes the player to sleep for one secned while getting the point.
        {
            env.ui.setFreeze(this.id,env.config.pointFreezeMillis - (i * 1000));
            try { 
                playerThread.sleep(1000); 
            } catch (Exception e) {}
        }

        env.ui.setFreeze(this.id,0);
        playersActions.clear(); //Restart the player actions queue.
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        // TODO implement, changed it
        for(int i=0; i < env.config.penaltyFreezeMillis/1000;i++)
        {
            env.ui.setFreeze(this.id,env.config.penaltyFreezeMillis - (i * 1000));
            try { 
                playerThread.sleep(1000); 
            } catch (Exception e) {}
        }
        env.ui.setFreeze(this.id,0);
        playersActions.clear(); //Restart the player actions queue.
    }

    public int getScore() {
        return score;
    }

    //Added this functions
    
    public void addToken(Integer slot){
        if(!(env.config.tableSize + 1 > slot && slot > -1))
            throw new IllegalArgumentException("Slot is out of table size");
        table.placeToken(this.id,slot);

    }

    public void removeToken(Integer slot)
    {
        setNeedsPenalty(false);
        if(!(env.config.tableSize + 1 > slot && slot > -1))
            throw new IllegalArgumentException("Slot is out of table size");
        table.removeToken(this.id,slot);
    }

    public void setNeedsPenalty(boolean setTo){
        this.needsPenalty = setTo;
    }

}
