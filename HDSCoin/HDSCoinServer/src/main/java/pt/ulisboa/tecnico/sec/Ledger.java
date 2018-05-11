package pt.ulisboa.tecnico.sec;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Time;
import java.util.*;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Ledger{
    public transient ServerSocket mainSocket;
    public transient ServerSocket nodeEndpoint;
    //private transient NodeManager manager;

    //public ArrayList<Block> blockchain = new ArrayList<Block>();
    public ArrayList<Account> accounts = new ArrayList<Account>();
    public ArrayList<Transaction> backlog = new ArrayList<Transaction>();
    public int difficulty = 2;
    private transient String publicKeyString;
    private transient String privateKeyString;
    private transient PublicKey publicKey;
    private transient PrivateKey privKey;
    private int KEY_SIZE = 2048;
    private transient String KEYSTORE_PASSWORD = "sec2018";
    private transient String ALGORITHM = "RSA";
    private transient static Ledger ledger = null;
    private int port;
    private String LEDGER_NAME = null;
    private String RESOURCES_PATH = System.getProperty("user.dir")+"/src/main/resources/";


    private Ledger() {

    }

    public static Ledger getInstance(){
        if(ledger == null)
            ledger = new Ledger();
        return ledger;
    }

    public static void main(String[] args){
        ledger = getInstance();

        if(args[0] == null){
            System.out.println("Please indicate which ledger this is.");
            return;
        }
        ledger.RESOURCES_PATH += args[0]+"/";

        if(args[0].equals("ledger1"))
            ledger.port = 1380;
        else if(args[0].equals("ledger2"))
            ledger.port = 1381;
        else if(args[0].equals("ledger3"))
            ledger.port = 1382;
        else if(args[0].equals("ledger4"))
            ledger.port = 1383;

        try {
            ledger.nodeEndpoint = new ServerSocket(ledger.port + 1000);
        }catch(Exception e){
            System.out.println("Failed to set up node listener endpoint.");
        }


        Thread listener = new Thread(new Runnable(){
            public void run(){
                while(true){
                    ledger.listenerNode();
                }
            }
        });

        boolean loaded = ledger.loadLedgerState(ledger.RESOURCES_PATH);

        try {
            ledger.loadKeys(ledger.RESOURCES_PATH, args[0]);
        }catch(IOException ioe){
            System.out.println("Could not load key files. Generating new ones.");
            ioe.printStackTrace();
            ledger.generateServerKeys();
        }catch(NoSuchAlgorithmException nsae){
            System.out.println("Incompatible algorithm");
            return;
        }catch(InvalidKeySpecException ikse){
            System.out.println("Invalid or corrupt keys. Generating new ones");
            ledger.generateServerKeys();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }


        try{
            System.out.println("Server Endpoint: " + "127.0.0.1:" + ledger.port);
            ledger.mainSocket = new ServerSocket(ledger.port);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        while(true) try {
            final Socket client = ledger.mainSocket.accept();
            Thread th = new Thread(new Runnable() {

                Socket tClient = client;
                Boolean shouldRun = true;

                public void run() {
                    System.out.println("New thread responding to client.");
                    while (shouldRun) {
                        try {
                            handleClientRequest(tClient);
                        } catch (Exception e) {
                            handleClientDisconnect(tClient);
                            System.out.println("Client exiting");
                            shouldRun = false;
                        }
                    }
                }
            });
            th.start();


        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

    }

    private void listenerNode() {
        while (true) {
            try {
                final Socket node = ledger.nodeEndpoint.accept();
                Thread th = new Thread(new Runnable() {

                    Socket listenNode = node;
                    Boolean nodeRun = true;
                    public void run() {
                        System.out.println("Node Listener setup");
                        while (nodeRun) {
                            try {
                                handleNodeListen(listenNode);
                            } catch (Exception e) {
                                handleClientDisconnect(listenNode);
                                System.out.println("Client exiting");
                                nodeRun = false;
                            }
                        }
                    }
                });
                th.start();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public PrivateKey getPrivateKey(){
        return this.privKey;
    }


    private static void handleNodeListen(Socket socket){

    }
    private static void handleClientDisconnect(Socket client){
        try{
            client.close();
        }catch(SocketException se){
            se.printStackTrace();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public synchronized String createAccount(String publicKeyBase64) {
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
            return "Success! Your account balance: " + newUser.getBalance();
        }catch(Exception e){
            e.printStackTrace();
            return "An error has occured";
        }
    }

    public synchronized String checkAccount(String key){
        StringBuilder sb = new StringBuilder();
        Account acc = getAccount(key);
        if (acc != null) {
            sb.append("Account : " + acc.getAccountAddress() + "\n");
            sb.append("Balance : " + acc.getBalance() + "\n");
        }else{
            sb.append("Account not found. \n");
            return sb.toString();
        }
        //sb.append("Incoming transactions pending confirmation:\n");
        for (Transaction t : backlog) {
            if (t.getDestinationAddress().equals(key)) {
                sb.append("Transaction ID: " + t.getTransactionId()+"\n");
                sb.append("Sender key: " + t.getSourceAddress()+"\n");
                sb.append("Value: " + t.getValue()+"\n");
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public synchronized String auditAccount(String key){
        StringBuilder sb = new StringBuilder();

        Account acc = getAccount(key);
        if(acc == null)
            return "Account not found.";
        sb.append("Audit for account:\n" + key + "\n");
        sb.append("\nPending Transactions: \n");
        for(Transaction t : backlog){
            sb.append("Transaction ID: " + t.getTransactionId() +"\n");
            sb.append("Sender: " + t.getSourceAddress() + "\n");
            sb.append("Value: " + t.getValue()+ "\n");
            sb.append("\n");
        }
        sb.append("\nCompleted Transactions: \n");
        for(Block b: acc.getBlockChain()){
            for(Transaction t: b.getBlockTransactions()){
                if(t.getDestinationAddress().equals(key) || t.getSourceAddress().equals(key)){
                    sb.append("\n*********** TRANSACTION START ***********\n");
                    sb.append("Transaction ID: " + t.getTransactionId() + "\n");
                    sb.append("Receiver ID: " + t.getReceiverId() + "\n");
                    sb.append("From: " + t.getSourceAddress() + "\n");
                    sb.append("To: " + t.getDestinationAddress() + "\n");
                    sb.append("Value: " + t.getValue() +"\n");
                    sb.append("*********** TRANSACTION END ***********\n");
                }
            }
        }
        return sb.toString();
    }

    public synchronized String createTransaction(String src, String dst, int value, String srcSig){
        Account acc1 = getAccount(src);
        Account acc2 = getAccount(dst);

        if (value <= 0) {
            return "Cannot do a transaction with negative or 0 value.";
        }
        if (acc1 != null && acc2 != null) {
            if( value > acc1.getBalance()){
                return "Not enough balance.";
            }
            acc1.setBalance(acc1.getBalance() - value);
            Transaction t = new Transaction(acc1, acc2, value, srcSig);
            t.settSig(srcSig);
            backlog.add(t);
            Block b = new Block("Sent Transaction", acc1);
            b.addTransaction(t);
            acc1.addBlockToBlockChain(b);
            System.out.println("Dead.");
            return "Transaction has been sent.";

        } else {
            return "Destination address is unknown.";
        }

    }

    public  String getChain(){
        try {
            StringBuilder sb = new StringBuilder();
            int i = 1;
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching chain.";
        }
    }

    private static Request createResponse(String message, long RID){
        Request req = new Request(Opcode.SERVER_ANSWER);
        req.addParameter(message);
        req.setRID(RID);
        req.setNodeID(ledger.publicKeyString);
        return req;
    }

    private static Request createReadResponse(String message, Account acc){
        Request req = new Request(Opcode.SERVER_ANSWER);
        req.addParameter(message);
        req.setRID(acc.getRID());
        req.setWTS(acc.getWTS());
        req.setNodeID(ledger.publicKeyString);
        System.out.println("THIS RID: " + req.getRID());
        System.out.println("THIS WTS: " + req.getWTS());
        return req;
    }

    private static Request createWriteResponse(Request request){
        Request ack = new Request(Opcode.ACK);
        ack.addParameter(""+request.getWTS());
        ack.setWTS(request.getWTS());
        request.setNodeID(ledger.publicKeyString);
        return ack;
    }

    private static void sendResponseToClient(Request request, DataOutputStream out){
        try{
            SecurityManager.SignMessage(request, ledger.privKey);
            //System.out.println("Client Answer:\n" + request.requestAsJson());
            out.writeUTF(request.requestAsJson());
            System.out.println("Response sent!");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static Request createNOACKResponse(Request request){
        Request no_ack = new Request(Opcode.NO_ACK);
        no_ack.setWTS(request.getWTS());
        request.setNodeID(ledger.publicKeyString);
        return no_ack;
    }

    public static void handleClientRequest(Socket client) throws SocketException, IOException{
            System.out.println("Awaiting client input");
            DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            String treq = in.readUTF();
            Request req = Request.requestFromJson(treq);
            System.out.println("Request Received! OP:" + req.getOpcode());
            //System.out.println(req.requestAsJson());
            if(req.getOpcode() == Opcode.TEST_MESSAGE){
                sendResponseToClient(createWriteResponse(req), out);
                return;
            }

            String publicKeyBase64 = req.getParameter(0);
            if((req.getOpcode() != Opcode.REQUEST_CHAIN && req.getOpcode() != Opcode.AUDIT)){
                if(!SecurityManager.VerifyMessage(req, publicKeyBase64)){
                    System.out.println("Failed to check signature for : " + req.getOpcode());
                    sendResponseToClient(createResponse("Invalid Message", req.getRID()), out);
                    return;
                }
                if(req.getOpcode() != Opcode.CREATE_ACCOUNT && req.getOpcode() != Opcode.REQUEST_SEQUENCE_NUMBER){
                    Account acc = ledger.getAccount(publicKeyBase64);
                    if(acc == null){
                        System.out.println("Account not found: " + req.getOpcode());
                        sendResponseToClient(createResponse("Account not found in our records", req.getRID()), out);
                        return;
                    }
                    if(!SecurityManager.VerifySequenceNumber(req, acc) && req.getOpcode() != Opcode.WRITE_BACK){
                        System.out.println("Incorrect Sequence Number: " + req.getSequenceNumber() + " Should be: " + acc.getSequenceNumber());
                        sendResponseToClient(createResponse("Incorrect Sequence Number.", req.getRID()), out);
                        return;
                    }
                }
            }else{
                System.out.println("Valid message!");
            }
            System.out.println("Valid message!");
            Gson gson = new Gson();
            switch(req.getOpcode()){
                case CREATE_ACCOUNT:
                    //sendResponseToClient(createResponse(ledger.createAccount(publicKeyBase64), req.getRID()), out);
                        ledger.createAccount(publicKeyBase64);
                        Account acc2 = ledger.getAccount(publicKeyBase64);
                        acc2.setWTS(req.getWTS());
                        ledger.saveLedgerState(ledger.RESOURCES_PATH);
                        sendResponseToClient(createWriteResponse(req), out);
                        break;
                case CHECK_ACCOUNT:
                    Account acc = ledger.getAccount(publicKeyBase64);
                    acc.setRID(req.getRID());
                    sendResponseToClient(createReadResponse(ledger.checkAccount(publicKeyBase64), acc), out);
                    break;
                case CREATE_TRANSACTION:
                    String src = req.getParameter(0);
                    Account srcAcc = ledger.getAccount(src);
                    System.out.println("ACC WTS: " + srcAcc.getWTS() + " REQ WTS: " + req.getWTS());
                    String dst = req.getParameter(1);
                    int value = Integer.valueOf(req.getParameter(2));
                    String tSign = req.getParameter(3);
                    if(((srcAcc.getWTS()+1) == req.getWTS())) {
                        System.out.println("Creating transaction..");
                        System.out.println(ledger.createTransaction(src, dst, value, tSign));
                        System.out.println("Transaction created.");
                        srcAcc.setWTS(req.getWTS());
                        if(srcAcc.getQueue().size() > 0){
                            while(srcAcc.getQueue().get(0).getWTS() == srcAcc.getWTS()+1){
                                Request toProcess = srcAcc.getQueue().get(0);
                                srcAcc.getQueue().remove(0);
                                ledger.processWriteRequest(toProcess);
                                srcAcc.setWTS(toProcess.getWTS());
                            }
                        }
                    }else if(req.getWTS() > (srcAcc.getWTS() + 1)){
                        srcAcc.getQueue().add(req);
                        srcAcc.getQueue().sort(RequestComparator.WTS);
                    }

                    System.out.println("Finishing transaction...");
                    sendResponseToClient(createWriteResponse(req), out);
                    ledger.saveLedgerState(ledger.RESOURCES_PATH);
                    break;

                case RECEIVE_TRANSACTION:
                    Account rec = ledger.getAccount(publicKeyBase64);
                    if((rec.getWTS()+1) == req.getWTS()) {
                        System.out.println("Valid WTS (" + rec.getWTS() + "<" + req.getWTS() + "). Writting request.");
                        ledger.ReceiveTransaction(req);
                        rec.setWTS(req.getWTS());
                        if(rec.getQueue().size() > 0){
                            while(rec.getQueue().get(0).getWTS() == rec.getWTS()+1){
                                Request toProcess = rec.getQueue().get(0);
                                rec.getQueue().remove(0);
                                ledger.processWriteRequest(toProcess);
                                rec.setWTS(toProcess.getWTS());
                            }
                        }
                    }else if(req.getWTS() > (rec.getWTS() + 1)){
                        rec.getQueue().add(req);
                        rec.getQueue().sort(RequestComparator.WTS);
                    }
                    //ledger.ReceiveTransaction(req);
                    sendResponseToClient(createWriteResponse(req), out);
                    ledger.saveLedgerState(ledger.RESOURCES_PATH);
                    break;
                case REQUEST_CHAIN:
                    sendResponseToClient(createResponse(ledger.getChain(), req.getRID()),out);
                    break;
                case AUDIT:
                    Account acc1 = ledger.getAccount(publicKeyBase64);
                    acc1.setRID(req.getRID());
                    sendResponseToClient(createReadResponse(ledger.auditAccount(publicKeyBase64), acc1), out);
                    break;
                case REQUEST_SEQUENCE_NUMBER:
                    String sq = ""+ledger.getAccountSequenceNumber(publicKeyBase64);
                    String wts = ""+ledger.getAccountWTS(publicKeyBase64);
                    String rid = ""+ledger.getAccountRID(publicKeyBase64);
                    sendResponseToClient(createResponse(sq+"/"+wts+"/"+rid, req.getRID()), out);
                    break;
                case WRITE_BACK:
                        System.out.println("Starting Write Back for Client: " + req.getParameter(0));
                        String recent_data = req.getParameter(1);
                        HashMap<String, String> data = gson.fromJson(recent_data, new TypeToken<HashMap<String,String>>(){}.getType());
                        Account acc_hi = null;
                        ArrayList<Transaction> acc_backlog = null;

                        for(Map.Entry<String, String> entry : data.entrySet()){
                            if(entry.getKey().equals("account")){
                                acc_hi = gson.fromJson(entry.getValue(), Account.class);
                            }else if(entry.getKey().equals("backlog")){
                                acc_backlog = gson.fromJson(entry.getValue(), new TypeToken<ArrayList<Transaction>>(){}.getType());
                            }
                        }
                        if(acc_hi == null || acc_backlog == null) {
                            sendResponseToClient(createNOACKResponse(req), out);
                            return;
                        }

                        for(Block b : acc_hi.getBlockChain()){
                            for(Transaction t : b.getBlockTransactions()){
                                if(t.getTSig() != null)
                                    if(!SecurityManager.VerifyMessage(t.getSourceAddress()+t.getDestinationAddress()+t.getValue(), t.getTSig(), t.getSourceAddress())){
                                        System.out.println("invalid state (Transaction TSig). NO_ACK");
                                        sendResponseToClient(createNOACKResponse(req), out);
                                        return;
                                    }
                                if(t.getRSig() != null) {
                                    if (!SecurityManager.VerifyMessage(t.getDestinationAddress() + t.getSourceAddress() + t.getTransactionId(), t.getRSig(), t.getDestinationAddress())) {
                                        System.out.println("Invalid RSig. NO_ACK");
                                        sendResponseToClient(createNOACKResponse(req), out);
                                        return;
                                    }
                                }

                            }
                        }
                        if(!acc_hi.verifyChain()){
                            System.out.println("Failed to verify chain integrity.");
                        }
                        for(Transaction t : acc_backlog){
                            if(t.getTSig() != null) {
                                if (!SecurityManager.VerifyMessage(t.getSourceAddress() + t.getDestinationAddress() + t.getValue(), t.getTSig(), t.getSourceAddress())) {
                                    System.out.println("invalid state (Transaction TSig) on backlog. NO_ACK");
                                    sendResponseToClient(createNOACKResponse(req), out);
                                    return;

                                }
                            }
                            if(t.getRSig() != null) {
                                if (!SecurityManager.VerifyMessage(t.getDestinationAddress() + t.getSourceAddress() + t.getTransactionId(), t.getRSig(), t.getDestinationAddress())) {
                                    System.out.println("Invalid RSig backlog. NO_ACK");
                                    sendResponseToClient(createNOACKResponse(req), out);
                                    return;
                                }
                            }
                        }
                        System.out.println("This state is gud.");
                        Account this_account = ledger.getAccount(publicKeyBase64);
                        this_account.setTransactionId(acc_hi.getTransactionId());
                        this_account.setSequenceNumber(acc_hi.getSequenceNumber());
                        this_account.setWTS(acc_hi.getWTS());
                        this_account.setRID(acc_hi.getRID());
                        this_account.setQueue(acc_hi.getQueue());
                        this_account.setBalance(acc_hi.getBalance());
                        this_account.setBlockchain(acc_hi.getBlockChain());
                        ledger.backlog.removeIf(t->t.getSourceAddress().equals(this_account.getPublicKeyString()));
                        for(Transaction t : acc_backlog){
                            if(!ledger.backlog.contains(t)){
                                ledger.backlog.add(t);
                            }
                        }

                        sendResponseToClient(createWriteResponse(req),out);
                        ledger.saveLedgerState(ledger.RESOURCES_PATH);


                        break;
                case GET_CURRENT_STATE:
                    HashMap<String, String> currentData = new HashMap<>();
                    Account acc_state = ledger.getAccount(req.getParameter(0));
                    System.out.println("Starting to gather info...");
                    String accountInfo = gson.toJson(acc_state);
                    ArrayList<Transaction> accountBacklog = new ArrayList<>();
                    for(Transaction t : ledger.backlog){
                        if(t.getSourceAddress().equals(acc_state.getPublicKeyString())){
                            accountBacklog.add(t);
                        }
                    }
                    String accountBack = gson.toJson(accountBacklog);
                    currentData.put("account", accountInfo);
                    currentData.put("backlog", accountBack);
                    String map_string = gson.toJson(currentData);
                    System.out.println("STATE SIZE : " + map_string.getBytes().length);
                    sendResponseToClient(createReadResponse(map_string, acc_state),out);
                    System.out.println("Sent ledger state to client.");
                    break;

                default:
                    sendResponseToClient(createResponse("Unrecognized command.", req.getRID()), out);
                    System.out.println("WTF");
                    break;
            }


    }


    public synchronized String serializeMap(HashMap<String, ArrayList<String>> serializable){
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, ArrayList<String>>>(){}.getType();
        return gson.toJson(serializable, type);

    }

    public synchronized HashMap<String, ArrayList<String>> ledgerFromJSON(String ledgerInfo){
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, ArrayList<String>>>(){}.getType();
        return gson.fromJson(ledgerInfo, type);

    }

    public synchronized void processWriteRequest(Request req) {
        switch (req.getOpcode()) {
            case CREATE_TRANSACTION:
                String src = req.getParameter(0);
                String dst = req.getParameter(1);
                int value = Integer.valueOf(req.getParameter(2));
                String tSign = req.getParameter(3);
                Account srcAcc = ledger.getAccount(src);
                ledger.createTransaction(src, dst, value, tSign);
                break;
            case RECEIVE_TRANSACTION:
                ledger.ReceiveTransaction(req);
                break;
        }
    }

    public synchronized long getAccountWTS(String key){
        Account acc = getAccount(key);
        if(acc != null)
            return acc.getWTS();
        return -1;
    }

    public synchronized long getAccountRID(String key){
        Account acc = getAccount(key);
        if(acc != null)
            return acc.getRID();
        return -1;
    }

    public synchronized long getAccountSequenceNumber(String key){
        Account acc = getAccount(key);
        if(acc != null)
            return acc.getSequenceNumber();
        return -1;
    }


    public synchronized String ReceiveTransaction(Request req){

        String sourceKey = req.getParameter(1);
        String destinationKey = req.getParameter(0);
        String rSign = req.getParameter(2);
        String transactionId = req.getParameter(3);
        for(Transaction t: backlog){
            if(t.getSourceAddress().equals(sourceKey) && t.getDestinationAddress().equals(destinationKey) && t.getTransactionId() == Long.parseLong(transactionId)){
                if(!t.isProcessed()){
                    if(!SecurityManager.VerifyMessage(t.getSourceAddress() + t.getDestinationAddress() + t.getValue(), t.getTSig(), t.getSourceAddress()))
                        System.out.println("Wrong sender signature. Transaction corrupted?");
                    if(!SecurityManager.VerifyMessage(t.getDestinationAddress() + t.getSourceAddress() + t.getTransactionId(), rSign, t.getDestinationAddress()))
                        System.out.println("Wrong receiver signature. Transaction corrupted?");
                    t.setRSig(rSign);
                    t.setReceiverId(ledger.getAccount(t.getDestinationAddress()).getTransactionId());
                    //ledger.getAccount(t.getDestinationAddress()).setTransactionId(t.getReceiverId()+1);
                    String transactionSignature = SecurityManager.SignMessage(t.getTransactionInfo(), privKey);
                    if(transactionSignature != null)
                        t.settSig(transactionSignature);
                    else
                        return "An error has occured while processing this transaction. Please try again.";

                    t.signalToProcess();
                    //test, 1 trasaction per block!
                    ledger.getAccount(destinationKey).addTransactionToBlockChain(t);
                    backlog.remove(t);
                    return "Transaction has been accepted! Check your new balance!";
                }
                return "No transaction to be processed with for the addresses.";
            }
        }
        return "Transaction not found. Re-check your payer's address.";
    }


    public synchronized Account getAccount(String publicKey){

        for (Account a : accounts) {
            if (a.getAccountAddress().equals(publicKey)) {
                return a;
            }
        }

        return null;
    }

    /*
    public synchronized boolean AddToBlockChain(Block block){
        block.mine(difficulty);
        blockchain.add(block);
        if(verifyChain()) {
            System.out.println("Everything's fine with the chain");
            return true;
        }
        else {
            //System.out.println("Chain's corrupted, altered and not viable");
            return false;
        }
    }
    */



    public void generateServerKeys(){
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KEY_SIZE);
            KeyPair keyPair = keyGen.generateKeyPair();
            privKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

            byte[] pubKeyBytes = publicKey.getEncoded();
            byte[] privKeyBytes = privKey.getEncoded();

            publicKeyString = Base64.encode(pubKeyBytes, KEY_SIZE);
            privateKeyString = Base64.encode(privKeyBytes, KEY_SIZE); // PKCS#8
            System.out.println("Server Key: " + publicKeyString);
            saveKeys(ledger.RESOURCES_PATH, publicKey, privKey);
        }catch(Exception e){

        }
    }

    public void loadKeys(String path, String ledger) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException, UnrecoverableKeyException, CertificateException {
            FileInputStream fis = new FileInputStream(path + "serverkeystore"+ledger);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(fis, KEYSTORE_PASSWORD.toCharArray());

            String alias = ledger;

            privKey = (PrivateKey) keystore.getKey(alias, KEYSTORE_PASSWORD.toCharArray());
            if (privKey != null) {
                // Get certificate of public key
                Certificate cert = keystore.getCertificate(alias);
                // Get public key
                publicKey = cert.getPublicKey();

                byte[] pubKeyBytes = publicKey.getEncoded();
                byte[] privKeyBytes = privKey.getEncoded();

                publicKeyString = Base64.encode(pubKeyBytes, KEY_SIZE);
                privateKeyString = Base64.encode(privKeyBytes); // PKCS#8
            }

    }

    public void saveKeys(String path, PublicKey publicKey, PrivateKey privateKey){
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

    public synchronized boolean saveLedgerState(String path){
        try{
            System.out.println("Starting atomic backup.");
            Gson gson = new Gson();
            PrintWriter out = new PrintWriter(path+"ledger.tmp");
            out.println(gson.toJson(ledger));
            out.close();
        }
        catch(IOException ioe){
            System.out.println("Failed to save state.");
            return false;
        }

        try {
                Path folder = Paths.get(path);
                Path tmp = folder.resolve("ledger.tmp");
                Path finalFile = folder.resolve("ledger.bak");
                Files.move(tmp, finalFile, ATOMIC_MOVE);

            System.out.println("Finished atomic backup.");
                return true;

        }catch(AccessDeniedException ade){
                System.out.println("Please run this program from an administrator console.");
                return false;
        }catch(IOException ioe){
            System.out.println("Something went wrong with the renaming");
            ioe.printStackTrace();
            return false;
        }
    }

    public boolean loadLedgerState(String path){
        try {
            System.out.println("Trying to load backup state...");
            Scanner fileScanner = new Scanner(new File(path+"ledger.bak"));
            String jsonBackup = fileScanner.useDelimiter("\\Z").next();
            fileScanner.close();
            Gson gson = new Gson();
            Ledger backup = gson.fromJson(jsonBackup, Ledger.class);
            ledger.accounts = backup.accounts;
            System.out.println("Loaded accounts: " + ledger.accounts.size());
            //System.out.println(gson.toJson(ledger.accounts));
            //System.out.println("Current Blockchain loaded: \n" + gson.toJson(ledger.blockchain));
            ledger.backlog = backup.backlog;
            ledger.difficulty = backup.difficulty;
            ledger.KEY_SIZE = backup.KEY_SIZE;
            ledger.ALGORITHM = backup.ALGORITHM;
            System.out.println("Backup loaded successfully.");
            return true;
        }catch(IOException ioe){
            System.out.println("Failed to load a backup, starting empty.");
            return false;
        }
    }







}
