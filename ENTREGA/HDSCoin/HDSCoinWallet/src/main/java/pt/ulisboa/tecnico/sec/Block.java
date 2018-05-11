package pt.ulisboa.tecnico.sec;

import java.security.MessageDigest;
import java.util.Date;
public class Block
{
    public String hash;
    public String previousBlockHash;
    private String data;
    private long time;
    private Transaction[] blockTransactions;

    public Block(String data, String previousHash){
        this.data = data;
        this.previousBlockHash = previousHash;
        this.time = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash(){
        return "a";
    }


}