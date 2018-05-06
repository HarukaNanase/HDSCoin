package pt.ulisboa.tecnico.sec;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
public class NodeManager {

    private ArrayList<LedgerNode> nodes;
    private int DEFAULT_TIMEOUT = 10000;
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
        System.out.println("SENDING OUT: " + request.requestAsJson());
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
            System.out.println(req.requestAsJson());
            Integer occurrences = occurrenceMap.get(req);
            occurrenceMap.put(req, occurrences == null ? 1 : occurrences + 1);
        }

        for(Entry<Request,Integer> entry : occurrenceMap.entrySet()){
            Request req = entry.getKey();
            if(req.getOpcode() == Opcode.ACK && Long.parseLong(req.getParameter(0)) == this.WTS){
                if(entry.getValue() > (FAULT_VALUE + 1)) {
                    System.out.println("Got F + 1 acks with correct WTS.");
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
        for(LedgerNode node : this.nodes)
            readlist.add(node.sendRequest(request));
        return decideRegularRegisterRead(readlist);
    }

    public Request decideRegularRegisterRead(ArrayList<Request> readlist){
        readlist.removeIf(r -> r.getRID() != this.RID || r.getOpcode() != Opcode.SERVER_ANSWER);
        System.out.println("DECIDE REGULAR REGISTER READ:");
        if(readlist.size() > (((float)nodes.size() + FAULT_VALUE)/2)) {
            Request highestval = null;
            for(Request req : readlist){
               // System.out.println(req.requestAsJson());
                if(highestval == null || highestval.getWTS() < req.getWTS())
                    highestval = req;
            }
            System.out.println("Decision: ");
            System.out.println(highestval.requestAsJson());
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






}
