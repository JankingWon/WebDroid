package sun.security.pkcs;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import sun.misc.HexDumpEncoder;
import sun.security.timestamp.TimestampToken;
import sun.security.util.ConstraintsParameters;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.DisabledAlgorithmConstraints;
import sun.security.util.KeyUtil;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.X500Name;

public class SignerInfo implements DerEncoder {
   private static final Set<CryptoPrimitive> DIGEST_PRIMITIVE_SET;
   private static final Set<CryptoPrimitive> SIG_PRIMITIVE_SET;
   private static final DisabledAlgorithmConstraints JAR_DISABLED_CHECK;
   BigInteger version;
   X500Name issuerName;
   BigInteger certificateSerialNumber;
   AlgorithmId digestAlgorithmId;
   AlgorithmId digestEncryptionAlgorithmId;
   byte[] encryptedDigest;
   Timestamp timestamp;
   private boolean hasTimestamp;
   private static final Debug debug;
   PKCS9Attributes authenticatedAttributes;
   PKCS9Attributes unauthenticatedAttributes;

   public SignerInfo(X500Name var1, BigInteger var2, AlgorithmId var3, AlgorithmId var4, byte[] var5) {
      this.hasTimestamp = true;
      this.version = BigInteger.ONE;
      this.issuerName = var1;
      this.certificateSerialNumber = var2;
      this.digestAlgorithmId = var3;
      this.digestEncryptionAlgorithmId = var4;
      this.encryptedDigest = var5;
   }

   public SignerInfo(X500Name var1, BigInteger var2, AlgorithmId var3, PKCS9Attributes var4, AlgorithmId var5, byte[] var6, PKCS9Attributes var7) {
      this.hasTimestamp = true;
      this.version = BigInteger.ONE;
      this.issuerName = var1;
      this.certificateSerialNumber = var2;
      this.digestAlgorithmId = var3;
      this.authenticatedAttributes = var4;
      this.digestEncryptionAlgorithmId = var5;
      this.encryptedDigest = var6;
      this.unauthenticatedAttributes = var7;
   }

   public SignerInfo(DerInputStream var1) throws IOException, ParsingException {
      this(var1, false);
   }

   public SignerInfo(DerInputStream var1, boolean var2) throws IOException, ParsingException {
      this.hasTimestamp = true;
      this.version = var1.getBigInteger();
      DerValue[] var3 = var1.getSequence(2);
      byte[] var4 = var3[0].toByteArray();
      this.issuerName = new X500Name(new DerValue((byte)48, var4));
      this.certificateSerialNumber = var3[1].getBigInteger();
      DerValue var5 = var1.getDerValue();
      this.digestAlgorithmId = AlgorithmId.parse(var5);
      if (var2) {
         var1.getSet(0);
      } else if ((byte)var1.peekByte() == -96) {
         this.authenticatedAttributes = new PKCS9Attributes(var1);
      }

      var5 = var1.getDerValue();
      this.digestEncryptionAlgorithmId = AlgorithmId.parse(var5);
      this.encryptedDigest = var1.getOctetString();
      if (var2) {
         var1.getSet(0);
      } else if (var1.available() != 0 && (byte)var1.peekByte() == -95) {
         this.unauthenticatedAttributes = new PKCS9Attributes(var1, true);
      }

      if (var1.available() != 0) {
         throw new ParsingException("extra data at the end");
      }
   }

   public void encode(DerOutputStream var1) throws IOException {
      this.derEncode(var1);
   }

   public void derEncode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      var2.putInteger(this.version);
      DerOutputStream var3 = new DerOutputStream();
      this.issuerName.encode(var3);
      var3.putInteger(this.certificateSerialNumber);
      var2.write((byte)48, (DerOutputStream)var3);
      this.digestAlgorithmId.encode(var2);
      if (this.authenticatedAttributes != null) {
         this.authenticatedAttributes.encode((byte)-96, var2);
      }

      this.digestEncryptionAlgorithmId.encode(var2);
      var2.putOctetString(this.encryptedDigest);
      if (this.unauthenticatedAttributes != null) {
         this.unauthenticatedAttributes.encode((byte)-95, var2);
      }

      DerOutputStream var4 = new DerOutputStream();
      var4.write((byte)48, (DerOutputStream)var2);
      var1.write(var4.toByteArray());
   }

   public X509Certificate getCertificate(PKCS7 var1) throws IOException {
      return var1.getCertificate(this.certificateSerialNumber, this.issuerName);
   }

   public ArrayList<X509Certificate> getCertificateChain(PKCS7 var1) throws IOException {
      X509Certificate var2 = var1.getCertificate(this.certificateSerialNumber, this.issuerName);
      if (var2 == null) {
         return null;
      } else {
         ArrayList var3 = new ArrayList();
         var3.add(var2);
         X509Certificate[] var4 = var1.getCertificates();
         if (var4 != null && !var2.getSubjectDN().equals(var2.getIssuerDN())) {
            Principal var5 = var2.getIssuerDN();
            int var6 = 0;

            boolean var7;
            do {
               var7 = false;

               for(int var8 = var6; var8 < var4.length; ++var8) {
                  if (var5.equals(var4[var8].getSubjectDN())) {
                     var3.add(var4[var8]);
                     if (var4[var8].getSubjectDN().equals(var4[var8].getIssuerDN())) {
                        var6 = var4.length;
                     } else {
                        var5 = var4[var8].getIssuerDN();
                        X509Certificate var9 = var4[var6];
                        var4[var6] = var4[var8];
                        var4[var8] = var9;
                        ++var6;
                     }

                     var7 = true;
                     break;
                  }
               }
            } while(var7);

            return var3;
         } else {
            return var3;
         }
      }
   }

   SignerInfo verify(PKCS7 var1, byte[] var2) throws NoSuchAlgorithmException, SignatureException {
      try {
         ContentInfo var3 = var1.getContentInfo();
         if (var2 == null) {
            var2 = var3.getContentBytes();
         }

         Timestamp var4 = null;

         try {
            var4 = this.getTimestamp();
         } catch (Exception var20) {
         }

         ConstraintsParameters var5 = new ConstraintsParameters(var4);
         String var6 = this.getDigestAlgorithmId().getName();
         byte[] var7;
         if (this.authenticatedAttributes == null) {
            var7 = var2;
         } else {
            ObjectIdentifier var8 = (ObjectIdentifier)this.authenticatedAttributes.getAttributeValue(PKCS9Attribute.CONTENT_TYPE_OID);
            if (var8 == null || !var8.equals((Object)var3.contentType)) {
               return null;
            }

            byte[] var9 = (byte[])((byte[])this.authenticatedAttributes.getAttributeValue(PKCS9Attribute.MESSAGE_DIGEST_OID));
            if (var9 == null) {
               return null;
            }

            try {
               JAR_DISABLED_CHECK.permits(var6, var5);
            } catch (CertPathValidatorException var19) {
               throw new SignatureException(var19.getMessage(), var19);
            }

            MessageDigest var10 = MessageDigest.getInstance(var6);
            byte[] var11 = var10.digest(var2);
            if (var9.length != var11.length) {
               return null;
            }

            for(int var12 = 0; var12 < var9.length; ++var12) {
               if (var9[var12] != var11[var12]) {
                  return null;
               }
            }

            var7 = this.authenticatedAttributes.getDerEncoding();
         }

         String var23 = this.getDigestEncryptionAlgorithmId().getName();
         String var24 = AlgorithmId.getEncAlgFromSigAlg(var23);
         if (var24 != null) {
            var23 = var24;
         }

         String var25 = AlgorithmId.makeSigAlg(var6, var23);

         try {
            JAR_DISABLED_CHECK.permits(var25, var5);
         } catch (CertPathValidatorException var18) {
            throw new SignatureException(var18.getMessage(), var18);
         }

         X509Certificate var26 = this.getCertificate(var1);
         if (var26 == null) {
            return null;
         } else {
            PublicKey var27 = var26.getPublicKey();
            if (!JAR_DISABLED_CHECK.permits((Set)SIG_PRIMITIVE_SET, (Key)var27)) {
               throw new SignatureException("Public key check failed. Disabled key used: " + KeyUtil.getKeySize((Key)var27) + " bit " + var27.getAlgorithm());
            } else if (var26.hasUnsupportedCriticalExtension()) {
               throw new SignatureException("Certificate has unsupported critical extension(s)");
            } else {
               boolean[] var13 = var26.getKeyUsage();
               if (var13 != null) {
                  KeyUsageExtension var14;
                  try {
                     var14 = new KeyUsageExtension(var13);
                  } catch (IOException var17) {
                     throw new SignatureException("Failed to parse keyUsage extension");
                  }

                  boolean var15 = var14.get("digital_signature");
                  boolean var16 = var14.get("non_repudiation");
                  if (!var15 && !var16) {
                     throw new SignatureException("Key usage restricted: cannot be used for digital signatures");
                  }
               }

               Signature var28 = Signature.getInstance(var25);
               var28.initVerify(var27);
               var28.update(var7);
               if (var28.verify(this.encryptedDigest)) {
                  return this;
               } else {
                  return null;
               }
            }
         }
      } catch (IOException var21) {
         throw new SignatureException("IO error verifying signature:\n" + var21.getMessage());
      } catch (InvalidKeyException var22) {
         throw new SignatureException("InvalidKey: " + var22.getMessage());
      }
   }

   SignerInfo verify(PKCS7 var1) throws NoSuchAlgorithmException, SignatureException {
      return this.verify(var1, (byte[])null);
   }

   public BigInteger getVersion() {
      return this.version;
   }

   public X500Name getIssuerName() {
      return this.issuerName;
   }

   public BigInteger getCertificateSerialNumber() {
      return this.certificateSerialNumber;
   }

   public AlgorithmId getDigestAlgorithmId() {
      return this.digestAlgorithmId;
   }

   public PKCS9Attributes getAuthenticatedAttributes() {
      return this.authenticatedAttributes;
   }

   public AlgorithmId getDigestEncryptionAlgorithmId() {
      return this.digestEncryptionAlgorithmId;
   }

   public byte[] getEncryptedDigest() {
      return this.encryptedDigest;
   }

   public PKCS9Attributes getUnauthenticatedAttributes() {
      return this.unauthenticatedAttributes;
   }

   public PKCS7 getTsToken() throws IOException {
      if (this.unauthenticatedAttributes == null) {
         return null;
      } else {
         PKCS9Attribute var1 = this.unauthenticatedAttributes.getAttribute(PKCS9Attribute.SIGNATURE_TIMESTAMP_TOKEN_OID);
         return var1 == null ? null : new PKCS7((byte[])((byte[])var1.getValue()));
      }
   }

   public Timestamp getTimestamp() throws IOException, NoSuchAlgorithmException, SignatureException, CertificateException {
      if (this.timestamp == null && this.hasTimestamp) {
         PKCS7 var1 = this.getTsToken();
         if (var1 == null) {
            this.hasTimestamp = false;
            return null;
         } else {
            byte[] var2 = var1.getContentInfo().getData();
            SignerInfo[] var3 = var1.verify(var2);
            ArrayList var4 = var3[0].getCertificateChain(var1);
            CertificateFactory var5 = CertificateFactory.getInstance("X.509");
            CertPath var6 = var5.generateCertPath((List)var4);
            TimestampToken var7 = new TimestampToken(var2);
            this.verifyTimestamp(var7);
            this.timestamp = new Timestamp(var7.getDate(), var6);
            return this.timestamp;
         }
      } else {
         return this.timestamp;
      }
   }

   private void verifyTimestamp(TimestampToken var1) throws NoSuchAlgorithmException, SignatureException {
      String var2 = var1.getHashAlgorithm().getName();
      if (!JAR_DISABLED_CHECK.permits(DIGEST_PRIMITIVE_SET, var2, (AlgorithmParameters)null)) {
         throw new SignatureException("Timestamp token digest check failed. Disabled algorithm used: " + var2);
      } else {
         MessageDigest var3 = MessageDigest.getInstance(var2);
         if (!Arrays.equals(var1.getHashedMessage(), var3.digest(this.encryptedDigest))) {
            throw new SignatureException("Signature timestamp (#" + var1.getSerialNumber() + ") generated on " + var1.getDate() + " is inapplicable");
         } else {
            if (debug != null) {
               debug.println();
               debug.println("Detected signature timestamp (#" + var1.getSerialNumber() + ") generated on " + var1.getDate());
               debug.println();
            }

         }
      }
   }

   public String toString() {
      HexDumpEncoder var1 = new HexDumpEncoder();
      String var2 = "";
      var2 = var2 + "Signer Info for (issuer): " + this.issuerName + "\n";
      var2 = var2 + "\tversion: " + Debug.toHexString(this.version) + "\n";
      var2 = var2 + "\tcertificateSerialNumber: " + Debug.toHexString(this.certificateSerialNumber) + "\n";
      var2 = var2 + "\tdigestAlgorithmId: " + this.digestAlgorithmId + "\n";
      if (this.authenticatedAttributes != null) {
         var2 = var2 + "\tauthenticatedAttributes: " + this.authenticatedAttributes + "\n";
      }

      var2 = var2 + "\tdigestEncryptionAlgorithmId: " + this.digestEncryptionAlgorithmId + "\n";
      var2 = var2 + "\tencryptedDigest: \n" + var1.encodeBuffer(this.encryptedDigest) + "\n";
      if (this.unauthenticatedAttributes != null) {
         var2 = var2 + "\tunauthenticatedAttributes: " + this.unauthenticatedAttributes + "\n";
      }

      return var2;
   }

   static {
      DIGEST_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.MESSAGE_DIGEST));
      SIG_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.SIGNATURE));
      JAR_DISABLED_CHECK = new DisabledAlgorithmConstraints("jdk.jar.disabledAlgorithms");
      debug = Debug.getInstance("jar");
   }
}
