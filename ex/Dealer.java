package bguspl.set.ex;

import bguspl.set.Env;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    //Added this
    private Thread[] playersThreads;
    private List<Integer> removeSlots;
    private boolean dealerBusy;



    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());

        //Added this
        this.dealerBusy = true;
        this.removeSlots = new LinkedList<Integer>();
        int numOfThreadsNeeded = env.config.humanPlayers + env.config.computerPlayers;
        this.playersThreads = new Thread[numOfThreadsNeeded];
        
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        env.logger.info("Thread " + Thread.currentThread().getName() + " starting.");
        createAndRunPlayersTherads();
        while (!shouldFinish()) {
            dealerBusy = true;
            updateTimerDisplay(true);
            placeCardsOnTable();
            timerLoop();
            updateTimerDisplay(false);
            removeAllCardsFromTable();
        }
        announceWinners();
        terminate();
        env.logger.info("Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            sleepUntilWokenOrTimeout();
            updateTimerDisplay(false);
            removeCardsFromTable();
            placeCardsOnTable();
            checksSetsValidation();
        }
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement,changed it
        for (int i=players.length-1; i>=0; i--)
        {
            players[i].terminate();
            playersThreads[i].interrupt();
        }
        this.terminate = true;
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeCardsFromTable() {
        // TODO implement, changed it
        if (!removeSlots.isEmpty()) {
            for (Integer slot : removeSlots)
                removeOneCard(slot);
            this.removeSlots.clear();
        }
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        // TODO implement, changed it
        //Finished

        boolean placeCard = false;
        while (table.countCards() < env.config.tableSize && deck.size() !=0) {
            placeCard = true;
            Collections.shuffle(deck);
            int avalibleSlot = 0;
            for (Integer slot : table.slotToCard) {
                if (slot != null) {
                avalibleSlot++;
                }
                else {
                    if (deck.size() > 0) {
                        table.placeCard(deck.remove(0), avalibleSlot);
                        avalibleSlot++;
                        try {
                            Thread.sleep(10);
                        } catch(Exception e) {}
                        
                    }
                    System.out.print("The deck contains " + deck.size() + " cards." + '\n'); //Just wanted to check if the cards are really removed from the deck
                }
            
            }
        }
        dealerBusy = false;
        if (placeCard && env.config.hints) { //Shows if there any legal sets left on the table
            table.hints();
        }

    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        // TODO implement,changed it
        synchronized (this) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }
        
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        // TODO implement,changed it
        if(reset)
            reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis;
        long timerDisplay = reshuffleTime - System.currentTimeMillis();
        if (timerDisplay > env.config.turnTimeoutWarningMillis)
            env.ui.setCountdown(timerDisplay, false);
        else
            env.ui.setCountdown(timerDisplay, true);
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // TODO implement, changed it
        // think its finished
        dealerBusy = true;
        for(Player player: players)
            player.setNeedsPenalty(true); //restart
        for(int i=0; i < env.config.tableSize; i++)
        {
            if(table.slotToCard[i] != null) {
                deck.add(table.slotToCard[i]);
                removeOneCard(i);
            }
        }
        for(Player player: players)
            player.setNeedsPenalty(false);
        
        dealerBusy = false;
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement, changed it
        //Finished
        int highestScore = 0;
        for(Player player:players)
        {
            if(highestScore < player.score())
            highestScore = player.score();
        }
        List<Integer> winnersList = new LinkedList<Integer>();
        for(Player player:players)
        {
            if (player.score() == highestScore)
                winnersList.add(player.id);
        }
        //convert winners list to array
        int[] winnerToAnnounced = new int[winnersList.size()];
        int i = 0;
        for ( Integer playerId: winnersList) {
            winnerToAnnounced[i] = playerId;
            i++;
        }

        env.ui.announceWinner(winnerToAnnounced);
    }

    //Added this functions

    private void checksSetsValidation() {

        try {
            Integer claimedPlayer = table.waitingPlayers.poll(1000, TimeUnit.MILLISECONDS);
            if (claimedPlayer != null) {
                List<Integer> claimedSetList = table.getCardsWithTokens(claimedPlayer);
                //Convert set list into an array
                int[] claimedSetArray = new int[claimedSetList.size()];
                int j = 0;
                for (Integer set: claimedSetList) {
                    claimedSetArray[j] = set;
                    j++;
                }

                if (claimedSetList.size() == env.config.featureSize) //All tokens are still on table
                {
                    System.out.println(Arrays.toString(claimedSetArray));
                    if (env.util.testSet(claimedSetArray)) { //Confirms this is a valid test
                        for (int i = 0; i < claimedSetArray.length; i++)
                            removeSlots.add(table.cardToSlot[claimedSetArray[i]]);
                        updateTimerDisplay(true);
                    } else {
                        players[claimedPlayer].setNeedsPenalty(true);
                    }
                }
                synchronized (players[claimedPlayer]) {
                    players[claimedPlayer].notifyAll();
                }
            }
        }
        catch (InterruptedException ignored) {}

    }

    public boolean isDealerBusy() {
        return dealerBusy;
    }

    private void removeOneCard(Integer slotToRemove) //Removes the card AND the token on it.
    {
        List<Integer> playersWithToken = new LinkedList<Integer>(table.tokens[slotToRemove]);
        for(int playerId : playersWithToken)
            players[playerId].removeToken(slotToRemove);
        table.removeCard(slotToRemove);
    }

    private void createAndRunPlayersTherads() {
        for(int i=0;i<playersThreads.length;i++)
            playersThreads[i] = new Thread(this.players[i], "Player number" + i);
        for(Thread nextThread: playersThreads)
            nextThread.start();
    }

}