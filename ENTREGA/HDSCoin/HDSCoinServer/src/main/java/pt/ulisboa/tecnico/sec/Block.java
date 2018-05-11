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
    private transient Account acc;
    private long height;
    private transient int MAX_TRANSACTIONS_PER_BLOCK = 10;

    public Block(String data, Account acc){
        this.data = data;
        this.acc = acc;
        this.previousBlockHash = acc.getBlockChainLastHash();
        this.time = new Date().getTime();
        this.height = acc.getWTS();
        //this.hash = calculateHash(acc.getWTS());
        this.blockTransactions = new ArrayList<Transaction>();
    }

    public String calculateHash(){
        Gson gson = new Gson();
        return StringUtil.sha256(this.previousBlockHash + this.data + gson.toJson(this.blockTransactions)+ this.height);
    }

    public ArrayList<Transaction> getBlockTransactions(){
        return this.blockTransactions;
    }

    public void mine(){
        for(Transaction t : this.blockTransactions){
            System.out.println("PROCESSING THIS TRANSACTION:\n" + t.getTransactionInfo());
            t.process();
        }
        this.hash = calculateHash();
        System.out.println("Block mined for a client! Block hash: " + this.hash);

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

    public long getHeight(){
        return this.height;
    }


}