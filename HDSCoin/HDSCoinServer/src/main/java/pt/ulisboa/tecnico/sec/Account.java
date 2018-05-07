package pt.ulisboa.tecnico.sec;

import com.google.gson.Gson;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Queue;

public class Account {
        private transient int KEY_SIZE = 4096;
        private transient PublicKey publicKey;
        private transient PrivateKey privateKey;
        private String publicKeyString;
        private int balance;
        private Transaction[] backlog;
        private long sequenceNumber;
        private long transactionId = 1;
        private long RID;
        private long WTS;
        private ArrayList<Request> delivered;
        private ArrayList<Request> queue;

        public Account(PublicKey pkey){
            this.publicKey = pkey;
            balance = 50;
            delivered = new ArrayList<>();
            queue = new ArrayList<>();
        }

        public Account(String publicKeyString){
            this.publicKeyString = publicKeyString;
            this.balance = 50;
            this.sequenceNumber = 0;
            delivered = new ArrayList<>();
            queue = new ArrayList<>();
        }

        public Account(PublicKey pkey, String publicKeyString){
            this.publicKey = pkey;
            this.publicKeyString = publicKeyString;
            this.balance = 50;
            this.sequenceNumber = 0;
            delivered = new ArrayList<>();
            queue = new ArrayList<>();
        }

        public Account(){
            balance = 50;
            this.sequenceNumber = 0;
            delivered = new ArrayList<>();
            queue = new ArrayList<>();
        }

        public void setTransactionId(long id){ this.transactionId = id;}
        public long getTransactionId(){ return this.transactionId;}

        public ArrayList<Request> getDelivered(){
            return this.delivered;
        }
        public ArrayList<Request> getQueue() { return this.queue;}
        public long getSequenceNumber(){
            return this.sequenceNumber;
        }

        public void setSequenceNumber(long new_number){
            this.sequenceNumber = new_number;
        }

        public String getPublicKeyString(){
            return this.publicKeyString;
        }

        public PublicKey getPublicKey(){ return this.publicKey;}

        public int getBalance(){
            return this.balance;
        }


        public void makePayment(int value) throws Exception{
            if((this.balance - value) < 0 || value <= 0){
                //error out
                throw new Exception("Not enough balance or negative value.");
            }
            balance -= value;
        }

        public void receivePayment(int value) throws Exception{
            if(value <= 0)
                throw new Exception("Receiving negative or zero value");
            this.balance += value;
        }

        public String getAccountInfo(){
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    public String getAccountAddress(){
            return this.publicKeyString;
        }


    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void setRID(long rid){
            this.RID = rid;
    }

    public void setWTS(long wts){
        this.WTS = wts;
    }

    public long getRID(){
            return this.RID;
    }
    public long getWTS(){
        return this.WTS;
    }
}
