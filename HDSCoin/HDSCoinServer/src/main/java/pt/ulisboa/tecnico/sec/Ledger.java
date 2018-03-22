package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class Ledger implements Serializable{
    public static ServerSocket mainSocket;

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static ArrayList<Account> accounts = new ArrayList<Account>();
    public static ArrayList<Transaction> backlog = new ArrayList<Transaction>();
    public static int difficulty = 2;
    private static String publicKeyString;
    private static String PrivateKeyString;
    private static PublicKey publicKey;
    private static PrivateKey privKey;
    private static int KEY_SIZE = 512;
    private static String ALGORITHM = "RSA";

    public Ledger() {
    }

    public static void main(String[] args){
        try {
            loadKeys(System.getProperty("user.dir")+"/resources/");
        }catch(IOException ioe){
            System.out.println("Could not load key files. Generating new ones.");
            ioe.printStackTrace();
            generateServerKeys();
        }catch(NoSuchAlgorithmException nsae){
            System.out.println("Incompatible algorithm");
            return;
        }catch(InvalidKeySpecException ikse){
            System.out.println("Invalid or corrupt keys. Generating new ones");
            generateServerKeys();
        }


        Block genesis = new Block("First block", "0");
        AddToBlockChain(genesis);
        Block block2 = new Block("Second block", genesis.hash);
        AddToBlockChain(block2);
        Block block3 = new Block("Third block", block2.hash);
        AddToBlockChain(block3);
        Block block4 = new Block("4th block", block3.hash);
        AddToBlockChain(block4);

        for(Block b : blockchain){
            System.out.println(b.getTransactionsAsJSon());
        }

        try{
            mainSocket = new ServerSocket(1381);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        while(true){
            try {
                final Socket client = mainSocket.accept();
                Thread th = new Thread(new Runnable() {

                    Socket tClient = client;
                    Boolean shouldRun = true;
                    public void run() {
                        while(shouldRun) {
                                System.out.println("New thread responding to client.");
                                handleClientRequest(tClient);
                        }
                    }
                });
                th.start();


            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }

    }

    private static String createAccount(String publicKeyBase64) {
        try {
            byte[] publicKeyBytes = Base64.decode(publicKeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            for (Account a : accounts) {
                if (a.getAccountAddress().equals(publicKeyBase64)) {
                    System.out.println("Account already exists");
                    return "Account already exists with that address";

                }
            }
            Account newUser = new Account(publicKey, publicKeyBase64);
            accounts.add(newUser);
            return "Success! Your account balance: ";
        }catch(Exception e){
            e.printStackTrace();
            return "An error has occured";
        }
    }

    private static String checkAccount(String key){
        StringBuilder sb = new StringBuilder();
        Account acc = getAccount(key);
        if (acc != null) {
            sb.append("Account : " + acc.getAccountAddress() + "\n");
            sb.append("Balance : " + acc.getBalance() + "\n");
        }

        for (Transaction t : backlog) {
            System.out.println("\n" + t.getTransactionInfo() + "\n");
            if (t.getDestinationAddress().equals(key)) {
                sb.append(t.getTransactionInfo());
                sb.append("\n");
            }
        }
        return sb.toString();
    }


    private static String createTransaction(String src, String dst, int value){
        Account acc1 = getAccount(src);
        Account acc2 = getAccount(dst);
        Transaction t;
        if (value <= 0) {
            return "Cannot do a transaction with negative or 0 value.";
        }
        if (acc1 != null && acc2 != null) {
            acc1.setBalance(acc1.getBalance() - value);
            t = new Transaction(acc1, acc2, value);
            backlog.add(t);
            return "Transaction has been sent.";

        } else {
            return "Destination address is unknown.";
        }

    }

    public static String getChain(){
        try {
            StringBuilder sb = new StringBuilder();
            int i = 1;
            for (Block b : blockchain) {
                sb.append("Block " + i + ": " + "\n");
                sb.append(b.getBlockAsJSon());
                sb.append("\n");
                i++;
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching chain.";
        }
    }

    public static void handleClientRequest(Socket client){
        try {
            DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            String treq = in.readUTF();
            Request req = Request.requestFromJson(treq);
            System.out.println("Request Received!");
            System.out.println(req.requestAsJson());

            if (req.getOpcode().equals("CreateAccount")) {
                String publicKeyBase64 = req.getParameter(0);
                out.writeUTF(createAccount(publicKeyBase64));
            }
            else if (req.getOpcode().equals("CheckAccount")) {
                out.writeUTF(checkAccount(req.getParameter(0)));
            }
            else if (req.getOpcode().equals("CreateTransaction")) {
                String src = req.getParameter(0);
                String dst = req.getParameter(1);
                int value = Integer.valueOf(req.getParameter(2));
                out.writeUTF(createTransaction(src,dst,value));
            }
            else if (req.getOpcode().equals("RequestChain")) {
                out.writeUTF(getChain());

            }
            else if(req.getOpcode().equals("ReceiveTransaction")){
                String destinationKey = req.getParameter(0);
                String sourceKey = req.getParameter(1);
                for(Transaction t: backlog){
                    if(t.getSourceAddress().equals(sourceKey) && t.getDestinationAddress().equals(destinationKey)){
                        if(!t.isProcessed()){
                            t.signalToProcess();
                            //test, 1 trasaction per block!
                            Block b = new Block("Transaction Completed", blockchain.get((blockchain.size() - 1)).hash);
                            b.addTransaction(t);
                            AddToBlockChain(b);
                            backlog.remove(t);
                            try{
                                out.writeUTF("Transaction has been accepted! Check your new balance!");
                                return;
                            }catch(Exception e){
                                out.writeUTF("A problem occured.");
                            }
                        }
                    }
                }

            }
            else if(req.getOpcode().equals("RequestServerKey")){
                try{
                    out.writeUTF(publicKeyString);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }catch(Exception e) {
            e.printStackTrace();

        }


    }

    public static Account getAccount(String publicKey){
        try {
            byte[] publicKeyBytes = Base64.decode(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey pubK = keyFactory.generatePublic(publicKeySpec);
            for (Account a : accounts) {
                if (a.getPublicKey().equals(pubK)) {
                    return a;
                }
            }
        }catch(Exception e){
            return null;
        }
        return null;
    }
    public static boolean AddToBlockChain(Block block){
        block.mine(difficulty);
        blockchain.add(block);
        if(verifyChain()) {
            System.out.println("Everything's fine with the chain");
            return true;
        }
        else {
            System.out.println("Chain's corrupted, altered and not viable");
            return false;
        }
    }




    public static boolean verifyChain(){
        Block current;
        Block previous;
        String objective = new String(new char[difficulty]).replace('\0', '0');
        for(int i = 1; i< blockchain.size(); i++){
            current = blockchain.get(i);
            previous = blockchain.get(i-1);
            if(!current.hash.equals(current.calculateHash())){
                System.out.println("Current block hash does not match.");
                return false;
            }
            if(!current.previousBlockHash.equals(previous.hash)){
                System.out.println("Previous block hash does not match current block previous hash.");
                return false;
            }
            if(!current.hash.substring(0, difficulty).equals(objective)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }

        return true;
    }


    private static void generateServerKeys(){
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KEY_SIZE);
            KeyPair keyPair = keyGen.generateKeyPair();
            privKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

            byte[] pubKeyBytes = publicKey.getEncoded();
            byte[] privKeyBytes = privKey.getEncoded();

            publicKeyString = Base64.encode(pubKeyBytes, 512);
            PrivateKeyString = Base64.encode(privKeyBytes); // PKCS#8
            System.out.println("Server Key: " + publicKeyString);
            saveKeys(System.getProperty("user.dir")+"/resources/", publicKey, privKey);
        }catch(Exception e){

        }
    }


    private static void loadKeys(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException{
            File filePublicKey = new File(path + "server.pub");
            FileInputStream fis = new FileInputStream(path + "server.pub");
            byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
            fis.read(encodedPublicKey);
            fis.close();

            // Read Private Key.
            File filePrivateKey = new File(path + "server.priv");
            fis = new FileInputStream(path + "server.priv");
            byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
            fis.read(encodedPrivateKey);
            fis.close();

            // Generate KeyPair.
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                    encodedPublicKey);
            publicKey = keyFactory.generatePublic(publicKeySpec);

            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                    encodedPrivateKey);
            privKey = keyFactory.generatePrivate(privateKeySpec);

    }

    private static void saveKeys(String path, PublicKey publicKey, PrivateKey privateKey){
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                publicKey.getEncoded());
        try {
            FileOutputStream fos = new FileOutputStream(path + "server.pub");
            fos.write(x509EncodedKeySpec.getEncoded());
            fos.close();
            // Store Private Key.
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                    privateKey.getEncoded());
            fos = new FileOutputStream(path + "server.priv");
            fos.write(pkcs8EncodedKeySpec.getEncoded());
            fos.close();
        }catch(IOException e){
            System.out.println("Failed to save key pair to file.");
            e.printStackTrace();
        }

    }






}
