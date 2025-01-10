package tn.supcom.appsec.security;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class TestJwtManager {

    private final String tenantId = "watermarking123";
    private final String subject = "john.doe";
    private final String scope = "resource:read,resource:write";
    private final String[] roles = new String[]{"Moderator","Client"};

    @Test
    public void testGenerateTokenAndVerifyToken(){
        assertDoesNotThrow(()->{
            JwtManager jwtManager = new JwtManager();
            var ret = jwtManager.verifyToken(jwtManager.generateToken(tenantId,subject,scope,roles));
            assertFalse(ret.isEmpty());
            assertEquals(tenantId, ret.get("tenant-id"));
            assertEquals(subject, ret.get("sub"));
            assertEquals(scope, ret.get("scope"));
            var rolesString = ret.get("groups");
            rolesString = rolesString.replace("[", "").replace("]", "");
            rolesString = rolesString.replace("\"", "");
            assertArrayEquals(roles, rolesString.split(","));
        });
    }

    @Test
    public void testKeyPairGeneration(){
        assertDoesNotThrow(()->{
            var publicKeyEncodedPrefix = "MCowBQYDK2VwAyEA";
            assertEquals(16, publicKeyEncodedPrefix.length());
            var curve = "Ed25519";
            var keyPairGenerator = KeyPairGenerator.getInstance(curve);
            for(var i=0;i<10;++i){
                var  keyPair = keyPairGenerator.generateKeyPair();
                var publicKeyEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(keyPair.getPublic().getEncoded());
                assertTrue(publicKeyEncoded.startsWith(publicKeyEncodedPrefix));
            }
        });
    }

    private final String encodedPublicKeyFromJWKX = "iUYJwW70bb_9JzLb-qFqUmszMICi3Wx9HevYTmFxaBA"; //from IAM JWK Endpoint
    private final String encodedToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFZERTQSIsImtpZCI6IjkyYmM3OWUxLTFmMzctNDA3Zi1iYjg2LTQ5MGY4ZTczOTdmMSJ9.eyJpc3MiOiJ1cm46bWUuYXBwc2VjLmlhbSIsImF1ZCI6WyJ1cm46bWUuYXBwc2VjLmFkbWluIiwidXJuOm1lLmFwcHNlYy53d3ciXSwidGVuYW50LWlkIjoid2F0ZXJtYXJraW5nMTIzIiwic3ViIjoiam9obi5kb2UiLCJ1cG4iOiJqb2huLmRvZSIsInNjb3BlIjoicmVzb3VyY2U6cmVhZCxyZXNvdXJjZTp3cml0ZSIsImdyb3VwcyI6WyJNb2RlcmF0b3IiLCJDbGllbnQiXSwiZXhwIjoxNzMyNjEyMDYxLCJpYXQiOjE3MzI2MTEwNDEsIm5iZiI6MTczMjYxMTA0MSwianRpIjoiMzIxNWVjYTgtYjZlZS00OGJiLTkwOWUtODQ4MTFiMTBiNDY4In0.PZe7qYcLGqULT_6v_1hA5toJpU7P_9wOjUxPpvJrGdrIub6r6NSUw6cA7L8rv8d4Ua7mKNlrca0JTCkIwC5ACA"; //from PWA/iOS app
    private final static String curve = "Ed25519";
    @Test
    public void testSignatureFromAPI(){
        assertDoesNotThrow(()->{
            var decodedPublicKey = Base64.getUrlDecoder().decode(encodedPublicKeyFromJWKX);

            var isOdd = (decodedPublicKey[decodedPublicKey.length - 1] & 255) >> 7 == 1;
            decodedPublicKey[decodedPublicKey.length - 1] &= 127;
            var i = 0;
            var j = decodedPublicKey.length -1;
            while (i<j){
                var tmp = decodedPublicKey[i];
                decodedPublicKey[i] = decodedPublicKey[j];
                decodedPublicKey[j] = tmp;
                ++i;--j;
            }
            var y = new BigInteger(1, decodedPublicKey);
            var ep = new EdECPoint(isOdd,y);
            var paramSpec = new NamedParameterSpec(curve);
            var publicKeySpec = new EdECPublicKeySpec(paramSpec,ep);
            var kf = KeyFactory.getInstance(curve);
            var publicKey = kf.generatePublic(publicKeySpec);
            var signatureAlgorithm = Signature.getInstance(curve);
            signatureAlgorithm.initVerify(publicKey);
            var parts = encodedToken.split("\\.");
            signatureAlgorithm.update((parts[0]+"."+parts[1]).getBytes());
            assertTrue(signatureAlgorithm.verify(Base64.getUrlDecoder().decode(parts[2].getBytes())));
        });
    }
}
