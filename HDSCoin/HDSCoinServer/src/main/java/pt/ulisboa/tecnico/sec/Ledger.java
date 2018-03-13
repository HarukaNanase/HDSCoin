package pt.ulisboa.tecnico.sec;

import javax.xml.ws.Endpoint;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class Ledger {
    public static ServerSocket mainSocket;

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static int difficulty = 5;


    public static void main(String[] args){
        Block genesis = new Block("First block", "0");
        System.out.println("Genesis Block Hash: " + genesis.hash);
        Block block2 = new Block("Second block", genesis.hash);
        System.out.println("Second Block Hash: " + block2.hash);
        Block block3 = new Block("Third block", block2.hash);
        System.out.println("Third Block Hash: " + block3.hash);
        System.out.println("Trying to block third block...");
        block3.mine(difficulty);

        System.out.println("Chain is valid? " + verifyChain());



        try{
            mainSocket = new ServerSocket(1381);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        while(true){
            try {
                Socket client = mainSocket.accept();
                DataInputStream in = new DataInputStream(client.getInputStream());
                byte[] received = new byte[1024];
                int count = 0;
                System.out.println("Message: " + in.readUTF());

            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }

    }

    public static boolean verifyChain(){
        Block current;
        Block previous;
        String objective = new String(new char[difficulty]).replace('\0', '0');
        for(int i = 0; i< blockchain.size(); i++){
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
            if(!current.hash.substring( 0, difficulty).equals(objective)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }

        return true;
    }






}
