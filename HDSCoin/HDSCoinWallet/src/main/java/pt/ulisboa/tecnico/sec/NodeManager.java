package pt.ulisboa.tecnico.sec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
public class NodeManager {

    private ArrayList<LedgerNode> nodes;
    private int DEFAULT_TIMEOUT = 0;
    private int FAULT_VALUE = 1;
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
            System.out.println("Added node: " + name + ":" + port);
        }
        else
            System.out.println("Failed to add node: " + name + ":" + port);
    }


    public Request broadcast(Request request){
        ArrayList<Request> answers = new ArrayList<Request>();
        for(LedgerNode node : this.nodes)
            answers.add(node.sendRequest(request));
        // verify answers and decide based on it.
        for(Request req : answers)
            System.out.println(req.requestAsJson());
        Request mostCommon = mostCommon(answers);
        if(mostCommon == null){
            System.out.println("Failed to check most common answer... retrying after 5 seconds.");
            try{
                Thread.sleep(5000);
                return broadcast(request);
            }catch(InterruptedException ie){
                System.out.println(ie.getMessage());
                return null;
            }
        }
        return mostCommon(answers);
    }

    /*
    * @method - mostCommon
    * @Input ArrayList of Requests - the answers from N nodes
    * @Returns Request - the most common request
     */
    public Request mostCommon(ArrayList<Request> answers){
        ArrayList<Request> commonAnswers = new ArrayList<Request>();
        HashMap<Request, Integer> occurrenceMap = new HashMap<Request, Integer>();
        for(Request req : answers){
            Integer occurrences = occurrenceMap.get(req);
            occurrenceMap.put(req, occurrences == null ? 1: occurrences+1);
        }

        if(occurrenceMap.size() == answers.size())
            return null;

        Entry<Request, Integer> mostOccurrences = null;

        for(Entry<Request,Integer> entry : occurrenceMap.entrySet()){
            if(mostOccurrences == null || entry.getValue() > mostOccurrences.getValue())
                mostOccurrences = entry;
            else if(entry.getValue().intValue() == mostOccurrences.getValue().intValue())
                return null;
        }
        return mostOccurrences == null ? null: mostOccurrences.getKey();

    }

    /*
    * @Method - decideRegularRegister
    * @Input - Answers from N nodes
    * @Returns boolean - decision of the consensus
     */
    //TODO: CHANGE SEQUENCE NUMBER FOR A WRITE/READ ID
    public boolean decideRegularRegister(ArrayList<Request> answers){
        //check answer and we must have F + 1 equal answers

        //java 8
        answers.removeIf(s -> s.getOpcode() == Opcode.NO_ANSWER || s.getOpcode() == Opcode.SOCKET_ERROR);
        if(answers.size() <= (((float)nodes.size() + FAULT_VALUE)/2))
            return false;

        HashMap<Request, Integer> occurrenceMap = new HashMap<Request, Integer>();
        for(Request req : answers){
            //check above, not needed anymore
            /*
            if(req.getOpcode() == Opcode.NO_ANSWER || req.getOpcode() == Opcode.SOCKET_ERROR){
                answers.remove(req);
            }
            else if(answers.size() <= (float)((nodes.size() + FAULT_VALUE)/2)){
                return false;
            }
            */
            //else {
                Integer occurrences = occurrenceMap.get(req);
                occurrenceMap.put(req, occurrences == null ? 1 : occurrences + 1);
            //}
        }

        // check if ACK with current sequence number from Wallet satisfies the condition of F + 1
        for(Entry<Request,Integer> entry : occurrenceMap.entrySet()){
            Request req = entry.getKey();
            if(req.getOpcode() == Opcode.ACK && req.getSequenceNumber() == Wallet.getSequenceNumber()){
                if(entry.getValue() > (FAULT_VALUE + 1))
                    return true;
            }
            //if sequence number is less than wallet sequence number, server is out of sync, so we need to re-sync the missing ops
            if(req.getOpcode() == Opcode.ACK && req.getSequenceNumber() < Wallet.getSequenceNumber()){
                //fix ledger that is missing info
            }
        }
        return false;

    }




}
