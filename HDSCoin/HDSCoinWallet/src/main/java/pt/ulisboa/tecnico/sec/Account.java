package pt.ulisboa.tecnico.sec;

import com.google.gson.Gson;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Account {
        private transient int KEY_SIZE = 512;
        private transient PublicKey publicKey;
        private transient PrivateKey privateKey;
        private String publicKeyString;
        private transient String privateKeyString;
        private int balance;
        private Transaction[] backlog;

        public Account(PublicKey pkey){
            this.publicKey = pkey;
            balance = 50;
        }
        public Account(){
            balance = 50;
            this.generateAccountKeys();
        }

        public void generateAccountKeys(){
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(KEY_SIZE);
                KeyPair keyPair = keyGen.generateKeyPair();
                this.privateKey = keyPair.getPrivate();
                this.publicKey = keyPair.getPublic();

                byte[] pubKeyBytes = publicKey.getEncoded();
                byte[] privKeyBytes = privateKey.getEncoded();

                this.publicKeyString = Base64.encode(pubKeyBytes);
                this.privateKeyString = Base64.encode(privKeyBytes); // PKCS#8


                //get the key from the string:
                /*
               // The bytes can be converted back to public and private key objects
               KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
               EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
               PrivateKey privateKey2 = keyFactory.generatePrivate(privateKeySpec);

               EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
               PublicKey publicKey2 = keyFactory.generatePublic(publicKeySpec);
                 */

            }catch(Exception e){

            }
        }

        public void makePayment(int value){
            if((this.balance - value) < 0){
                //error out
            }
            balance -= value;
        }

        public void receivePayment(int value){
            this.balance += value;
        }

        public String getAccountInfo(){
            Gson gson = new Gson();
            return gson.toJson(this);
        }
        public String getAccountAddress(){
            return this.publicKeyString;
        }



}
