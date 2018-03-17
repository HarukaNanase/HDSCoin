package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import javax.xml.ws.Endpoint;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class Ledger {
    public static ServerSocket mainSocket;

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static ArrayList<Account> accounts = new ArrayList<Account>();
    public static ArrayList<Transaction> backlog = new ArrayList<Transaction>();
    public static int difficulty = 5;


    public static void main(String[] args){
        Block genesis = new Block("First block", "0");

        AddToBlockChain(genesis);
        Block block2 = new Block("Second block", genesis.hash);
        AddToBlockChain(block2);
        Block block3 = new Block("Third block", block2.hash);
        AddToBlockChain(block3);
        Account acc1 = new Account();
        Account acc2 = new Account();
        Transaction t = new Transaction(acc1, acc2, 5);
        //Transaction t2 = new Transaction(acc2, acc1, 10);
        Block block4 = new Block("4th block", block3.hash);
        t.signalToProcess();
        //t2.signalToProcess();
        block4.addTransaction(t);
        //block4.addTransaction(t2);
        AddToBlockChain(block4);

        for(Block b : blockchain){
            System.out.println(b.getTransactionsAsJSon());
        }

        try{
            mainSocket = new ServerSocket(1381);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        while(true){
            try {
                final Socket client = mainSocket.accept();
                int count = 0;
                //System.out.println("Message: " + in.readUTF());

                Thread th = new Thread(new Runnable() {

                    Socket tClient = client;
                    Boolean shouldRun = true;
                    public void run() {
                        while(shouldRun) {
                                System.out.println("New thread responding to client.");
                                handleClientRequest(tClient);
                        }
                    }
                });
                th.start();


            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }

    }

    public static void handleClientRequest(Socket client){
        try {
            DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            String treq = in.readUTF();
            Request req = Request.requestFromJson(treq);
            System.out.println("Request Received!");
            System.out.println(req.requestAsJson());
            if (req.getOpcode().equals("CreateAccount")) {
                String publicKeyBase64 = req.getParameter(0);
                try {
                    byte[] publicKeyBytes = Base64.decode(publicKeyBase64);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                    PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
                    Account newUser = new Account(publicKey, publicKeyBase64);
                    accounts.add(newUser);
                    System.out.println("Account added. Account Size: " + accounts.size());
                    out.writeUTF("Success! Your account balance: " + newUser.getBalance());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (req.getOpcode().equals("CheckAccount")) {
                String key = null;
                key = req.getParameter(0);
                StringBuilder sb = new StringBuilder();
                Account acc = getAccount(key);
                    if (acc != null) {
                        sb.append("Account : " + acc.getAccountAddress() + "\n");
                        sb.append("Balance : " + acc.getBalance() + "\n");
                    }

                for (Transaction t : backlog) {
                    if (t.getDestinationAddress().equals(key)) {
                        sb.append(t.getTransactionInfo());
                        sb.append("\n");
                    }
                }
                try {
                    out.writeUTF(sb.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (req.getOpcode().equals("sendAmount")) {

            } else if (req.getOpcode().equals("receiveAmount")) {

            } else if (req.getOpcode().equals("CreateTransaction")) {
                String src = req.getParameter(0);
                String dst = req.getParameter(1);
                Account acc1 = getAccount(src);
                Account acc2 = getAccount(dst);
                Transaction t;
                int value = Integer.valueOf(req.getParameter(2));
                if(acc1 != null && acc2 != null) {
                    t = new Transaction(acc1, acc2, value);
                    backlog.add(t);
                }
                else
                    out.writeUTF("Destination address is unknown.");


            } else if (req.getOpcode().equals("RequestChain")) {
                try {
                    StringBuilder sb = new StringBuilder();
                    int i = 1;
                    for (Block b : blockchain) {
                        sb.append("Block " + i + ": " + "\n");
                        sb.append(b.getBlockAsJSon());
                        sb.append("\n");
                        i++;
                    }
                    out.writeUTF(sb.toString());
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }catch(Exception e){
            e.printStackTrace();

        }



    }

    public static Account getAccount(String publicKey){
        for(Account a : accounts){
            if (a.getAccountAddress().equals(publicKey)) {
                System.out.println("Account found!");
                return a;
            }
        }
        return null;
    }
    public static boolean AddToBlockChain(Block block){
        block.mine(difficulty);
        blockchain.add(block);
        verifyChain();
        return true;
    }




    public static boolean verifyChain(){
        Block current;
        Block previous;
        String objective = new String(new char[difficulty]).replace('\0', '0');
        for(int i = 1; i< blockchain.size(); i++){
            current = blockchain.get(i);
            previous = blockchain.get(i-1);
            if(!current.hash.equals(current.calculateHash())){
                System.out.println("Current block hash does not match.");
                return false;
            }
            if(!current.previousBlockHash.equals(previous.hash)){
                System.out.println("Previous block hash does not match current block previous hash.");
                return false;
            }
            if(!current.hash.substring(0, difficulty).equals(objective)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }

        return true;
    }






}
