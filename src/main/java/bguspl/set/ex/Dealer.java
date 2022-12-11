package bguspl.set.ex;

import bguspl.set.Env;

/*i imported Collections */
import java.util.Collections;
import java.util.*;
/*i imported Collections */

import java.util.List;
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

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
        while (!shouldFinish()) {
    
        /*Added this lines */
        reshuffleTime = System.currentTimeMillis() + 61000;
        try { // Makes the card to be putten slower on the table
            placeCardsOnTable();
        } catch (Exception e){}
        /*Added this lines */
        timerLoop();
        //Added this line
        Arrays.stream(players).forEach(Player::clearTokens);
        //
        updateTimerDisplay(false);
        removeAllCardsFromTable();
            
        }
        announceWinners();
        System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            sleepUntilWokenOrTimeout();
            updateTimerDisplay(false);
            removeCardsFromTable();
            
            //changed this line 11/12/2022
            try {
                placeCardsOnTable();
            } catch (Exception e){}
            

            //Added this lines.
            tokensValidation();
        }
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement
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
     * Checks if any cards should be removed from the table and returns them to the deck.
     */
    private void removeCardsFromTable() {
        // TODO implement
        // Removes the cards the player picked if they're are set
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() throws InterruptedException {
        //TODO implement, changed it
        //How would he know ? if there any empty space on the table
        //Think im finished, could have created a method that will return all the avalible slots.
        Collections.shuffle(deck);
        int avalibleSlot = 0;
        for (Integer slot : table.slotToCard) {
            if (slot != null) {
                avalibleSlot++;
            }
            else {
                if (deck.size() > 0) {
                    Thread.sleep(200);
                    table.placeCard(deck.remove(0), avalibleSlot);
                    avalibleSlot++;
                    //System.out.print(deck.size()); // just wanted to check if the cards really removed from the deck
                }
            }
        }
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        // TODO implement
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        // TODO implement, changed it
        //not finished, need to change
        env.ui.setCountdown(reshuffleTime-System.currentTimeMillis(), reset);

    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // TODO implement, changed it
         for (int sloth: table.slotToCard) {
             if (table.slotToCard[sloth] != null) {
                 deck.add(table.slotToCard[sloth]);
                 table.removeCard(sloth);
             }
         }
        
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement
    }

    //Added this functions

    private void tokensValidation() { //Checks if cards selected performe a valid set
        for (Player p : players) {
            if (p.tokenToSlots().size() == 3) {
                if (CheckIfSetIsValid(p.tokenToSlots())) {
                    removeCardsBySlots(p.tokenToSlots());
                    p.point();
                    updateTimerDisplay(true);
                    p.clearTokens();
                } else {
                    p.penalty();
                }
            }
        }
        try {
            placeCardsOnTable();
        } catch (Exception e) {}
    }

    private boolean CheckIfSetIsValid(Queue<Integer> cardsSelected) {
        int[] setToTestArray = new int[3];
        int i = 0;
        Iterator<Integer> cardsSelectedIterator = cardsSelected.iterator();
        while (cardsSelectedIterator.hasNext() && i < 3) {
            setToTestArray[i] = cardsSelectedIterator.next();
            System.out.println(setToTestArray[i]); //Shows what is in the queue
            i++;
        }
        return env.util.testSet(setToTestArray);
    }

    private void removeCardsBySlots(Queue<Integer> slots){
        Iterator<Integer> sIterator = slots.iterator();
        while (sIterator.hasNext())
            table.removeCard(sIterator.next());
    }
}
