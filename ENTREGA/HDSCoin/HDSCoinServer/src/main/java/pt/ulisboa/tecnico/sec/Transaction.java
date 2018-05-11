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
    private String TSig;
    private String RSig;
    private long transactionId;
    private long receiverId;
    private String transactionSignature;
    public Transaction(Account src, Account dest, int value, String tSig){
        this.sourceAcc = src;
        this.destinationAcc = dest;
        this.sourceAddress = src.getAccountAddress();
        this.destinationAddress = dest.getAccountAddress();
        this.value = value;
        this.TSig = tSig;
        this.RSig = null;
        this.transactionId = src.getTransactionId();
        src.setTransactionId(this.transactionId+1);
        this.receiverId = 0;
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
                    this.receiverId = destinationAcc.getTransactionId();
                    destinationAcc.setTransactionId(destinationAcc.getTransactionId()+1);
                }catch(Exception e){
                    System.out.println(e.getMessage());
                    return;
                }
                this.isProcessed = true;
            }
        }
    }

    public void setRSig(String RSig){
        this.RSig = RSig;
    }
    public String getRSig(){
        return this.RSig;
    }

    public void setTSig(String tsig){
        this.TSig = tsig;
    }

    public String getTSig(){
        return this.TSig;
    }

    public int getValue(){
        return this.value;
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
        this.transactionSignature = transaction_signature;
    }

    public String gettSig(){
        return this.transactionSignature;
    }

    public long getTransactionId(){
        return this.transactionId;
    }

    public long getReceiverId(){
        return this.receiverId;
    }

    public void setReceiverId( long id){
        this.receiverId = id;
    }
}

