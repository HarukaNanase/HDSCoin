package pt.ulisboa.tecnico.sec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

public class SecurityManagerTest {
    private String ACCOUNT_ADDRESS = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJS8erQTo+xl5IfVl0ckURA1eL7IKeRzBNWIMW/4NxzBjnPZF+Sdt0KisKeed4kcrEYGLntniL2LDt2r2NyQ8pECAwEAAQ==";
    private String ACCOUNT_PRIVATE_KEY_STRING = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAlLx6tBOj7GXkh9WXRyRREDV4vsgp5HME1Ygxb/g3HMGOc9kX5J23QqKwp553iRysRgYue2eIvYsO3avY3JDykQIDAQABAkAW8KsMMytJPr3spWjbtCI8mcKxyjWL4qGQPZ1CY8o8SO/Mk9bXL1sExA3B2trUBwCTu8kU4e9+CECq7QkO/tgBAiEA/yw/0bgFigFz9K/GL8VfLQEyX9Oj57UAdATay6EqcrECIQCVN+fNCXyqxQFJCfaNsalSbB/3+KGUrMPGOgbyYpu14QIhAPeRltmCEN20Syw63a27cHvZjWYrj/peQfJOQ3kNBIIBAiBw3I0NzwwlGcbEGK4MNAEunyt64epMynN1HfSdJipB4QIhANLxmv1Rr1xHzvq0GZE30mKNAuVslyMAvsg6qjOyKQZj";
    private PublicKey ACCOUNT_PUBLIC_KEY;
    private PrivateKey ACCOUNT_PRIVATE_KEY;
    private Ledger ledger;

    @Before
    public void setUp() throws Exception{
        byte[] publicBytes = Base64.decode(ACCOUNT_ADDRESS);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.ACCOUNT_PUBLIC_KEY = keyFactory.generatePublic(keySpec);

        byte[] privatebytes = Base64.decode(ACCOUNT_PRIVATE_KEY_STRING);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privatebytes);
        this.ACCOUNT_PRIVATE_KEY = keyFactory.generatePrivate(spec);
        ledger = Ledger.getInstance();
    }

    @After
    public void tearDown(){
        ledger = null;
    }

    @Test
    public void RequestIntegrityTest(){
        Request req = new Request(Opcode.CREATE_ACCOUNT);
        req.addParameter(ACCOUNT_ADDRESS);
        SecurityManager.SignMessage(req, ACCOUNT_PRIVATE_KEY);
        System.out.println(req.requestAsJson());
        assertEquals(true, SecurityManager.VerifyMessage(req, ACCOUNT_ADDRESS));
        req.setOpcode(Opcode.RECEIVE_TRANSACTION);
        //req.setParameter(0, "ASDASDASD");
        System.out.println(req.requestAsJson());
        assertEquals(false, SecurityManager.VerifyMessage(req, ACCOUNT_ADDRESS));
    }

    @Test
    public void RequestIntegrityContentTest(){
        Request req = new Request(Opcode.CREATE_ACCOUNT);
        req.addParameter(ACCOUNT_ADDRESS);
        SecurityManager.SignMessage(req, ACCOUNT_PRIVATE_KEY);
        System.out.println(req.requestAsJson());
        assertEquals(true, SecurityManager.VerifyMessage(req, ACCOUNT_ADDRESS));
        req.setParameter(0, "ASDASDASD");
        System.out.println(req.requestAsJson());
        assertEquals(false, SecurityManager.VerifyMessage(req, ACCOUNT_ADDRESS));
    }

    @Test
    public void ReplayAttackTest(){
        Account account = new Account(ACCOUNT_PUBLIC_KEY, ACCOUNT_ADDRESS);
        Request req = new Request(Opcode.CREATE_ACCOUNT);
        req.addParameter(ACCOUNT_ADDRESS);
        SecurityManager.SignMessage(req, ACCOUNT_PRIVATE_KEY);
        Request atkReq = new Request(Opcode.CHECK_ACCOUNT);
        atkReq.addParameter(ACCOUNT_ADDRESS);
        atkReq.setSequenceNumber((account.getSequenceNumber() + 1));
        SecurityManager.SignMessage(atkReq, ACCOUNT_PRIVATE_KEY);
        if(SecurityManager.VerifyMessage(atkReq, ACCOUNT_ADDRESS)){
            assertEquals(true, SecurityManager.VerifySequenceNumber(atkReq, account));
            account.setSequenceNumber(account.getSequenceNumber()+1);
            assertEquals(false, SecurityManager.VerifySequenceNumber(atkReq, account));
        }else{
            fail();
        }

    }

    @Test
    public void SuccessTwoMessagesTest(){
        Account account = new Account(ACCOUNT_PUBLIC_KEY, ACCOUNT_ADDRESS);
        Request req = new Request(Opcode.CHECK_ACCOUNT);
        req.setSequenceNumber((account.getSequenceNumber() + 1));
        req.addParameter(ACCOUNT_ADDRESS);
        SecurityManager.SignMessage(req, ACCOUNT_PRIVATE_KEY);
        if(SecurityManager.VerifyMessage(req, ACCOUNT_ADDRESS)){
            assertEquals(true, SecurityManager.VerifySequenceNumber(req, account));
            account.setSequenceNumber(account.getSequenceNumber()+1);
            Request req2 = new Request(Opcode.CHECK_ACCOUNT);
            req2.addParameter(ACCOUNT_ADDRESS);
            req2.setSequenceNumber((account.getSequenceNumber() + 1));
            req2.addParameter(ACCOUNT_ADDRESS);
            SecurityManager.SignMessage(req2, ACCOUNT_PRIVATE_KEY);
            if(SecurityManager.VerifyMessage(req2, ACCOUNT_ADDRESS)){
                assertEquals(true, SecurityManager.VerifySequenceNumber(req2, account));
            }else{
                fail();
            }
        }else
            fail();
    }

    @Test
    public void ExpiresOnTest(){
        Request req = new Request(Opcode.CHECK_ACCOUNT);
        req.addParameter(ACCOUNT_ADDRESS);
        SecurityManager.SignMessage(req, ACCOUNT_PRIVATE_KEY);
        try{
            Thread.sleep(SecurityManager.getMaxMessageDelay() + 1000);
            assertEquals(false, SecurityManager.VerifyMessage(req, ACCOUNT_ADDRESS));
        }catch(InterruptedException ie){
            //
        }
    }



}
