package bguspl.set.ex;

import bguspl.set.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealerTest {

    Dealer dealer;

    private Env env;
    private List<Integer> deck;

    @Mock
    private UserInterface ui;
    @Mock
    private Table table;
    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {

        Config config = new Config(logger, "config.properties");
        env = new Env(logger, config, ui, new UtilImpl(config));
        //env = new Env(logger, config, ui, util);
        Player[] players = new Player[0];
        dealer = new Dealer(env, table, players);

      //  players[0] = new Player(env, dealer, table, 0, false);
    }

    private void setDealerBusy(boolean state){

        dealer.dealerBusy = state; //setting dealerBusy field for testing

    }

    @Test
    void isDealerBusy(){
        //when created dealer should be busy
        assertTrue(dealer.isDealerBusy());

        //changing dealer to not busy
        setDealerBusy(false);

        //should return that dealer isn't busy
        assertFalse(dealer.isDealerBusy());

        //returning dealer to its right state
        setDealerBusy(true);
    }


    @Test
    void terminate() {

        // needs to return false
        assertFalse(dealer.shouldFinishTest());

        // setting dealer's terminate field to true
        dealer.terminate();

        // needs to return true
        assertTrue(dealer.shouldFinishTest());

    }

}