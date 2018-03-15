package pt.ulisboa.tecnico.sec;

import java.io.*;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Wallet {
    public static Socket mainSocket;
    private static PublicKey pKey;
    private static PrivateKey privKey;

    public static void main(String[] args){
        System.out.println("Wallet v0.01");
        try {
            mainSocket = new Socket("127.0.0.1", 1381);
            DataOutputStream out = new DataOutputStream(mainSocket.getOutputStream());
            DataInputStream in = new DataInputStream(mainSocket.getInputStream());
            Request req = new Request("RequestChain");
            out.writeUTF(req.getRequest());
            System.out.println(in.readUTF());

        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
