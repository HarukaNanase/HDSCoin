package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

public class Wallet {
    public static Socket mainSocket;
    private static PublicKey pKey;
    private static PrivateKey privKey;
    private static String publicKeyString = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAL4+Qm4IeaIuoV5SATb2BEyuL6tCuqJoblxypcY325sa\\nTKiurtYzWYsQv2HtZ3nuBKul09lX8GI+pJU2uBKOajkCAwEAAQ\\u003d\\u003d";
    private static String privateKeyString;
    private static DataOutputStream out;
    private static DataInputStream in;
    public static void main(String[] args){
        System.out.println("Wallet v0.01");
        Scanner scanner = new Scanner(System.in);
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(512);
            KeyPair keyPair = keyGen.generateKeyPair();
            privKey = keyPair.getPrivate();
            pKey = keyPair.getPublic();

            byte[] pubKeyBytes = pKey.getEncoded();
            byte[] privKeyBytes = privKey.getEncoded();

            publicKeyString = Base64.encode(pubKeyBytes);
            privateKeyString = Base64.encode(privKeyBytes);
            System.out.println("Your key: " + publicKeyString);

            mainSocket = new Socket("127.0.0.1", 1381);
            out = new DataOutputStream(mainSocket.getOutputStream());
            in = new DataInputStream(mainSocket.getInputStream());
            while(true){
                System.out.println("What you wanna do?");
                String opcode = scanner.next();
                handleUserInput(opcode);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void handleUserInput(String opcode){
        Request ureq = new Request(opcode);
        if(opcode.equals("CreateAccount")){
            Scanner scanner = new Scanner(System.in);
            System.out.println("Your key address: " + publicKeyString);

            ureq.addParamemter(publicKeyString);
            try {
                out.writeUTF(ureq.requestAsJson());
                System.out.println(in.readUTF());
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        else if(opcode.equals("CheckAccount")) {
            ureq.addParamemter(publicKeyString);
            try {
                out.writeUTF(ureq.requestAsJson());
                System.out.println(in.readUTF());

            }catch(Exception e){
                e.printStackTrace();
            }
        }else if(opcode.equals("CreateTransaction")){
            System.out.println("Enter destination address: ");
            Scanner scanner = new Scanner(System.in);
            String destination = scanner.next();
            System.out.println("How many coins do you want to send : " );
            int value = scanner.nextInt();
            ureq.addParamemter(publicKeyString);
            ureq.addParamemter(destination);
            ureq.addParamemter(Integer.toString(value));
            try {
                out.writeUTF(ureq.requestAsJson());
                System.out.println(in.readUTF());
            }catch(Exception e){
                e.printStackTrace();

            }

        }else if(opcode.equals("RequestChain")){
            try{
                out.writeUTF(ureq.requestAsJson());
                System.out.println(in.readUTF());
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }
}
