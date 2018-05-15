package pt.ulisboa.tecnico.sec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertNull;

public class ByzantineTest {
    private NodeManager manager = null;
    @Before
    public void setUp(){
        if(manager == null) {
            manager = new NodeManager();
            manager.createNode("127.0.0.1", 1380, "ledger1.cer");
            manager.createNode("127.0.0.1", 1381, "ledger2.cer");
            manager.createNode("127.0.0.1", 1382, "ledger3.cer");
            manager.createNode("127.0.0.1", 1383, "ledger4.cer");
        }

        Request request = new Request(Opcode.CREATE_ACCOUNT);
        Wallet.GenerateKeys();
        request.addParameter(Wallet.getPublicKeyString());
        assertEquals(true, manager.broadcastWrite(request));
    }

    @After
    public void tearDown(){

    }


    @Test
    public void WriteAtomicRegisterTest(){

        System.out.println("WRITE_ATOMIC_REGISTER_CREATE_ACCOUNT_TEST");
        Request request = new Request(Opcode.CREATE_ACCOUNT);
        Wallet.GenerateKeys();
        request.addParameter(Wallet.getPublicKeyString());
        assertEquals(true, manager.broadcastWrite(request));
    }

/*

    @Test
    public void WriteAtomicRegisterTestCreateTransactionTest(){
        System.out.println("WRITE_ATOMIC_REGISTER_CREATE_TRANSACTION");
        Request transaction = new Request(Opcode.CREATE_TRANSACTION);
        transaction.addParameter(Wallet.getPublicKeyString());
        transaction.addParameter(Wallet.getPublicKeyString());
        transaction.addParameter("5");
        transaction.setSequenceNumber(Wallet.getSequenceNumber());
        Wallet.setSequenceNumber(Wallet.getSequenceNumber()+1);
        String tSign = SecurityManager.SignMessage(Wallet.getPublicKeyString() + Wallet.getPublicKeyString() + "5", Wallet.getPrivateKey());
        transaction.addParameter(tSign);
        assertEquals(true, manager.broadcastWrite(transaction));
    }
*/
    @Test
    public void ReadAtomicRegisterTestReadTransactionTest(){
        System.out.println("READ_ATOMIC_READ_TRANSACTION_TEST");
        Request transaction = new Request(Opcode.CREATE_TRANSACTION);
        transaction.addParameter(Wallet.getPublicKeyString());
        transaction.addParameter(Wallet.getPublicKeyString());
        transaction.addParameter("5");
        transaction.setSequenceNumber(Wallet.getSequenceNumber());
        Wallet.setSequenceNumber(Wallet.getSequenceNumber()+1);
        String tSign = SecurityManager.SignMessage(Wallet.getPublicKeyString() + Wallet.getPublicKeyString() + "5", Wallet.getPrivateKey());
        transaction.addParameter(tSign);
        assertEquals(true, manager.broadcastWrite(transaction));
        Request read = new Request(Opcode.CHECK_ACCOUNT);
        read.addParameter(Wallet.getPublicKeyString());
        read.setSequenceNumber(Wallet.getSequenceNumber());
        Wallet.setSequenceNumber(Wallet.getSequenceNumber()+1);
        assertNotNull(manager.broadcastRead(read));
    }

/*
    @Test
    public void WriteAtomicRegisterOneFaultTest(){

        System.out.println("WRITE_ATOMIC_REGISTER_ONE_FAULT_TEST");
        this.manager.getNodes().get(3).setMessageTime(1);
        //force node to timeout
        //only 3 will answer
        Request transaction = new Request(Opcode.CREATE_TRANSACTION);
        transaction.addParameter(Wallet.getPublicKeyString());
        transaction.addParameter(Wallet.getPublicKeyString());
        transaction.addParameter("5");
        transaction.setSequenceNumber(Wallet.getSequenceNumber());
        Wallet.setSequenceNumber(Wallet.getSequenceNumber()+1);
        String tSign = SecurityManager.SignMessage(Wallet.getPublicKeyString() + Wallet.getPublicKeyString() + "5", Wallet.getPrivateKey());
        transaction.addParameter(tSign);
        assertEquals(true, manager.broadcastWrite(transaction));

    }
*/
    @Test
    public void ReadAtomicRegisterOneFaultTest(){
        System.out.println("READ_ATOMIC_REGISTER_ONE_FAULT_TEST");
        this.manager.getNodes().get(3).setMessageTime(1);
        Request read = new Request(Opcode.CHECK_ACCOUNT);
        read.setSequenceNumber(Wallet.getSequenceNumber());
        Wallet.setSequenceNumber(Wallet.getSequenceNumber()+1);
        read.addParameter(Wallet.getPublicKeyString());
        assertNotNull(manager.broadcastRead(read));
    }

    @Test
    public void ReadAtomicRegisterTwoFaultTest(){
        System.out.println("READ_ATOMIC_REGISTER_TWO_FAULT_TEST");
        this.manager.getNodes().get(2).setMessageTime(1);
        this.manager.getNodes().get(3).setMessageTime(1);
        Request read = new Request(Opcode.CHECK_ACCOUNT);
        read.addParameter(Wallet.getPublicKeyString());
        read.setSequenceNumber(Wallet.getSequenceNumber());
        Wallet.setSequenceNumber(Wallet.getSequenceNumber()+1);
        assertNull(manager.broadcastRead(read));
    }
    /*
    @Test
    public void WriteBackPhaseTest(){
        System.out.println("\nWriteBackPhaseTest\n");
        manager.getNodes().remove(3);
        Request transaction = new Request(Opcode.CREATE_TRANSACTION);
        transaction.addParameter(Wallet.getPublicKeyString());
        transaction.addParameter(Wallet.getPublicKeyString());
        transaction.addParameter("5");
        transaction.setSequenceNumber(Wallet.getSequenceNumber());
        Wallet.setSequenceNumber(Wallet.getSequenceNumber()+1);
        String tSign = SecurityManager.SignMessage(Wallet.getPublicKeyString() + Wallet.getPublicKeyString() + "5", Wallet.getPrivateKey());
        transaction.addParameter(tSign);
        assertEquals(true, manager.broadcastWrite(transaction));
        manager.getNodes().remove(2);
        Request transaction2 = new Request(Opcode.CREATE_TRANSACTION);
        transaction2.addParameter(Wallet.getPublicKeyString());
        transaction2.addParameter(Wallet.getPublicKeyString());
        transaction2.addParameter("5");
        transaction2.setSequenceNumber(Wallet.getSequenceNumber()+1);
        Wallet.setSequenceNumber(Wallet.getSequenceNumber()+1);
        String tSign2 = SecurityManager.SignMessage(Wallet.getPublicKeyString() + Wallet.getPublicKeyString() + "5", Wallet.getPrivateKey());
        transaction2.addParameter(tSign2);
        manager.broadcastWrite(transaction2);
        manager.createNode("127.0.0.1", 1382, "ledger3.cer");
        manager.createNode("127.0.0.1", 1383, "ledger4.cer");
        System.out.println("READ_ATOMIC_REGISTER_TWO_FAULT_TEST");
        this.manager.getNodes().get(2).setMessageTime(1);
        this.manager.getNodes().get(3).setMessageTime(1);
        Request read = new Request(Opcode.CHECK_ACCOUNT);
        read.addParameter(Wallet.getPublicKeyString());
        read.setSequenceNumber(Wallet.getSequenceNumber()+1);
        Wallet.setSequenceNumber(Wallet.getSequenceNumber()+1);
        assertNull(manager.broadcastRead(read));
        Request read2 = new Request(Opcode.CHECK_ACCOUNT);
        read2.addParameter(Wallet.getPublicKeyString());
        read2.setSequenceNumber(Wallet.getSequenceNumber()+1);
        Wallet.setSequenceNumber(Wallet.getSequenceNumber()+1);
        assertNull(manager.broadcastRead(read2));


    }
    */



}
