package bguspl.set.ex;

import bguspl.set.Config;
import bguspl.set.Env;
import bguspl.set.UserInterface;
import bguspl.set.Util;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealerTest {

    Dealer dealer;
    Env env;
    @Mock
    Util util;
    @Mock
    private UserInterface ui;
    @Mock
    private Table table;
    @Mock
    private Player[] players;
    @Mock
    private Logger logger;
    @Mock
    private Config config;
    @Mock
    private List<Integer> deck;
    @Mock
    private Player player;
        
    @BeforeEach
    void setUp() {
        // purposely do not find the configuration files (use defaults here).
        env = new Env(logger, config, ui, util);
        dealer = new Dealer(env, table, players);
        player = new Player(env, dealer, table, 0, false);
        players[0] = player;
    }

    private void setDealerBusy(boolean state){

        dealer.dealerBusy = state; //setting dealerBusy field for testing

    }

    @Test
    void isDealerBusy(){
        assertTrue(dealer.isDealerBusy());

        setDealerBusy(false);

        assertFalse(dealer.isDealerBusy());

    }

    @Test
    void shouldFinish(){

        assertFalse(dealer.shouldFinish2());

        when(env.util.findSets(deck, 1).size()).thenReturn(0);

    }
}