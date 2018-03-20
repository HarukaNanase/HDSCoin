package pt.ulisboa.tecnico.sec;

import com.google.gson.*;

import java.util.ArrayList;

public class Request {
    private String opcode;
    private ArrayList<String> parameters;

    public Request(String opcode, ArrayList<String> param){
        this.opcode = opcode;
        this.parameters = param;
    }

    public Request(String opcode){
        this.opcode = opcode;
        this.parameters = new ArrayList<String>();
    }

    public Request(){
        this.opcode = "";
        this.parameters = new ArrayList<String>();
    }

    public void addParamemter(String param){
        this.parameters.add(param);
    }

    public void setOpcode(String opc){
        this.opcode = opcode;
    }

    public String getOpcode(){
        return this.opcode;
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
}