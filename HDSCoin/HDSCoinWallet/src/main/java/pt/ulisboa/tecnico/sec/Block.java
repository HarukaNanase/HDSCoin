package pt.ulisboa.tecnico.sec;

import java.util.Date;
import pt.ulisboa.tecnico.meic.sirs.*;
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
    }

    public calculateHash(){
        
    }

}