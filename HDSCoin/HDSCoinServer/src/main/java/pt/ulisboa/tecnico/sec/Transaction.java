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
    private String tSig;
    public Transaction(Account src, Account dest, int value){
        this.sourceAcc = src;
        this.destinationAcc = dest;
        this.sourceAddress = src.getAccountAddress();
        this.destinationAddress = dest.getAccountAddress();
        this.value = value;
    }
    public boolean isProcessed(){
        return this.isProcessed;
    }

    public void process(){
        if(shouldProcess) {
            if(sourceAcc != null && destinationAcc != null){
                //this.sourceAcc.makePayment(value);
                try {
                    this.destinationAcc.receivePayment(value);
                }catch(Exception e){
                    System.out.println(e.getMessage());
                    return;
                }
                this.isProcessed = true;
            }
        }
    }

    public void setSourceAddress(String src){
        this.sourceAddress = src;
    }

    public void setDestinationAddress(String dst){
        this.destinationAddress = dst;
    }

    public void signalToProcess(){
        this.shouldProcess = true;
    }

    public String getTransactionInfo(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String getSourceAddress(){
        return sourceAddress;
    }

    public String getDestinationAddress(){
        return destinationAddress;
    }

    public void settSig(String transaction_signature){
        this.tSig = transaction_signature;
    }

    public String gettSig(){
        return this.tSig;
    }
}

