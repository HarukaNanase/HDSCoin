package pt.ulisboa.tecnico.sec;

import java.util.ArrayList;

public class NodeManager {

    private ArrayList<LedgerNode> nodes;
    private int DEFAULT_TIMEOUT = 0;
    public NodeManager(){
        nodes = new ArrayList<LedgerNode>();
    }

    public ArrayList<LedgerNode> getNodes(){
        return this.nodes;
    }

    public void addNewNode(LedgerNode node){
        this.nodes.add(node);
    }

    public void createNode(String name, int port){
        LedgerNode node = new LedgerNode(name, port);
        if(node.connect()) {
            node.setMessageTime(DEFAULT_TIMEOUT);
            this.nodes.add(node);
        }
        else
            System.out.println("Failed to add node: " + name + ":" + port);
    }


    public boolean broadcast(Request request){
        ArrayList<Request> answers = new ArrayList<Request>();
        for(LedgerNode node : this.nodes)
            answers.add(node.sendRequest(request));
        // verify answers and decide based on it.
        for(Request req : answers)
            System.out.println(req.requestAsJson());

        return true;
    }

}
