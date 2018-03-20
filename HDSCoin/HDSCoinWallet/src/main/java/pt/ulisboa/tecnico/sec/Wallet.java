package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

public class Wallet {
    public static Socket mainSocket;
    private static PublicKey pKey;
    private static PrivateKey privKey;
    private static String publicKeyString;
    private static String privateKeyString;
    private static DataOutputStream out;
    private static DataInputStream in;

    private static int KEY_SIZE = 512;

    private static String serverPublicKeyString;
    private static PublicKey serverPublicKey;

    public static void main(String[] args){
        System.out.println("Wallet v0.01");
        Scanner scanner = new Scanner(System.in);
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KEY_SIZE);
            KeyPair keyPair = keyGen.generateKeyPair();
            privKey = keyPair.getPrivate();
            pKey = keyPair.getPublic();

            byte[] pubKeyBytes = pKey.getEncoded();
            byte[] privKeyBytes = privKey.getEncoded();

            publicKeyString = Base64.encode(pubKeyBytes, 512);
            privateKeyString = Base64.encode(privKeyBytes, 512);
            System.out.println("Your key: " + publicKeyString);

            mainSocket = new Socket("127.0.0.1", 1381);
            out = new DataOutputStream(mainSocket.getOutputStream());
            in = new DataInputStream(mainSocket.getInputStream());
            handleUserInput("RequestServerKey");
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
            Scanner scanner = new Scanner(System.in);
            ureq.addParamemter(scanner.next());

            //ureq.addParamemter(publicKeyString);
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

        }else if(opcode.equals("RequestServerKey")){
            try{
                out.writeUTF(ureq.requestAsJson());
                serverPublicKeyString = in.readUTF();
                byte[] publicKeyBytes = Base64.decode(serverPublicKeyString);
                // The bytes can be converted back to public and private key objects
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");

                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                serverPublicKey = keyFactory.generatePublic(publicKeySpec);
                System.out.println("Server Key: " + serverPublicKeyString);

            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }
}
