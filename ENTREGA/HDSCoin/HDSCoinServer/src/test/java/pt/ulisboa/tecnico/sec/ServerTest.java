package pt.ulisboa.tecnico.sec;


import com.sun.org.apache.xml.internal.security.Init;
import org.junit.After;
import org.junit.Before;
import pt.ulisboa.tecnico.sec.Account;
import pt.ulisboa.tecnico.sec.Block;
import pt.ulisboa.tecnico.sec.Ledger;
import pt.ulisboa.tecnico.sec.Transaction;
import java.util.ArrayList;

public abstract class ServerTest{
    Ledger ledger = Ledger.getInstance();

    @Before
    public void setUp(){
        Ledger ledger = Ledger.getInstance();
        //ledger.blockchain = new ArrayList<Block>();
        ledger.accounts = new ArrayList<Account>();
        ledger.backlog = new ArrayList<Transaction>();
        Init.init();
    }

    @After
    public void tearDown(){
        ledger = null;
    }


}
