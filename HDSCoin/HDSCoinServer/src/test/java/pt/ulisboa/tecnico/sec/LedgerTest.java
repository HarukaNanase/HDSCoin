package pt.ulisboa.tecnico.sec;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LedgerTest extends ServerTest {
    private int INITIAL_BALANCE = 50;
    private String ACCOUNT_ADDRESS_1 = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIPZkX+W7MyU3DXKa8Yd8oryMXkaunjYRGRPrK3LoYFhwSSYKpvx3yOmb2uN86Va8LI+LqJxgYHWby6m7XL45Y8CAwEAAQ==";
    private String ACCOUNT_ADDRESS_2 = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMxLYADb0fArf6vD17na1WORpKReSzigGHm+ZgAt0C5SiOxm/ReNyRH+HXuX/OxuRwPBm6FvTNyDju/3sVck9W0CAwEAAQ==";


    @Test
    public void LedgerCreationTest(){
        assertEquals(ledger.accounts.size(), 0);
        assertEquals(ledger.backlog.size(), 0);
        assertEquals(ledger.blockchain.size(), 0);
    }

    @Test
    public void LedgerAccountCreationTest(){
        ledger.createAccount(ACCOUNT_ADDRESS_1);
        assertEquals(ledger.accounts.size(), 1);
    }

    @Test
    public void LedgerCheckAccountNotFoundTest(){
        String response = ledger.checkAccount(ACCOUNT_ADDRESS_1);
        assertEquals(response, "Account not found. \n");
    }

    @Test
    public void LedgerCheckAccountWorkingTest(){
        ledger.createAccount(ACCOUNT_ADDRESS_1);
        assertNotEquals(ledger.checkAccount(ACCOUNT_ADDRESS_1), "Account not found. \n");
    }

    @Test
    public void LedgerCreateTransactionTest(){
        ledger.createAccount(ACCOUNT_ADDRESS_1);
        ledger.createAccount(ACCOUNT_ADDRESS_2);
        ledger.createTransaction(ACCOUNT_ADDRESS_1, ACCOUNT_ADDRESS_2, 20, "123");
        assertEquals(ledger.backlog.size(), 1);
        assertEquals(ledger.backlog.get(0).getSourceAddress(), ACCOUNT_ADDRESS_1);
        assertEquals(ledger.backlog.get(0).getDestinationAddress(), ACCOUNT_ADDRESS_2);
        assertEquals(ledger.backlog.get(0).gettSig(), "123");
        assertEquals(ledger.accounts.get(0).getBalance(), INITIAL_BALANCE - 20);
        assertEquals(ledger.accounts.get(1).getBalance(), INITIAL_BALANCE);
    }


    /*
    @Test
    public void LedgerReceiveTransactionTest(){
        try {
            ledger.loadKeys(System.getProperty("user.dir") + "src/main/resources/");
        }catch(Exception e){

        }
        ledger.createAccount(ACCOUNT_ADDRESS_1);
        ledger.createAccount(ACCOUNT_ADDRESS_2);
        ledger.createTransaction(ACCOUNT_ADDRESS_1, ACCOUNT_ADDRESS_2, 20, "123");
        Request req = new Request(Opcode.RECEIVE_TRANSACTION);
        req.addParameter(ACCOUNT_ADDRESS_2);
        req.addParameter((ACCOUNT_ADDRESS_1));
        req.setdSig("123123");
        ledger.ReceiveTransaction(req);
        assertEquals(ledger.backlog.size(), 0);
        assertEquals(ledger.accounts.get(0).getBalance(), INITIAL_BALANCE - 20);
        assertEquals(ledger.accounts.get(1).getBalance(), INITIAL_BALANCE + 20);
        Block b = ledger.blockchain.get(1);
        assertEquals(b.getBlockTransactions().size(), 1);
        assertEquals(ledger.verifyChain(), true);
        Transaction t = b.getBlockTransactions().get(0);
        assertEquals(t.getDestinationAddress(), ACCOUNT_ADDRESS_2);
        assertEquals(t.getSourceAddress(), ACCOUNT_ADDRESS_1);
    }

    @Test
    public void LedgerAlterDoneTransactionTest(){
        ledger.createAccount(ACCOUNT_ADDRESS_1);
        ledger.createAccount(ACCOUNT_ADDRESS_2);
        ledger.createTransaction(ACCOUNT_ADDRESS_1, ACCOUNT_ADDRESS_2, 20, "123");
        Request req = new Request(Opcode.RECEIVE_TRANSACTION);
        req.addParameter(ACCOUNT_ADDRESS_2);
        req.addParameter((ACCOUNT_ADDRESS_1));
        req.setdSig("123123");
        ledger.ReceiveTransaction(req);
        assertEquals(ledger.backlog.size(), 0);
        assertEquals(ledger.accounts.get(0).getBalance(), INITIAL_BALANCE - 20);
        assertEquals(ledger.accounts.get(1).getBalance(), INITIAL_BALANCE + 20);
        Block b = ledger.blockchain.get(1);
        assertEquals(b.getBlockTransactions().size(), 1);
        assertEquals(ledger.verifyChain(), true);
        Transaction t = b.getBlockTransactions().get(0);
        assertEquals(t.getDestinationAddress(), ACCOUNT_ADDRESS_2);
        assertEquals(t.getSourceAddress(), ACCOUNT_ADDRESS_1);
        t.setDestinationAddress("HACKED_THIS_TRANSACTION");
        assertEquals(ledger.verifyChain(), false);
    }
    */

}
