package pt.ulisboa.tecnico.sec;

import com.google.gson.Gson;

public class Transaction {
    private transient Account sourceAcc;
    private transient Account destinationAcc;
    private String sourceAddress;
    private String destinationAddress;
    private int value;
    private transient boolean isProcessed = false;
    private transient boolean shouldProcess = false;

    public Transaction(Account src, Account dest, int value){
        this.sourceAcc = src;
        this.destinationAcc = dest;
        this.sourceAddress = src.getAccountAddress();
        this.destinationAddress = dest.getAccountAddress();
        this.value = value;
    }

    public void process(){
        if(shouldProcess) {
            this.sourceAcc.makePayment(value);
            this.destinationAcc.receivePayment(value);
            this.isProcessed = true;
        }
    }

    public void signalToProcess(){
        this.shouldProcess = true;
    }

    public String getTransactionInfo(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

