package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class LedgerNode {
    private String nodeName;
    private int port;
    private Socket nodeSocket;
    private int messageWaitTime = 0;
    private DataOutputStream out;
    private DataInputStream in;
    private String publicKeyString;
    private ArrayList<Request> delivered;

    public LedgerNode(String name, int port){
        this.nodeName = name;
        this.port = port;
        this.delivered = new ArrayList<Request>();
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

    //return answer?
    public Request sendRequest(Request request){
        try {
            SecurityManager.SignMessage(request, Wallet.getPrivateKey());
            this.out.writeUTF(request.requestAsJson());
            String answer = this.in.readUTF();
            Request ans = Request.requestFromJson(answer);
            if(SecurityManager.VerifyMessage(ans, this.publicKeyString) && !delivered.contains(request)){
                delivered.add(request);
                return ans;
            }
            return new Request(Opcode.INVALID_SIG);
        }catch(SocketTimeoutException ste){
            System.out.println("Socket timed out. Handle fault.");
            //retry?
            return new Request(Opcode.NO_ANSWER);
        } catch(IOException ioe){
            //reconnect and resend?
            ioe.printStackTrace();
            System.out.println("Socket: " + this.nodeSocket);
            return new Request(Opcode.SOCKET_ERROR);
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

    //temp
    public void loadKey(String path) throws Exception{
        FileInputStream fin = new FileInputStream(path);
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)f.generateCertificate(fin);
        PublicKey serverPublicKey = certificate.getPublicKey();
        byte[] pubKeyBytes = serverPublicKey.getEncoded();
        this.publicKeyString = Base64.encode(pubKeyBytes, 2048);
        fin.close();
    }

}
