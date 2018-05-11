package pt.ulisboa.tecnico.sec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import pt.ulisboa.tecnico.sec.Account;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccountTest {

    Account account;
    Account account2;
    int INITIAL_BALANCE = 50;
    @Before
    public void setUp(){
        account = new Account();
    }

    @After
    public void tearDown(){
        account = null;
    }

    @Test
    public void AccountConstructorTest(){
        assertEquals(account.getBalance(), 50);
        assertEquals(account.getSequenceNumber(), 0);
    }

    @Test
    public void SequenceNumberTest(){
        account.setSequenceNumber(10);
        assertEquals(account.getSequenceNumber(), 10);
    }

    @Test
    public void ReceivePaymentTest() throws Exception{
        assertEquals(account.getBalance(), 50);
        account.receivePayment(10);
        assertEquals(account.getBalance(), 60);
    }

    @Test(expected=Exception.class)
    public void ReceivePaymentNegativeErrorTest() throws Exception{
        account.receivePayment(-10);
    }

    @Test(expected=Exception.class)
    public void ReceivePaymentZeroErrorTest() throws Exception{
        account.receivePayment(0);
    }

    @Test
    public void GetPublicKeyTest(){
        account = new Account("123123");
        assertEquals(account.getPublicKeyString(), "123123");
    }

    @Test
    public void GetAddressTest(){
        account = new Account("123123");
        assertEquals(account.getAccountAddress(), "123123");
        assertEquals(account.getAccountAddress(), account.getPublicKeyString());
    }

}
