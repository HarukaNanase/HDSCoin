package pt.ulisboa.tecnico.sec;

import java.util.Date;

public class Block
{
    public String hash;
    public String previousBlockHash;
    private String data;
    private long time;
    private Transaction[] blockTransactions;
    private int nonce;

    public Block(String data, String previousHash){
        this.data = data;
        this.previousBlockHash = previousHash;
        this.time = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash(){
        return StringUtil.sha256(this.previousBlockHash + Long.toString(this.time) + this.data + Integer.toString(nonce));
    }

    public void mine(int dificulty){
        String objective = new String(new char[dificulty]).replace('\0','0');
        while(!this.hash.substring(0, dificulty).equals(objective)){
            this.nonce++;
            System.out.println("Nonce: " + this.nonce);
            this.hash = calculateHash();
            System.out.println("Hash calculated for this nonce: " + this.hash);
        }

        System.out.println("Block mined! Block hash: " + this.hash);
    }

}