package pt.ulisboa.tecnico.sec;

import java.util.ArrayList;
import java.util.Date;
import com.google.gson.Gson;

public class Block
{
    public String hash;
    public String previousBlockHash;
    private String data;
    private long time;
    private ArrayList<Transaction> blockTransactions;
    private transient int nonce;
    private transient int MAX_TRANSACTIONS_PER_BLOCK = 10;
    public Block(String data, String previousHash){
        this.data = data;
        this.previousBlockHash = previousHash;
        this.time = new Date().getTime();
        this.hash = calculateHash();
        this.blockTransactions = new ArrayList<Transaction>();
    }

    public String calculateHash(){
        Gson gson = new Gson();
        return StringUtil.sha256(this.previousBlockHash + Long.toString(this.time) + this.data + Integer.toString(nonce) + gson.toJson(this.blockTransactions));
    }

    public void mine(int dificulty){
        String objective = new String(new char[dificulty]).replace('\0','0');
        for(Transaction t : this.blockTransactions){
            t.process();
        }
        while(!this.hash.substring(0, dificulty).equals(objective)){
            this.nonce++;
            this.hash = calculateHash();
        }

        System.out.println("Block mined! Block hash: " + this.hash);
    }

    public void addTransaction(Transaction tr){
        //block has a limit.
        if(this.blockTransactions.size() < MAX_TRANSACTIONS_PER_BLOCK)
            this.blockTransactions.add(tr);
    }

    public String getTransactionsAsJSon(){
        Gson gson = new Gson();
        return gson.toJson(this.blockTransactions);
    }
    public String getBlockAsJSon(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }


}