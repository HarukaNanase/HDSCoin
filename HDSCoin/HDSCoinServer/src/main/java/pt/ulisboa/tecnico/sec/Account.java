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

        public Account(String publicKeyString){
            this.publicKeyString = publicKeyString;
            this.balance = 50;
        }

        public Account(PublicKey pkey, String publicKeyString){
            this.publicKey = pkey;
            this.publicKeyString = publicKeyString;
            this.balance = 50;
        }

        public Account(){
            balance = 50;
            this.generateAccountKeys();
        }

        public String getPublicKeyString(){
            return this.publicKeyString;
        }

        public PublicKey getPublicKey(){ return this.publicKey;}

        public int getBalance(){
            return this.balance;
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


    public void setBalance(int balance) {
        this.balance = balance;
    }
}
