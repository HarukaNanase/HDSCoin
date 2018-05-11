package pt.ulisboa.tecnico.sec;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
public class NodeManager {

    private ArrayList<LedgerNode> nodes;
    private int DEFAULT_TIMEOUT = 5000;
    private int FAULT_VALUE = 1;
    private long WTS = 0;
    private long RID = 0;
    public NodeManager(){
        nodes = new ArrayList<LedgerNode>();
    }

    public ArrayList<LedgerNode> getNodes(){
        return this.nodes;
    }

    public void addNewNode(LedgerNode node){
        this.nodes.add(node);
    }

    public void createNode(String name, int port, String certName){
        LedgerNode node = new LedgerNode(name, port);
        if(node.connect()) {
            node.setMessageTime(DEFAULT_TIMEOUT);
            try {
                node.loadKey(System.getProperty("user.dir") + "/src/main/resources/"+certName);
            }catch(Exception e){
                e.printStackTrace();
            }
            this.nodes.add(node);
            System.out.println("Added node: " + name + ":" + port);
        }
        else
            System.out.println("Failed to add node: " + name + ":" + port);
    }

    public void setWTS( long wts){
        this.WTS = wts;
    }

    public void setRID(long rid){
        this.RID = rid;
    }

    public boolean broadcastWrite(Request request){
        ArrayList<Request> answers = new ArrayList<Request>();
        this.WTS++;
        request.setWTS(WTS);
        //SecurityManager.SignMessage(request, Wallet.getPrivateKey());
        //System.out.println("SENDING OUT: " + request.requestAsJson());
        for(LedgerNode node : this.nodes)
            answers.add(node.sendRequest(request));
        // verify answers and decide based on it.
        //Request mostCommon = mostCommon(answers);
        return decideRegularRegisterWrite(answers);
    }

    /*
     * @Method - decideRegularRegister
     * @Input - Answers from N node s
     * @Returns boolean - decision of the consensus
     */
    public boolean decideRegularRegisterWrite(ArrayList<Request> answers){
        //check answer and we must have F + 1 equal answers
        System.out.println("DECIDE REGULAR REGISTER START: ");
        ArrayList<Request> outOfSync = new ArrayList<Request>(answers);
        outOfSync.removeIf(r -> r.getOpcode() != Opcode.ACK || r.getWTS() == this.WTS);
        //out of sync list contains the nodes which have replied with an ACK but wrong WTS.
        //java 8
        answers.removeIf(s -> s.getOpcode() != Opcode.ACK || s.getWTS() != this.WTS);
        if(answers.size() <= (((float)nodes.size() + FAULT_VALUE)/2))
            return false;

        //is this enough? can i just return true?

        HashMap<Request, Integer> occurrenceMap = new HashMap<Request, Integer>();
        for(Request req : answers){
            Integer occurrences = occurrenceMap.get(req);
            occurrenceMap.put(req, occurrences == null ? 1 : occurrences + 1);
        }

        for(Entry<Request,Integer> entry : occurrenceMap.entrySet()){
            Request req = entry.getKey();
            if(req.getOpcode() == Opcode.ACK && Long.parseLong(req.getParameter(0)) == this.WTS){
                if(entry.getValue() >= (2*FAULT_VALUE + 1)) {
                    System.out.println("Number of acks: " + entry.getValue());
                    return true;
                }
            }
        }
        return false;

    }

    public Request broadcastRead(Request request){

        ArrayList<Request> readlist = new ArrayList<>();
        this.RID++;
        request.setRID(this.RID);
        for(LedgerNode node : this.nodes) {
            request.setNodeID(node.getPublicKeyString());
            readlist.add(node.sendRequest(request));
        }

        return decideRegularRegisterRead(readlist, request);
    }

    public Request decideRegularRegisterRead(ArrayList<Request> readlist, Request rekt){
        if(rekt.getOpcode() == Opcode.AUDIT)
            readlist.removeIf(r->r.getOpcode() != Opcode.SERVER_ANSWER || r.getRID() != this.RID);
        else
            readlist.removeIf(r->r.getOpcode() != Opcode.SERVER_ANSWER || r.getWTS() > this.WTS || r.getRID() != this.RID);

        System.out.println("VALID ANSWERS SIZE: " + readlist.size());
        System.out.println("DECIDE REGULAR REGISTER READ:");
        if(readlist.size() > (((float)nodes.size() + FAULT_VALUE)/2)) {
            Request highestval = null;
            for(Request req : readlist){
                if(highestval == null || highestval.getWTS() < req.getWTS())
                    highestval = req;
            }

            HashMap<Request, Integer> occurrenceMap = new HashMap<Request, Integer>();
            for(Request req : readlist){
                Integer occurrences = occurrenceMap.get(req);
                occurrenceMap.put(req, occurrences == null ? 1 : occurrences + 1);
            }

            for(Entry<Request,Integer> entry : occurrenceMap.entrySet()) {
                Request req = entry.getKey();
                System.out.println("Values: " + entry.getValue());
                if (req.getOpcode() == Opcode.SERVER_ANSWER && req.getWTS() == this.WTS) {
                    if (entry.getValue() >= (2 * FAULT_VALUE + 1)) {
                        System.out.println("Quorum sucessfull. 2F+1 equal highestvals.");
                        return req;
                    } else {
                        if (highestval != null) {
                            LedgerNode node = this.getNodeByKey(highestval.getNodeID());
                            if(node != null && node.getDelivered().get(node.getDelivered().size()-1).getOpcode() == Opcode.REQUEST_SEQUENCE_NUMBER){
                                return highestval;
                            }
                            Request request = new Request(Opcode.GET_CURRENT_STATE);
                            request.addParameter(Wallet.getPublicKeyString());
                            request.setSequenceNumber(Wallet.getSequenceNumber() + 1);
                            Wallet.setSequenceNumber(request.getSequenceNumber());
                            Request highestState = node.sendRequest(request);
                            System.out.println("WTF");
                            String stateData = highestState.getParameter(0);
                            Request writeBack = new Request(Opcode.WRITE_BACK);
                            writeBack.addParameter(Wallet.getPublicKeyString());
                            writeBack.addParameter(stateData);
                            if (broadcastWrite(writeBack)) {
                                return highestval;
                            } else {
                                //?
                                return highestval;
                            }
                        }
                    }
                }
            }
            //System.out.println("Decision: ");
            //for(String s : highestval.getParameters())
             //   System.out.println(s);
           return highestval;
        }
        return null;
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


    public LedgerNode getNodeByKey(String pkey){
        for(LedgerNode ledger : this.nodes){
            if(ledger.getPublicKeyString().equals(pkey))
                return ledger;
        }
        return null;
    }





}
