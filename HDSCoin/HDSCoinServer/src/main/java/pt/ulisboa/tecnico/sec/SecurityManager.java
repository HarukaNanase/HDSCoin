package pt.ulisboa.tecnico.sec;

import com.sun.org.apache.xml.internal.security.utils.Base64;


import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SecurityManager {
    private static String algorithm = "RSA";

    public static boolean VerifyMessage(Request request, String sender){
        try {
            String signature = request.getdSig();
            byte[] signatureBytes = Base64.decode(signature);
            Signature sign = Signature.getInstance("SHA1WithRSA");

            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

            EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(sender));
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            byte[] data = request.requestAsJson().getBytes();
            
            sign.initVerify(publicKey);
            sign.update(data);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void SignMessage(Request request, PrivateKey privateKey){
        try {
            Signature d_sig = Signature.getInstance("SHA1WithRSA");
            d_sig.initSign(privateKey);
            d_sig.update(request.requestAsJson().getBytes());
            byte[] sigBytes = d_sig.sign();
            request.setdSig(Base64.encode(sigBytes, 1024));

            System.out.println("Signature: " + request.getdSig());

        }catch(NoSuchAlgorithmException noae){
            noae.printStackTrace();
        }catch(InvalidKeyException ike){
            ike.printStackTrace();
        }catch(SignatureException se){
            se.printStackTrace();
        }
    }



}
