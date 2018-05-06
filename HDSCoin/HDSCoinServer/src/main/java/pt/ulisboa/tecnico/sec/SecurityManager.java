package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;


import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

public class SecurityManager {
    private static String algorithm = "RSA";
    private static int MAX_MESSAGE_DELAY = 5000;
    private static String hashAlgorithm = "SHA256";

    public static boolean VerifyMessage(Request request, String sender){
        try {
            if(request.getOpcode() == Opcode.AUDIT || request.getOpcode() == Opcode.TEST_MESSAGE)
                return true;
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
            long currentTime = System.currentTimeMillis();
            boolean validTimer = true;
            if(validSign) {
                if (currentTime > request.getExpiresOn() || currentTime < request.getCreatedOn()) {
                    validTimer = false;
                }
                return (validSign && validTimer);
            }
            System.out.println("Invalid sign");
            return false;

        }catch(Exception e){
            return false;
        }
    }

    public static void SignMessage(Request request, PrivateKey privateKey){
        try {
            if(request.getdSig() != null)
                request.setdSig(null);

            long currentTime = System.currentTimeMillis();
            request.setCreatedOn(currentTime);
            request.setExpiresOn(currentTime + MAX_MESSAGE_DELAY);
            Signature d_sig = Signature.getInstance(hashAlgorithm+"With"+algorithm);
            d_sig.initSign(privateKey);
            d_sig.update(request.requestAsJson().getBytes());
            byte[] sigBytes = d_sig.sign();
            request.setdSig(Base64.encode(sigBytes, 2048));

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


    public static int getMaxMessageDelay(){
        return MAX_MESSAGE_DELAY;
    }

}
