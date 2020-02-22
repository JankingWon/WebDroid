package cn.janking.webDroid.util.Sign;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class Signature {
    private static Signature signature = new Signature();

    byte[] afterAlgorithmIdBytes = new byte[] { (byte)4, (byte)20 };

    byte[] algorithmIdBytes = new byte[] {
            (byte)48, (byte)9, (byte)6, (byte)5, (byte)43, (byte)14, (byte)3, (byte)2, (byte)26, (byte)5,
            (byte)0 };

    byte[] beforeAlgorithmIdBytes = new byte[] { (byte)48, (byte)33 };

    Cipher cipher;

    MessageDigest md;

    public static Signature getInstance() { return signature; }

    public void initSign(PrivateKey paramPrivateKey) throws InvalidKeyException, Exception {
        this.md = MessageDigest.getInstance("SHA1");
        this.cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        this.cipher.init(1, paramPrivateKey);
    }

    public byte[] sign() throws BadPaddingException, IllegalBlockSizeException {
        this.cipher.update(this.beforeAlgorithmIdBytes);
        this.cipher.update(this.algorithmIdBytes);
        this.cipher.update(this.afterAlgorithmIdBytes);
        this.cipher.update(this.md.digest());
        return this.cipher.doFinal();
    }

    public void update(byte[] paramArrayOfbyte) { this.md.update(paramArrayOfbyte); }

    public void update(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) { this.md.update(paramArrayOfbyte, paramInt1, paramInt2); }
}
