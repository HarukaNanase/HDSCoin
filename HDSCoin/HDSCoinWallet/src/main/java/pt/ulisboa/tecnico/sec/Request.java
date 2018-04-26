package pt.ulisboa.tecnico.sec;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Objects;

public class Request{
    private Opcode opcode;
    private ArrayList<String> parameters;
    private String dSig;
    private long createdOn;
    private long expiresOn;
    private long sequenceNumber = 0;

    public Request(Opcode opcode, ArrayList<String> param){
        this.opcode = opcode;
        this.parameters = param;
    }

    public Request(Opcode opcode){
        this.opcode = opcode;
        this.parameters = new ArrayList<String>();
    }

    public Request(){
        this.parameters = new ArrayList<String>();
    }

    public void addParameter(String param){
        this.parameters.add(param);
    }

    public void setParameter(int idx, String newParam){
        this.parameters.set(idx, newParam);
    }
    public void setOpcode(Opcode opc){
        this.opcode = opcode;
    }

    public Opcode getOpcode(){
        return this.opcode;
    }

    public long getCreatedOn(){
        return this.createdOn;
    }

    public void setCreatedOn(long time){
        this.createdOn = time;
    }

    public long getExpiresOn(){
        return this.expiresOn;
    }

    public void setExpiresOn(long time){
        this.expiresOn = time;
    }

    public long getSequenceNumber(){
        return this.sequenceNumber;
    }

    public void setSequenceNumber(long new_sequence){
        this.sequenceNumber = new_sequence;
    }


    public String requestAsJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public ArrayList<String> getParameters(){
        return this.parameters;
    }

    public String getParameter(int idx){
        return this.parameters.get(idx);
    }

    public void readRequest(String incoming){
        Gson gson = new Gson();
        Request incomingRequest = gson.fromJson(incoming, Request.class);
        this.opcode = incomingRequest.opcode;
        this.parameters = incomingRequest.parameters;
    }

    public static Request requestFromJson(String incomingJson){
        Gson gson = new Gson();
        return gson.fromJson(incomingJson, Request.class);
    }

    public void setdSig(String dSig){
        this.dSig = dSig;
    }

    public String getdSig(){
        return this.dSig;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Request) {
            Request target = (Request) obj;
            if (this.getOpcode() == target.getOpcode() && this.getSequenceNumber() == target.getSequenceNumber() && this.getParameters().size() == target.getParameters().size()) {
                for (int i = 0; i < this.getParameters().size(); i++) {
                    if (!this.getParameter(i).equals(target.getParameter(i))) {
                        System.out.println("A parameter is different at index: " + i);
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public int hashCode(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.opcode);
        sb.append(this.sequenceNumber);
        for(String str : this.getParameters()){
            sb.append(str);
        }
        return Objects.hash(sb.toString());
    }
}
