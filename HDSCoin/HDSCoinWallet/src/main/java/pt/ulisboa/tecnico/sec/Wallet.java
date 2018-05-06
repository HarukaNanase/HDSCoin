package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;

public class Wallet {
    public static Socket mainSocket;
    private static PublicKey pKey;
    private static PrivateKey privKey;
    private static String publicKeyString;
    private static String privateKeyString;
    private static DataOutputStream out;
    private static DataInputStream in;
    private static boolean isRegistered = false;
    private static int KEY_SIZE = 2048;
    private static String ALGORITHM = "RSA";
    private static long sequenceNumber = 0;
    private static String serverPublicKeyString;
    private static PublicKey serverPublicKey;
    private static NodeManager manager;

    public static PrivateKey getPrivateKey(){
        return privKey;
    }


    public static void main(String[] args){
        System.out.println("Wallet v0.01");
        manager = new NodeManager();
        Scanner scanner = new Scanner(System.in);

        try{
            loadServerKey(System.getProperty("user.dir") + "/src/main/resources/");
        }catch(Exception e){
            System.out.println("Failed to load server keys @ " + System.getProperty("user.dir") + "/src/main/resources/");
            return;
        }
        try {
            if (args.length == 0) {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(KEY_SIZE);
                KeyPair keyPair = keyGen.generateKeyPair();
                privKey = keyPair.getPrivate();
                pKey = keyPair.getPublic();

                byte[] pubKeyBytes = pKey.getEncoded();
                byte[] privKeyBytes = privKey.getEncoded();

                publicKeyString = Base64.encode(pubKeyBytes, KEY_SIZE);
                privateKeyString = Base64.encode(privKeyBytes, KEY_SIZE);
                X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                        pKey.getEncoded());
                try {
                    FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "/src/main/resources/" + "client.pub");
                    fos.write(x509EncodedKeySpec.getEncoded());
                    fos.close();
                    // Store Private Key.
                    PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                            privKey.getEncoded());
                    fos = new FileOutputStream(System.getProperty("user.dir") + "/src/main/resources/" + "client.priv");
                    fos.write(pkcs8EncodedKeySpec.getEncoded());
                    fos.close();
                } catch (IOException e) {
                    System.out.println("Failed to save key pair to file.");
                    e.printStackTrace();
                }
                System.out.println("Your key: " + publicKeyString);
                System.out.println("Your private key: " + privateKeyString);
            } else {
                System.out.println("Trying to load keys from folder: " + args[0]);
                try {
                    File filePublicKey = new File(System.getProperty("user.dir") + "/src/main/resources/" + args[0] + "/" + "client.pub");
                    FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/" + args[0] + "/" + "client.pub");
                    byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
                    fis.read(encodedPublicKey);
                    fis.close();

                    // Read Private Key.
                    File filePrivateKey = new File(System.getProperty("user.dir") + "/src/main/resources/" + args[0] + "/" + "client.priv");
                    fis = new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/" + args[0] + "/" + "client.priv");
                    byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
                    fis.read(encodedPrivateKey);
                    fis.close();

                    // Generate KeyPair.
                    KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
                    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                            encodedPublicKey);
                    pKey = keyFactory.generatePublic(publicKeySpec);

                    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                            encodedPrivateKey);
                    privKey = keyFactory.generatePrivate(privateKeySpec);

                    byte[] pubKeyBytes = pKey.getEncoded();
                    byte[] privKeyBytes = privKey.getEncoded();

                    publicKeyString = Base64.encode(pubKeyBytes, KEY_SIZE);
                    privateKeyString = Base64.encode(privKeyBytes); // PKCS#8

                    System.out.println("Keys loaded successfully!\nYour key:\n" + publicKeyString);
                } catch (IOException ioe) {
                    System.out.println("Failed to load keys from folder: " + args[0]);
                    return;
                }
            }

            //mainSocket = new Socket("127.0.0.1", 1381);
            //out = new DataOutputStream(mainSocket.getOutputStream());
            //in = new DataInputStream(mainSocket.getInputStream());
            manager.createNode("127.0.0.1", 1380);
            manager.createNode("127.0.0.1", 1381);
            manager.createNode("127.0.0.1", 1382);
            Request test1 = new Request(Opcode.TEST_MESSAGE);
            test1.addParameter(publicKeyString);
            boolean ans = manager.broadcastWrite(test1);
            System.out.println(ans);

            System.out.println("Contacting server to request sequence number...");
            Request seqNumber = new Request(Opcode.REQUEST_SEQUENCE_NUMBER);
            seqNumber.addParameter(publicKeyString);
            Request seq = manager.broadcastRead(seqNumber);
            if (seq != null) {
                String[] values = seq.getParameter(0).split("/");
                sequenceNumber = Long.parseLong(values[0]);
                long wts = Long.parseLong(values[1]);
                long rid = Long.parseLong(values[2]);
                sequenceNumber = sequenceNumber == -1 ? 0 : sequenceNumber;
                manager.setWTS(wts == -1? 0 : wts);
                manager.setRID(rid == -1? 0: rid);
                System.out.println("Manager : WTS: " + wts + " RID: " + rid);
            }
            if (sequenceNumber != -1)
                System.out.println("Done. Current Sequence Number: " + sequenceNumber);
            else {
                System.out.println("Sequence Number not found. Setting to 0");
                sequenceNumber = 0;
            }



            while (true) {
                System.out.println("What you wanna do?");
                String opcode = scanner.next();
                handleUserInput(opcode);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    public static long getSequenceNumber(){
        return sequenceNumber;
    }
    public static Opcode InputToOpcode(String opcode){
       String new_opcode = opcode.toLowerCase();
       if(new_opcode.equals("create_account"))
           return Opcode.CREATE_ACCOUNT;
       else if(new_opcode.equals("check_account"))
           return Opcode.CHECK_ACCOUNT;
       else if(new_opcode.equals("create_transaction"))
           return Opcode.CREATE_TRANSACTION;
       else if(new_opcode.equals("receive_transaction"))
           return Opcode.RECEIVE_TRANSACTION;
       else if(new_opcode.equals("audit"))
           return Opcode.AUDIT;
       else if(new_opcode.equals("request_chain"))
           return Opcode.REQUEST_CHAIN;
       else if(new_opcode.equals("exit"))
           System.exit(0);
       return null;
    }

    public static String receiveAndVerifyAnswer() {
        try {
            String incoming = in.readUTF();
            Request inReq = Request.requestFromJson(incoming);
           // System.out.println(incoming);
            if (inReq.getOpcode() == Opcode.SERVER_ANSWER && SecurityManager.VerifyMessage(inReq, serverPublicKeyString)) {
                return inReq.getParameter(0);
            } else {
                System.out.println("Something is wrong with this message...");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }

    public static void handleUserInput(String opcode){
        Request ureq = new Request(InputToOpcode(opcode));
        if(ureq.getOpcode() == null){
            System.out.println("Unknown command.");
            return;
        }

        if(ureq.getOpcode() != Opcode.CREATE_ACCOUNT && ureq.getOpcode() != Opcode.REQUEST_CHAIN && ureq.getOpcode() != Opcode.AUDIT)
            ureq.setSequenceNumber(++sequenceNumber);

        ureq.addParameter(publicKeyString);
        Scanner scanner = new Scanner(System.in);
        switch(ureq.getOpcode()) {
            case CREATE_ACCOUNT:
                //signAndSendMessage(ureq);
                isRegistered = manager.broadcastWrite(ureq);
                break;
            case CHECK_ACCOUNT:
                //signAndSendMessage(ureq);
                manager.broadcastRead(ureq);
                break;
            case CREATE_TRANSACTION:
                System.out.println("Enter destination address: ");
                String destination = scanner.next();
                System.out.println("How many coins do you want to send : " );
                int value = scanner.nextInt();
                ureq.addParameter(destination);
                ureq.addParameter(Integer.toString(value));
                //signAndSendMessage(ureq);
                manager.broadcastWrite(ureq);
                break;
            case RECEIVE_TRANSACTION:
                System.out.println("Enter the payer's address:");
                String payerAddress = scanner.next();
                ureq.addParameter(payerAddress);
                manager.broadcastWrite(ureq);
                break;
            case REQUEST_CHAIN:
                signAndSendMessage(ureq);
                break;
            case AUDIT:
                System.out.println("Enter the account to be audited:");
                String auditTarget = scanner.next();
                ureq.setParameter(0, auditTarget);
                manager.broadcastRead(ureq);
                break;
        }
    }

    private static void signAndSendMessage(Request ureq){
        SecurityManager.SignMessage(ureq, privKey);
        try {
            out.writeUTF(ureq.requestAsJson());
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
