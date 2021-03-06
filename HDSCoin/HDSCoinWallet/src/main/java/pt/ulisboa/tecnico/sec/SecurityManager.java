package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SecurityManager {
    private static String algorithm = "RSA";
    private static long MAX_MESSAGE_DELAY = 15000;
    private static String hashAlgorithm = "SHA256";

    public static boolean VerifyMessage(Request request, String sender){
        try {
            if(request.getOpcode() == Opcode.AUDIT)
                return true;
            long currentTime = System.currentTimeMillis();
            boolean validTimer = true;
            String signature = request.getdSig();
            byte[] signatureBytes = Base64.decode(signature);
            request.setdSig(null);
            Signature sign = Signature.getInstance(hashAlgorithm+"With"+algorithm);
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(sender));
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            byte[] data = request.requestAsJson().getBytes();
            request.setdSig(signature);
            sign.initVerify(publicKey);
            sign.update(data);
            boolean validSign = sign.verify(signatureBytes);
            if(validSign) {
                if (currentTime > request.getExpiresOn() || currentTime < request.getCreatedOn()) {
                    validTimer = false;
                }
                return (validSign && validTimer);
            }

            return false;

        }catch(Exception e){
            return false;
        }
    }

    public static void SignMessage(Request request, PrivateKey privateKey){
        try {
            if(request.getdSig() != null)
                request.setdSig(null);
            if(request.getSequenceNumber() == 0)
                request.setSequenceNumber(Wallet.getSequenceNumber());
            long currentTime = System.currentTimeMillis();
            request.setCreatedOn(currentTime);
            request.setExpiresOn(currentTime + MAX_MESSAGE_DELAY);
            Signature d_sig = Signature.getInstance(hashAlgorithm+"With"+algorithm);
            d_sig.initSign(privateKey);
            d_sig.update(request.requestAsJson().getBytes());
            byte[] sigBytes = d_sig.sign();
            request.setdSig(Base64.encode(sigBytes, 2048));
           // System.out.println("Final Request: \n " + request.requestAsJson());
        }catch(NoSuchAlgorithmException noae){
            noae.printStackTrace();
        }catch(InvalidKeyException ike){
            ike.printStackTrace();
        }catch(SignatureException se){
            se.printStackTrace();
        }
    }

    public static String SignMessage(String message, PrivateKey privateKey){
        try {

            Signature d_sig = Signature.getInstance(hashAlgorithm+"With"+algorithm);
            d_sig.initSign(privateKey);
            d_sig.update(message.getBytes());
            byte[] sigBytes = d_sig.sign();
            return Base64.encode(sigBytes);

        }catch(NoSuchAlgorithmException noae){
            noae.printStackTrace();
            return null;
        }catch(InvalidKeyException ike){
            ike.printStackTrace();
            return null;
        }catch(SignatureException se){
            se.printStackTrace();
            return null;
        }
    }

    public static boolean VerifySequenceNumber(Request request, Account user){
        System.out.println("\n\n This Account Sequence Number: " + user.getSequenceNumber() + "\n\n");
        if(request.getSequenceNumber() == (user.getSequenceNumber()+1)){
            user.setSequenceNumber(request.getSequenceNumber());
            return true;
        }
        return false;
    }




}
