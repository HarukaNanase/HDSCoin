package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;


import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

public class SecurityManager {
    private static String algorithm = "RSA";
    private static long MAX_MESSAGE_DELAY = 5000;
    public static boolean VerifyMessage(Request request, String sender){
        try {
            long currentTime = System.currentTimeMillis();
            boolean validTimer = true;
            String signature = request.getdSig();
            byte[] signatureBytes = Base64.decode(signature);
            request.setdSig(null);
            Signature sign = Signature.getInstance("SHA1WithRSA");
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(sender));
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            byte[] data = request.requestAsJson().getBytes();
            request.setdSig(signature);
            sign.initVerify(publicKey);
            sign.update(data);
            if(currentTime > request.getExpiresOn() || currentTime < request.getCreatedOn()){
                validTimer = false;
            }
            return (sign.verify(signatureBytes) && validTimer);
        }catch(Exception e){
            return false;
        }
    }

    public static void SignMessage(Request request, PrivateKey privateKey){
        try {
            request.setCreatedOn(System.currentTimeMillis());
            request.setExpiresOn(System.currentTimeMillis() + MAX_MESSAGE_DELAY);
            Signature d_sig = Signature.getInstance("SHA1WithRSA");
            d_sig.initSign(privateKey);
            d_sig.update(request.requestAsJson().getBytes());
            byte[] sigBytes = d_sig.sign();
            request.setdSig(Base64.encode(sigBytes, 1024));

        }catch(NoSuchAlgorithmException noae){
            noae.printStackTrace();
        }catch(InvalidKeyException ike){
            ike.printStackTrace();
        }catch(SignatureException se){
            se.printStackTrace();
        }
    }



}
