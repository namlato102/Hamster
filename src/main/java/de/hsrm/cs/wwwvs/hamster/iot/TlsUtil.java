package de.hsrm.cs.wwwvs.hamster.iot;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class TlsUtil {

    public static SSLSocketFactory getSocketFactory(final String caCrtFile,
                                                    final String crtFile, final String keyFile, final String password)
            throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        X509Certificate caCert = null;

        FileInputStream fis = new FileInputStream(caCrtFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        while (bis.available() > 0) {
            caCert = (X509Certificate) cf.generateCertificate(bis);
        }

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(caKs);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());

        // create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1.2");

        if (crtFile  != null) {
            // load client private key
            PEMParser pemParser = new PEMParser(new FileReader(keyFile));
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            PrivateKey key;
            if (object instanceof PEMKeyPair) {
                PEMKeyPair pemKeyPair = (PEMKeyPair) object;
                key = converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
            } else if (object instanceof PrivateKeyInfo) {
                PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) object;
                key = converter.getPrivateKey(privateKeyInfo);
            } else {
                System.out.println(object.toString());
                throw new IllegalArgumentException("Unsupported key format");
            }
            pemParser.close();

            // load client certificate
            bis = new BufferedInputStream(new FileInputStream(crtFile));
            X509Certificate cert = null;
            while (bis.available() > 0) {
                cert = (X509Certificate) cf.generateCertificate(bis);
            }

            // client key and certificates are sent to server so it can authenticate
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("certificate", cert);
            ks.setKeyEntry("private-key", key, password == null ? null : password.toCharArray(),
                    new java.security.cert.Certificate[]{cert});
            kmf.init(ks, password == null ? null : password.toCharArray());

            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        }
        else {
            context.init(null, tmf.getTrustManagers(), null);
        }
        return context.getSocketFactory();
    }
}
