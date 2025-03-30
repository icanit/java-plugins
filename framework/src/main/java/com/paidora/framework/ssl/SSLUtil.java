package com.paidora.framework.ssl;

import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class SSLUtil {

    public static SSLContext getSSLContextForCertificate(String key, String cert) throws NoSuchAlgorithmException, KeyManagementException, CertificateException, InvalidKeySpecException, KeyStoreException, IOException, UnrecoverableKeyException {
        String privateKeyContent = key
                .replaceAll("(-+BEGIN RSA PRIVATE KEY-+\\r?\\n?|-+END RSA PRIVATE KEY-+\\r?\\n?)", "")
                .replaceAll("(-+BEGIN PRIVATE KEY-+\\r?\\n?|-+END PRIVATE KEY-+\\r?\\n?)", "")
                .replace("\n", "");
        byte[] privKeyBytes = Base64.getDecoder().decode(privateKeyContent);

        var cf = CertificateFactory.getInstance("X.509");
        var caCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert.getBytes()));
        var certificateChain = new Certificate[]{caCert};

        var keyFactory = KeyFactory.getInstance("RSA");
        var ks = new PKCS8EncodedKeySpec(privKeyBytes);
        var privKey = (RSAPrivateKey) keyFactory.generatePrivate(ks);

        var keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null); // You don't need the KeyStore instance to come from a file.
        keyStore.setKeyEntry("key", privKey, null, certificateChain);

        var builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustAllStrategy());
        builder.loadKeyMaterial(keyStore, null);
        return builder.build();
    }

    public static SSLContext getSSLContextForBase64Certificate(String password, String base64cert) throws NoSuchAlgorithmException, KeyManagementException, CertificateException, InvalidKeySpecException, KeyStoreException, IOException, UnrecoverableKeyException, UnexpectedBehaviourException {
        var ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(Base64.getDecoder().decode(base64cert.replace("\n", ""))), password.toCharArray());
        var builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustAllStrategy());
        builder.loadKeyMaterial(ks, password.toCharArray());
        return builder.build();
    }
}
