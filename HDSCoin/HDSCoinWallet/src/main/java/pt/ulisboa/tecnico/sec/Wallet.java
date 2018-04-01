package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
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

    private static int KEY_SIZE = 2048;
    private static String ALGORITHM = "RSA";

    private static String serverPublicKeyString;
    private static PublicKey serverPublicKey;

    public static void main(String[] args){
        System.out.println("Wallet v0.01");
        Scanner scanner = new Scanner(System.in);
        try{
            loadServerKey(System.getProperty("user.dir") + "/HDSCoinWallet/resources/");
        }catch(Exception e){
            System.out.println("Failed to load server keys");
            return;
        }
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KEY_SIZE);
            KeyPair keyPair = keyGen.generateKeyPair();
            privKey = keyPair.getPrivate();
            pKey = keyPair.getPublic();

            byte[] pubKeyBytes = pKey.getEncoded();
            byte[] privKeyBytes = privKey.getEncoded();

            publicKeyString = Base64.encode(pubKeyBytes, KEY_SIZE);
            privateKeyString = Base64.encode(privKeyBytes, KEY_SIZE);
            System.out.println("Your key: " + publicKeyString);

            mainSocket = new Socket("127.0.0.1", 1381);
            out = new DataOutputStream(mainSocket.getOutputStream());
            in = new DataInputStream(mainSocket.getInputStream());
            //handleUserInput("RequestServerKey");
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
        ureq.addParameter(publicKeyString);
        if(opcode.equals("CreateAccount")){
            Scanner scanner = new Scanner(System.in);
            System.out.println("Your key address: " + publicKeyString);
           // ureq.addParameter(publicKeyString);

        }
        else if(opcode.equals("CheckAccount")) {
           // ureq.addParameter(publicKeyString);

        }else if(opcode.equals("CreateTransaction")){
            System.out.println("Enter destination address: ");
            Scanner scanner = new Scanner(System.in);
            String destination = scanner.next();
            System.out.println("How many coins do you want to send : " );
            int value = scanner.nextInt();
            //ureq.addParameter(publicKeyString);
            ureq.addParameter(destination);
            ureq.addParameter(Integer.toString(value));

        }else if(opcode.equals("RequestChain")){
                SendMessage(ureq);
                return;

        }else if(opcode.equals("ReceiveTransaction")){
                System.out.println("Enter the payer's address:");
                Scanner scanner = new Scanner(System.in);
                String payerAddress = scanner.next();
               // ureq.addParameter(publicKeyString);
                ureq.addParameter(payerAddress);
        }
        signAndSendMessage(ureq);
    }

    private static void signAndSendMessage(Request ureq){
        SecurityManager.SignMessage(ureq, privKey);
        try {
            out.writeUTF(ureq.requestAsJson());
            System.out.println(in.readUTF());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void SendMessage(Request ureq){
        try {
            out.writeUTF(ureq.requestAsJson());
            System.out.println(in.readUTF());
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    private static void loadServerKey(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File filePublicKey = new File(path + "server.pub");
        FileInputStream fis = new FileInputStream(path + "server.pub");
        byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
        fis.read(encodedPublicKey);
        fis.close();
        // Generate KeyPair.
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                encodedPublicKey);
        serverPublicKey = keyFactory.generatePublic(publicKeySpec);
        byte[] pubKeyBytes = serverPublicKey.getEncoded();
        serverPublicKeyString = Base64.encode(pubKeyBytes, KEY_SIZE);

    }
}
