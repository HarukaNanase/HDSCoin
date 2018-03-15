package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Wallet {
    public static Socket mainSocket;
    private static PublicKey pKey;
    private static PrivateKey privKey;
    private static String publicKeyString = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAL4+Qm4IeaIuoV5SATb2BEyuL6tCuqJoblxypcY325sa\\nTKiurtYzWYsQv2HtZ3nuBKul09lX8GI+pJU2uBKOajkCAwEAAQ\\u003d\\u003d";
    private static String privateKeyString;
    public static void main(String[] args){
        System.out.println("Wallet v0.01");
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

            mainSocket = new Socket("127.0.0.1", 1381);
            DataOutputStream out = new DataOutputStream(mainSocket.getOutputStream());
            DataInputStream in = new DataInputStream(mainSocket.getInputStream());
            Request req = new Request("CreateAccount");
            req.addParamemter(publicKeyString);
            out.writeUTF(req.requestAsJson());
            System.out.println(in.readUTF());

            req = new Request("CheckAccount");
            req.addParamemter(publicKeyString);
            out.writeUTF(req.requestAsJson());
            System.out.println(in.readUTF());

        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
