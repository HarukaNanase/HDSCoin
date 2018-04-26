package pt.ulisboa.tecnico.sec;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class LedgerNode {
    private String nodeName;
    private int port;
    private Socket nodeSocket;
    private int messageWaitTime = 0;
    private DataOutputStream out;
    private DataInputStream in;
    private String publicKeyString;

    public LedgerNode(String name, int port){
        this.nodeName = name;
        this.port = port;
    }

    public boolean connect(){
        try{
            this.nodeSocket = new Socket(this.nodeName, this.port);
            this.in = new DataInputStream(this.nodeSocket.getInputStream());
            this.out = new DataOutputStream(this.nodeSocket.getOutputStream());
            return true;
        }catch(IOException ioe) {
            ioe.getMessage();
            return false;
        }
    }

    public Request sendRequest(Request request){
        try {
            this.out.writeUTF(request.requestAsJson());
            //this.nodeSocket.setSoTimeout(messageWaitTime);
            String answer = this.in.readUTF();
            return Request.requestFromJson(answer);
        }catch(SocketTimeoutException ste){
            System.out.println("Socket timed out. Handle fault.");
            return new Request(Opcode.NO_ANSWER);
        } catch(IOException ioe){
            ioe.printStackTrace();
            System.out.println("Socket: " + this.nodeSocket);
            return new Request(Opcode.SOCKET_ERROR);
        }
    }

    public int getMessageWaitTime(){
        return this.messageWaitTime;
    }

    public void setMessageTime(int newDelay){
        this.messageWaitTime = newDelay;
        try {
            this.nodeSocket.setSoTimeout(messageWaitTime);
        }catch(IOException ioe){
            ioe.getMessage();
        }
    }




}
