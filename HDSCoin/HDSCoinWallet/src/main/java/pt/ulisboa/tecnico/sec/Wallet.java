package pt.ulisboa.tecnico.sec;

import java.io.*;
import java.net.Socket;

public class Wallet {
    public static Socket mainSocket;

    public static void main(String[] args){
        System.out.println("Wallet v0.01");
        try {
            mainSocket = new Socket("127.0.0.1", 1381);
            DataOutputStream out = new DataOutputStream(mainSocket.getOutputStream());
            out.writeUTF("HELO");

        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
