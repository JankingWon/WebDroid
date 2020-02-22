package cn.janking.webDroid.util.Sign;

import cn.janking.webDroid.util.Zio.ZioEntry;
import cn.janking.webDroid.util.Zio.ZipInput;
import cn.janking.webDroid.util.Zio.ZipOutput;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Signer {
    private static final String CERT_RSA_NAME = "META-INF/CERT.RSA";

    private static final String CERT_SF_NAME = "META-INF/CERT.SF";

    public static final String[] DEFAULT_KEYS;

    static PrivateKey privateKey;

    static X509Certificate publicKey;

    private static Signer res;

    static byte[] sigBlockTemp;

    private static Pattern stripPattern = Pattern.compile("^META-INF/(.*)[.](SF|RSA|DSA)$");

    static  {
        DEFAULT_KEYS = new String[] { "platform", "testkey" };
        res = new Signer();
    }

    public Signer() {}

    public Signer(String paramString) throws IOException, GeneralSecurityException { loadKeys(paramString); }

    private static Manifest addDigestsToManifest(Map<String, ZioEntry> paramMap) throws IOException, GeneralSecurityException {
        Manifest manifest1 = (Manifest)null;
        ZioEntry zioEntry = paramMap.get("META-INF/MANIFEST.MF");
        if (zioEntry != null) {
            manifest1 = new Manifest();
            manifest1.read(zioEntry.getInputStream());
        }
        Manifest manifest2 = new Manifest();
        Attributes attributes = manifest2.getMainAttributes();
        if (manifest1 != null) {
            attributes.putAll(manifest1.getMainAttributes());
        } else {
            attributes.putValue("Manifest-Version", "1.0");
            attributes.putValue("Created-By", "1.0 (Android SignApk)");
        }
        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        byte[] arrayOfByte = new byte[4096];
        TreeMap<Object, Object> treeMap = new TreeMap<Object, Object>();
        treeMap.putAll(paramMap);
        Iterator<ZioEntry> iterator = treeMap.values().iterator();
        label37: while (true) {
            if (!iterator.hasNext())
                return manifest2;
            ZioEntry zioEntry1 = iterator.next();
            String str = zioEntry1.getName();
            if (!zioEntry1.isDirectory() && !str.equals("META-INF/MANIFEST.MF") && !str.equals("META-INF/CERT.SF") && !str.equals("META-INF/CERT.RSA") && (stripPattern == null || !stripPattern.matcher(str).matches())) {
                InputStream inputStream = zioEntry1.getInputStream();
                while (true) {
                    int i = inputStream.read(arrayOfByte);
                    if (i <= 0) {
                        Attributes attributes2 = (Attributes)null;
                        Attributes attributes1 = attributes2;
                        if (manifest1 != null) {
                            Attributes attributes3 = manifest1.getAttributes(str);
                            attributes1 = attributes2;
                            if (attributes3 != null)
                                attributes1 = new Attributes(attributes3);
                        }
                        attributes2 = attributes1;
                        if (attributes1 == null)
                            attributes2 = new Attributes();
                        attributes2.putValue("SHA1-Digest", Base64.encode(messageDigest.digest()));
                        manifest2.getEntries().put(str, attributes2);
                        continue label37;
                    }
                    messageDigest.update(arrayOfByte, 0, i);
                }
                break;
            }
        }
    }

    private static void copyFiles(Manifest paramManifest, Map<String, ZioEntry> paramMap, ZipOutput paramZipOutput, long paramLong) throws IOException {
        ArrayList<Comparable> arrayList = new ArrayList(paramManifest.getEntries().keySet());
        Collections.sort(arrayList);
        Iterator<Comparable> iterator = arrayList.iterator();
        while (true) {
            if (!iterator.hasNext())
                return;
            ZioEntry zioEntry = paramMap.get(iterator.next());
            zioEntry.setTime(paramLong);
            paramZipOutput.write(zioEntry);
        }
    }

    private static KeySpec decryptPrivateKey(byte[] paramArrayOfbyte, String paramString) throws GeneralSecurityException {
        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = (EncryptedPrivateKeyInfo)null;
        try {
            EncryptedPrivateKeyInfo encryptedPrivateKeyInfo1 = new EncryptedPrivateKeyInfo(paramArrayOfbyte);
            char[] arrayOfChar = paramString.toCharArray();
            SecretKey secretKey = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo1.getAlgName()).generateSecret(new PBEKeySpec(arrayOfChar));
            Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo1.getAlgName());
            cipher.init(2, secretKey, encryptedPrivateKeyInfo1.getAlgParameters());
            try {
                return encryptedPrivateKeyInfo1.getKeySpec(cipher);
            } catch (InvalidKeySpecException invalidKeySpecException) {
                System.err.println("PrivateKey may be bad.");
                throw invalidKeySpecException;
            }
        } catch (IOException iOException) {
            return (KeySpec)null;
        }
    }

    private static void generateSignatureFile(Manifest paramManifest, OutputStream paramOutputStream) throws IOException, GeneralSecurityException {
        paramOutputStream.write("Signature-Version: 1.0\r\n".getBytes());
        paramOutputStream.write("Created-By: 1.0 (Android SignApk)\r\n".getBytes());
        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        PrintStream printStream = new PrintStream(new DigestOutputStream(new ByteArrayOutputStream(), messageDigest), true, "UTF-8");
        paramManifest.write(printStream);
        printStream.flush();
        paramOutputStream.write(("SHA1-Digest-Manifest: " + Base64Util.encode(messageDigest.digest()) + "\r\n\r\n").getBytes());
        Iterator<Map.Entry> iterator = paramManifest.getEntries().entrySet().iterator();
        label12: while (true) {
            if (!iterator.hasNext())
                return;
            Map.Entry entry = iterator.next();
            String str = "Name: " + (String)entry.getKey() + "\r\n";
            printStream.print(str);
            Iterator<Map.Entry<Object, Object>> iterator1 = ((Attributes)entry.getValue()).entrySet().iterator();
            while (true) {
                if (!iterator1.hasNext()) {
                    printStream.print("\r\n");
                    printStream.flush();
                    paramOutputStream.write(str.getBytes());
                    paramOutputStream.write(("SHA1-Digest: " + Base64.encode(messageDigest.digest()) + "\r\n\r\n").getBytes());
                    continue label12;
                }
                Map.Entry entry1 = iterator1.next();
                printStream.print((new StringBuffer()).append(entry1.getKey()).append(": ").toString() + entry1.getValue() + "\r\n");
            }
            break;
        }
    }

    private static byte[] readBytes(InputStream paramInputStream) throws IOException {
        byte[] arrayOfByte = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int i = paramInputStream.read(arrayOfByte, 0, arrayOfByte.length);
            if (i == -1)
                return byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.write(arrayOfByte, 0, i);
        }
    }

    private static PrivateKey readPrivateKey(InputStream paramInputStream) throws IOException, GeneralSecurityException {
        try {
            byte[] arrayOfByte = readBytes(paramInputStream);
            KeySpec keySpec = decryptPrivateKey(arrayOfByte, "");
            if (keySpec == null){
                keySpec = new PKCS8EncodedKeySpec(arrayOfByte);
                PKCS8EncodedKeySpec(keySpec, arrayOfByte);
            }

            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } finally {
            paramInputStream.close();
        }
        return null;
    }

    private static X509Certificate readPublicKey(InputStream paramInputStream) throws IOException, GeneralSecurityException {
        try {
            return (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(paramInputStream);
        } finally {
            paramInputStream.close();
        }
    }

    public static void sign(String paramString1, String paramString2) throws IOException, GeneralSecurityException { // Byte code:
        //   0: getstatic apksigner/Signer.privateKey : Ljava/security/PrivateKey;
        //   3: ifnonnull -> 14
        //   6: getstatic apksigner/Signer.res : Lapksigner/Signer;
        //   9: ldc 'testkey'
        //   11: invokevirtual loadKeys : (Ljava/lang/String;)V
        //   14: aconst_null
        //   15: checkcast apksigner/io/ZipInput
        //   18: astore #5
        //   20: aconst_null
        //   21: checkcast apksigner/io/ZipOutput
        //   24: astore #4
        //   26: aload_0
        //   27: invokestatic read : (Ljava/lang/String;)Lapksigner/io/ZipInput;
        //   30: astore_0
        //   31: aload_0
        //   32: invokevirtual getEntries : ()Ljava/util/Map;
        //   35: astore #6
        //   37: new apksigner/io/ZipOutput
        //   40: dup
        //   41: new java/io/FileOutputStream
        //   44: dup
        //   45: aload_1
        //   46: invokespecial <init> : (Ljava/lang/String;)V
        //   49: invokespecial <init> : (Ljava/io/OutputStream;)V
        //   52: astore #5
        //   54: getstatic apksigner/Signer.publicKey : Ljava/security/cert/X509Certificate;
        //   57: invokevirtual getNotBefore : ()Ljava/util/Date;
        //   60: invokevirtual getTime : ()J
        //   63: ldc2_w 3600000
        //   66: ladd
        //   67: lstore_2
        //   68: aload #6
        //   70: invokestatic addDigestsToManifest : (Ljava/util/Map;)Ljava/util/jar/Manifest;
        //   73: astore_1
        //   74: new apksigner/io/ZioEntry
        //   77: dup
        //   78: ldc 'META-INF/MANIFEST.MF'
        //   80: invokespecial <init> : (Ljava/lang/String;)V
        //   83: astore #4
        //   85: aload #4
        //   87: lload_2
        //   88: invokevirtual setTime : (J)V
        //   91: aload_1
        //   92: aload #4
        //   94: invokevirtual getOutputStream : ()Ljava/io/OutputStream;
        //   97: invokevirtual write : (Ljava/io/OutputStream;)V
        //   100: aload #5
        //   102: aload #4
        //   104: invokevirtual write : (Lapksigner/io/ZioEntry;)V
        //   107: new apksigner/io/ZioEntry
        //   110: dup
        //   111: ldc 'META-INF/CERT.SF'
        //   113: invokespecial <init> : (Ljava/lang/String;)V
        //   116: astore #7
        //   118: aload #7
        //   120: lload_2
        //   121: invokevirtual setTime : (J)V
        //   124: invokestatic getInstance : ()Lapksigner/Signature;
        //   127: astore #4
        //   129: aload #4
        //   131: getstatic apksigner/Signer.privateKey : Ljava/security/PrivateKey;
        //   134: invokevirtual initSign : (Ljava/security/PrivateKey;)V
        //   137: new java/io/ByteArrayOutputStream
        //   140: dup
        //   141: invokespecial <init> : ()V
        //   144: astore #8
        //   146: aload_1
        //   147: aload #8
        //   149: invokestatic generateSignatureFile : (Ljava/util/jar/Manifest;Ljava/io/OutputStream;)V
        //   152: aload #8
        //   154: invokevirtual toByteArray : ()[B
        //   157: astore #8
        //   159: aload #7
        //   161: invokevirtual getOutputStream : ()Ljava/io/OutputStream;
        //   164: aload #8
        //   166: invokevirtual write : ([B)V
        //   169: aload #5
        //   171: aload #7
        //   173: invokevirtual write : (Lapksigner/io/ZioEntry;)V
        //   176: aload #4
        //   178: aload #8
        //   180: invokevirtual update : ([B)V
        //   183: new apksigner/io/ZioEntry
        //   186: dup
        //   187: ldc 'META-INF/CERT.RSA'
        //   189: invokespecial <init> : (Ljava/lang/String;)V
        //   192: astore #7
        //   194: aload #7
        //   196: lload_2
        //   197: invokevirtual setTime : (J)V
        //   200: aload #4
        //   202: getstatic apksigner/Signer.publicKey : Ljava/security/cert/X509Certificate;
        //   205: aload #7
        //   207: invokevirtual getOutputStream : ()Ljava/io/OutputStream;
        //   210: invokestatic writeSignatureBlock : (Lapksigner/Signature;Ljava/security/cert/X509Certificate;Ljava/io/OutputStream;)V
        //   213: aload #5
        //   215: aload #7
        //   217: invokevirtual write : (Lapksigner/io/ZioEntry;)V
        //   220: aload_1
        //   221: aload #6
        //   223: aload #5
        //   225: lload_2
        //   226: invokestatic copyFiles : (Ljava/util/jar/Manifest;Ljava/util/Map;Lapksigner/io/ZipOutput;J)V
        //   229: aload_0
        //   230: ifnull -> 237
        //   233: aload_0
        //   234: invokevirtual close : ()V
        //   237: aload #5
        //   239: ifnull -> 247
        //   242: aload #5
        //   244: invokevirtual close : ()V
        //   247: return
        //   248: astore_1
        //   249: aload #5
        //   251: astore_0
        //   252: aload_1
        //   253: astore #5
        //   255: aload #4
        //   257: astore_1
        //   258: aload #5
        //   260: invokevirtual printStackTrace : ()V
        //   263: aload_1
        //   264: astore #5
        //   266: goto -> 229
        //   269: astore_1
        //   270: aload #5
        //   272: astore_0
        //   273: aload_0
        //   274: ifnull -> 281
        //   277: aload_0
        //   278: invokevirtual close : ()V
        //   281: aload #4
        //   283: ifnull -> 291
        //   286: aload #4
        //   288: invokevirtual close : ()V
        //   291: aload_1
        //   292: athrow
        //   293: astore_0
        //   294: return
        //   295: astore_0
        //   296: goto -> 291
        //   299: astore_1
        //   300: goto -> 273
        //   303: astore_1
        //   304: aload #5
        //   306: astore #4
        //   308: goto -> 273
        //   311: astore #5
        //   313: aload_1
        //   314: astore #4
        //   316: aload #5
        //   318: astore_1
        //   319: goto -> 273
        //   322: astore #5
        //   324: aload #4
        //   326: astore_1
        //   327: goto -> 258
        //   330: astore #4
        //   332: aload #5
        //   334: astore_1
        //   335: aload #4
        //   337: astore #5
        //   339: goto -> 258
        // Exception table:
        //   from	to	target	type
        //   26	31	248	java/lang/Exception
        //   26	31	269	finally
        //   31	54	322	java/lang/Exception
        //   31	54	299	finally
        //   54	229	330	java/lang/Exception
        //   54	229	303	finally
        //   233	237	293	java/io/IOException
        //   242	247	293	java/io/IOException
        //   258	263	311	finally
        //   277	281	295	java/io/IOException
        //   286	291	295	java/io/IOException
        }

        public static void sign(String paramString1, String paramString2, String paramString3) throws IOException, GeneralSecurityException { // Byte code:
            //   0: getstatic apksigner/Signer.res : Lapksigner/Signer;
            //   3: aload_2
            //   4: invokevirtual loadKeys : (Ljava/lang/String;)V
            //   7: aconst_null
            //   8: checkcast apksigner/io/ZipInput
            //   11: astore #5
            //   13: aconst_null
            //   14: checkcast apksigner/io/ZipOutput
            //   17: astore_2
            //   18: aload_0
            //   19: invokestatic read : (Ljava/lang/String;)Lapksigner/io/ZipInput;
            //   22: astore_0
            //   23: aload_0
            //   24: invokevirtual getEntries : ()Ljava/util/Map;
            //   27: astore #6
            //   29: new apksigner/io/ZipOutput
            //   32: dup
            //   33: new java/io/FileOutputStream
            //   36: dup
            //   37: aload_1
            //   38: invokespecial <init> : (Ljava/lang/String;)V
            //   41: invokespecial <init> : (Ljava/io/OutputStream;)V
            //   44: astore #5
            //   46: getstatic apksigner/Signer.publicKey : Ljava/security/cert/X509Certificate;
            //   49: invokevirtual getNotBefore : ()Ljava/util/Date;
            //   52: invokevirtual getTime : ()J
            //   55: ldc2_w 3600000
            //   58: ladd
            //   59: lstore_3
            //   60: aload #6
            //   62: invokestatic addDigestsToManifest : (Ljava/util/Map;)Ljava/util/jar/Manifest;
            //   65: astore_1
            //   66: new apksigner/io/ZioEntry
            //   69: dup
            //   70: ldc 'META-INF/MANIFEST.MF'
            //   72: invokespecial <init> : (Ljava/lang/String;)V
            //   75: astore_2
            //   76: aload_2
            //   77: lload_3
            //   78: invokevirtual setTime : (J)V
            //   81: aload_1
            //   82: aload_2
            //   83: invokevirtual getOutputStream : ()Ljava/io/OutputStream;
            //   86: invokevirtual write : (Ljava/io/OutputStream;)V
            //   89: aload #5
            //   91: aload_2
            //   92: invokevirtual write : (Lapksigner/io/ZioEntry;)V
            //   95: new apksigner/io/ZioEntry
            //   98: dup
            //   99: ldc 'META-INF/CERT.SF'
            //   101: invokespecial <init> : (Ljava/lang/String;)V
            //   104: astore #7
            //   106: aload #7
            //   108: lload_3
            //   109: invokevirtual setTime : (J)V
            //   112: invokestatic getInstance : ()Lapksigner/Signature;
            //   115: astore_2
            //   116: aload_2
            //   117: getstatic apksigner/Signer.privateKey : Ljava/security/PrivateKey;
            //   120: invokevirtual initSign : (Ljava/security/PrivateKey;)V
            //   123: new java/io/ByteArrayOutputStream
            //   126: dup
            //   127: invokespecial <init> : ()V
            //   130: astore #8
            //   132: aload_1
            //   133: aload #8
            //   135: invokestatic generateSignatureFile : (Ljava/util/jar/Manifest;Ljava/io/OutputStream;)V
            //   138: aload #8
            //   140: invokevirtual toByteArray : ()[B
            //   143: astore #8
            //   145: aload #7
            //   147: invokevirtual getOutputStream : ()Ljava/io/OutputStream;
            //   150: aload #8
            //   152: invokevirtual write : ([B)V
            //   155: aload #5
            //   157: aload #7
            //   159: invokevirtual write : (Lapksigner/io/ZioEntry;)V
            //   162: aload_2
            //   163: aload #8
            //   165: invokevirtual update : ([B)V
            //   168: new apksigner/io/ZioEntry
            //   171: dup
            //   172: ldc 'META-INF/CERT.RSA'
            //   174: invokespecial <init> : (Ljava/lang/String;)V
            //   177: astore #7
            //   179: aload #7
            //   181: lload_3
            //   182: invokevirtual setTime : (J)V
            //   185: aload_2
            //   186: getstatic apksigner/Signer.publicKey : Ljava/security/cert/X509Certificate;
            //   189: aload #7
            //   191: invokevirtual getOutputStream : ()Ljava/io/OutputStream;
            //   194: invokestatic writeSignatureBlock : (Lapksigner/Signature;Ljava/security/cert/X509Certificate;Ljava/io/OutputStream;)V
            //   197: aload #5
            //   199: aload #7
            //   201: invokevirtual write : (Lapksigner/io/ZioEntry;)V
            //   204: aload_1
            //   205: aload #6
            //   207: aload #5
            //   209: lload_3
            //   210: invokestatic copyFiles : (Ljava/util/jar/Manifest;Ljava/util/Map;Lapksigner/io/ZipOutput;J)V
            //   213: aload_0
            //   214: ifnull -> 221
            //   217: aload_0
            //   218: invokevirtual close : ()V
            //   221: aload #5
            //   223: ifnull -> 231
            //   226: aload #5
            //   228: invokevirtual close : ()V
            //   231: return
            //   232: astore_1
            //   233: aload #5
            //   235: astore_0
            //   236: aload_1
            //   237: astore #5
            //   239: aload_2
            //   240: astore_1
            //   241: aload #5
            //   243: invokevirtual printStackTrace : ()V
            //   246: aload_1
            //   247: astore #5
            //   249: goto -> 213
            //   252: astore_1
            //   253: aload #5
            //   255: astore_0
            //   256: aload_0
            //   257: ifnull -> 264
            //   260: aload_0
            //   261: invokevirtual close : ()V
            //   264: aload_2
            //   265: ifnull -> 272
            //   268: aload_2
            //   269: invokevirtual close : ()V
            //   272: aload_1
            //   273: athrow
            //   274: astore_0
            //   275: return
            //   276: astore_0
            //   277: goto -> 272
            //   280: astore_1
            //   281: goto -> 256
            //   284: astore_1
            //   285: aload #5
            //   287: astore_2
            //   288: goto -> 256
            //   291: astore #5
            //   293: aload_1
            //   294: astore_2
            //   295: aload #5
            //   297: astore_1
            //   298: goto -> 256
            //   301: astore #5
            //   303: aload_2
            //   304: astore_1
            //   305: goto -> 241
            //   308: astore_2
            //   309: aload #5
            //   311: astore_1
            //   312: aload_2
            //   313: astore #5
            //   315: goto -> 241
            // Exception table:
            //   from	to	target	type
            //   18	23	232	java/lang/Exception
            //   18	23	252	finally
            //   23	46	301	java/lang/Exception
            //   23	46	280	finally
            //   46	213	308	java/lang/Exception
            //   46	213	284	finally
            //   217	221	274	java/io/IOException
            //   226	231	274	java/io/IOException
            //   241	246	291	finally
            //   260	264	276	java/io/IOException
            //   268	272	276	java/io/IOException
            }

            public static void sign(String paramString1, String paramString2, String paramString3, String paramString4) throws Exception {
                res.loadKeys("testkey");
                ZipInput zipInput2 = (ZipInput)null;
                ZipOutput zipOutput2 = (ZipOutput)null;
                ZipInput zipInput1 = ZipInput.read(paramString1);
                Map<String, ZioEntry> map = zipInput1.getEntries();
                ZipOutput zipOutput1 = new ZipOutput(new FileOutputStream(paramString2));
                long l = publicKey.getNotBefore().getTime() + 3600000L;
                Manifest manifest = addDigestsToManifest(map);
                ZioEntry zioEntry1 = new ZioEntry("META-INF/MANIFEST.MF");
                zioEntry1.setTime(l);
                manifest.write(zioEntry1.getOutputStream());
                zipOutput1.write(zioEntry1);
                ZioEntry zioEntry2 = new ZioEntry("META-INF/CERT.SF");
                zioEntry2.setTime(l);
                Signature signature = Signature.getInstance();
                signature.initSign(privateKey);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                generateSignatureFile(manifest, byteArrayOutputStream);
                byte[] arrayOfByte = byteArrayOutputStream.toByteArray();
                zioEntry2.getOutputStream().write(arrayOfByte);
                zipOutput1.write(zioEntry2);
                signature.update(arrayOfByte);
                zioEntry2 = new ZioEntry("META-INF/CERT.RSA");
                zioEntry2.setTime(l);
                writeSignatureBlock(signature, publicKey, zioEntry2.getOutputStream());
                zipOutput1.write(zioEntry2);
                copyFiles(manifest, map, zipOutput1, l);
                zipInput1.close();
                zipOutput1.close();
            }

            private static void writeSignatureBlock(Signature paramSignature, X509Certificate paramX509Certificate, OutputStream paramOutputStream) throws IOException, GeneralSecurityException, Exception {
                paramOutputStream.write(sigBlockTemp);
                paramOutputStream.write(paramSignature.sign());
            }

            public void loadKeys(String paramString) throws IOException, GeneralSecurityException {
                privateKey = readPrivateKey(getClass().getResource("/assets/keys/" + paramString + ".pk8").openStream());
                publicKey = readPublicKey(getClass().getResource("/assets/keys/" + paramString + ".x509.pem").openStream());
                sigBlockTemp = readBytes(getClass().getResource("/assets/keys/" + paramString + ".sbt").openStream());
            }

            public void loadKeys(String paramString1, String paramString2) throws IOException, GeneralSecurityException {
                privateKey = readPrivateKey(new FileInputStream(paramString1 + paramString2 + ".pk8"));
                publicKey = readPublicKey(new FileInputStream(paramString1 + paramString2 + ".x509.pem"));
                sigBlockTemp = readBytes(new FileInputStream(paramString1 + paramString2 + ".sbt"));
            }
        }
