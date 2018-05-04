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

    public boolean sendRequest(Request request){
        try {
            SecurityManager.SignMessage(request, Wallet.getPrivateKey());
            this.out.writeUTF(request.requestAsJson());
            return true;
         //   String answer = this.in.readUTF();
         //   return Request.requestFromJson(answer);
        }catch(SocketTimeoutException ste){
            System.out.println("Socket timed out. Handle fault.");
            return false;
          //  return new Request(Opcode.NO_ANSWER);
        } catch(IOException ioe){
            ioe.printStackTrace();
            System.out.println("Socket: " + this.nodeSocket);
            return false;
           // return new Request(Opcode.SOCKET_ERROR);
        }
    }



    public Request receiveRequest(){
        try {
            String answer = this.in.readUTF();
            return Request.requestFromJson(answer);
        }catch(SocketTimeoutException ste) {
            System.out.println("Socket timed out. Handle fault.");
            return new Request(Opcode.NO_ANSWER);
        }
        catch(IOException ioe){
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

    public boolean clearSocket(){
        int n;
        byte[] b= new byte[1024];
        try {
            while ((n = in.read(b)) >= 0) {
                continue;
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
            return false;
        }


        return true;
    }

}
