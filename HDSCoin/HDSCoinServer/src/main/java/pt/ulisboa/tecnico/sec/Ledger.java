package pt.ulisboa.tecnico.sec;

import javax.xml.ws.Endpoint;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.PublicKey;
import java.util.ArrayList;

public class Ledger {
    public static ServerSocket mainSocket;

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static int difficulty = 2;


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
        Transaction t2 = new Transaction(acc2, acc1, 10);
        Block block4 = new Block("4th block", block3.hash);
        t.signalToProcess();
        t2.signalToProcess();
        block4.addTransaction(t);
        block4.addTransaction(t2);
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
                DataInputStream in = new DataInputStream(client.getInputStream());
                int count = 0;
                //System.out.println("Message: " + in.readUTF());
                final String req = in.readUTF();

                Thread th = new Thread(new Runnable() {
                    String tReq = req;
                    Socket tClient = client;
                    public void run() {
                        System.out.println("New thread responding to cilent.");
                        handleClientRequest(tReq, tClient);
                    }
                });
                th.start();


            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }

    }

    public static void handleClientRequest(String treq, Socket client){
        Request req = Request.requestFromJson(treq);
        if(req.getOpcode().equals("createAccount")){

        }
        else if(req.getOpcode().equals("sendAmount")){

        }else if(req.getOpcode().equals("receiveAmount")){

        }
        else if(req.getOpcode().equals("CreateTransaction")){

        }else if(req.getOpcode().equals("RequestChain")){
            try{
                DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                StringBuilder sb = new StringBuilder();
                int i = 1;
                for(Block b: blockchain){
                    sb.append("Block " + i + ": "+"\n");
                    sb.append(b.getBlockAsJSon());
                    sb.append("\n");
                    i++;
                }
                dos.writeUTF(sb.toString());
                dos.close();
            }catch(Exception e){
                e.printStackTrace();
            }

        }



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
