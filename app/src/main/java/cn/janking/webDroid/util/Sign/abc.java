package cn.janking.webDroid.util.Sign;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class abc {
    public abc() throws NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException {
        FileInputStream fileInputStream = new FileInputStream(".keystore");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(fileInputStream, "123456".toCharArray());
        keyStore.getCertificate((String)null);
    }
}
