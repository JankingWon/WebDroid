package sun.security.pkcs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class PKCS7 {
   private ObjectIdentifier contentType;
   private BigInteger version;
   private AlgorithmId[] digestAlgorithmIds;
   private ContentInfo contentInfo;
   private X509Certificate[] certificates;
   private X509CRL[] crls;
   private SignerInfo[] signerInfos;
   private boolean oldStyle;
   private Principal[] certIssuerNames;
   private static final String KP_TIMESTAMPING_OID = "1.3.6.1.5.5.7.3.8";
   private static final String EXTENDED_KEY_USAGE_OID = "2.5.29.37";

   public PKCS7(InputStream var1) throws ParsingException, IOException {
      this.version = null;
      this.digestAlgorithmIds = null;
      this.contentInfo = null;
      this.certificates = null;
      this.crls = null;
      this.signerInfos = null;
      this.oldStyle = false;
      DataInputStream var2 = new DataInputStream(var1);
      byte[] var3 = new byte[var2.available()];
      var2.readFully(var3);
      this.parse(new DerInputStream(var3));
   }

   public PKCS7(DerInputStream var1) throws ParsingException {
      this.version = null;
      this.digestAlgorithmIds = null;
      this.contentInfo = null;
      this.certificates = null;
      this.crls = null;
      this.signerInfos = null;
      this.oldStyle = false;
      this.parse(var1);
   }

   public PKCS7(byte[] var1) throws ParsingException {
      this.version = null;
      this.digestAlgorithmIds = null;
      this.contentInfo = null;
      this.certificates = null;
      this.crls = null;
      this.signerInfos = null;
      this.oldStyle = false;

      try {
         DerInputStream var2 = new DerInputStream(var1);
         this.parse(var2);
      } catch (IOException var4) {
         ParsingException var3 = new ParsingException("Unable to parse the encoded bytes");
         var3.initCause(var4);
         throw var3;
      }
   }

   private void parse(DerInputStream var1) throws ParsingException {
      try {
         var1.mark(var1.available());
         this.parse(var1, false);
      } catch (IOException var6) {
         try {
            var1.reset();
            this.parse(var1, true);
            this.oldStyle = true;
         } catch (IOException var5) {
            ParsingException var4 = new ParsingException(var5.getMessage());
            var4.initCause(var6);
            var4.addSuppressed(var5);
            throw var4;
         }
      }

   }

   private void parse(DerInputStream var1, boolean var2) throws IOException {
      this.contentInfo = new ContentInfo(var1, var2);
      this.contentType = this.contentInfo.contentType;
      DerValue var3 = this.contentInfo.getContent();
      if (this.contentType.equals((Object) ContentInfo.SIGNED_DATA_OID)) {
         this.parseSignedData(var3);
      } else if (this.contentType.equals((Object) ContentInfo.OLD_SIGNED_DATA_OID)) {
         this.parseOldSignedData(var3);
      } else {
         if (!this.contentType.equals((Object) ContentInfo.NETSCAPE_CERT_SEQUENCE_OID)) {
            throw new ParsingException("content type " + this.contentType + " not supported.");
         }

         this.parseNetscapeCertChain(var3);
      }

   }

   public PKCS7(AlgorithmId[] var1, ContentInfo var2, X509Certificate[] var3, X509CRL[] var4, SignerInfo[] var5) {
      this.version = null;
      this.digestAlgorithmIds = null;
      this.contentInfo = null;
      this.certificates = null;
      this.crls = null;
      this.signerInfos = null;
      this.oldStyle = false;
      this.version = BigInteger.ONE;
      this.digestAlgorithmIds = var1;
      this.contentInfo = var2;
      this.certificates = var3;
      this.crls = var4;
      this.signerInfos = var5;
   }

   public PKCS7(AlgorithmId[] var1, ContentInfo var2, X509Certificate[] var3, SignerInfo[] var4) {
      this(var1, var2, var3, (X509CRL[])null, var4);
   }

   private void parseNetscapeCertChain(DerValue var1) throws ParsingException, IOException {
      DerInputStream var2 = new DerInputStream(var1.toByteArray());
      DerValue[] var3 = var2.getSequence(2);
      this.certificates = new X509Certificate[var3.length];
      CertificateFactory var4 = null;

      try {
         var4 = CertificateFactory.getInstance("X.509");
      } catch (CertificateException var16) {
      }

      for(int var5 = 0; var5 < var3.length; ++var5) {
         ByteArrayInputStream var6 = null;

         try {
            ParsingException var8;
            try {
               if (var4 == null) {
                  this.certificates[var5] = new X509CertImpl(var3[var5]);
               } else {
                  byte[] var7 = var3[var5].toByteArray();
                  var6 = new ByteArrayInputStream(var7);
                  this.certificates[var5] = (X509Certificate)var4.generateCertificate(var6);
                  var6.close();
                  var6 = null;
               }
            } catch (CertificateException var14) {
               var8 = new ParsingException(var14.getMessage());
               var8.initCause(var14);
               throw var8;
            } catch (IOException var15) {
               var8 = new ParsingException(var15.getMessage());
               var8.initCause(var15);
               throw var8;
            }
         } finally {
            if (var6 != null) {
               var6.close();
            }

         }
      }

   }

   private void parseSignedData(DerValue var1) throws ParsingException, IOException {
      DerInputStream var2 = var1.toDerInputStream();
      this.version = var2.getBigInteger();
      DerValue[] var3 = var2.getSet(1);
      int var4 = var3.length;
      this.digestAlgorithmIds = new AlgorithmId[var4];

      try {
         for(int var5 = 0; var5 < var4; ++var5) {
            DerValue var36 = var3[var5];
            this.digestAlgorithmIds[var5] = AlgorithmId.parse(var36);
         }
      } catch (IOException var34) {
         ParsingException var6 = new ParsingException("Error parsing digest AlgorithmId IDs: " + var34.getMessage());
         var6.initCause(var34);
         throw var6;
      }

      this.contentInfo = new ContentInfo(var2);
      CertificateFactory var35 = null;

      try {
         var35 = CertificateFactory.getInstance("X.509");
      } catch (CertificateException var29) {
      }

      DerValue[] var37;
      int var7;
      if ((byte)var2.peekByte() == -96) {
         var37 = var2.getSet(2, true);
         var4 = var37.length;
         this.certificates = new X509Certificate[var4];
         var7 = 0;

         for(int var8 = 0; var8 < var4; ++var8) {
            ByteArrayInputStream var9 = null;

            try {
               ParsingException var11;
               try {
                  byte var10 = var37[var8].getTag();
                  if (var10 == 48) {
                     if (var35 == null) {
                        this.certificates[var7] = new X509CertImpl(var37[var8]);
                     } else {
                        byte[] var42 = var37[var8].toByteArray();
                        var9 = new ByteArrayInputStream(var42);
                        this.certificates[var7] = (X509Certificate)var35.generateCertificate(var9);
                        var9.close();
                        var9 = null;
                     }

                     ++var7;
                  }
               } catch (CertificateException var31) {
                  var11 = new ParsingException(var31.getMessage());
                  var11.initCause(var31);
                  throw var11;
               } catch (IOException var32) {
                  var11 = new ParsingException(var32.getMessage());
                  var11.initCause(var32);
                  throw var11;
               }
            } finally {
               if (var9 != null) {
                  var9.close();
               }

            }
         }

         if (var7 != var4) {
            this.certificates = (X509Certificate[]) Arrays.copyOf((Object[])this.certificates, var7);
         }
      }

      if ((byte)var2.peekByte() == -95) {
         var37 = var2.getSet(1, true);
         var4 = var37.length;
         this.crls = new X509CRL[var4];

         for(var7 = 0; var7 < var4; ++var7) {
            ByteArrayInputStream var38 = null;

            try {
               if (var35 == null) {
                  this.crls[var7] = new X509CRLImpl(var37[var7]);
               } else {
                  byte[] var41 = var37[var7].toByteArray();
                  var38 = new ByteArrayInputStream(var41);
                  this.crls[var7] = (X509CRL)var35.generateCRL(var38);
                  var38.close();
                  var38 = null;
               }
            } catch (CRLException var28) {
               ParsingException var40 = new ParsingException(var28.getMessage());
               var40.initCause(var28);
               throw var40;
            } finally {
               if (var38 != null) {
                  var38.close();
               }

            }
         }
      }

      var37 = var2.getSet(1);
      var4 = var37.length;
      this.signerInfos = new SignerInfo[var4];

      for(var7 = 0; var7 < var4; ++var7) {
         DerInputStream var39 = var37[var7].toDerInputStream();
         this.signerInfos[var7] = new SignerInfo(var39);
      }

   }

   private void parseOldSignedData(DerValue var1) throws ParsingException, IOException {
      DerInputStream var2 = var1.toDerInputStream();
      this.version = var2.getBigInteger();
      DerValue[] var3 = var2.getSet(1);
      int var4 = var3.length;
      this.digestAlgorithmIds = new AlgorithmId[var4];

      try {
         for(int var5 = 0; var5 < var4; ++var5) {
            DerValue var6 = var3[var5];
            this.digestAlgorithmIds[var5] = AlgorithmId.parse(var6);
         }
      } catch (IOException var21) {
         throw new ParsingException("Error parsing digest AlgorithmId IDs");
      }

      this.contentInfo = new ContentInfo(var2, true);
      CertificateFactory var22 = null;

      try {
         var22 = CertificateFactory.getInstance("X.509");
      } catch (CertificateException var19) {
      }

      DerValue[] var23 = var2.getSet(2);
      var4 = var23.length;
      this.certificates = new X509Certificate[var4];

      for(int var7 = 0; var7 < var4; ++var7) {
         ByteArrayInputStream var8 = null;

         try {
            ParsingException var10;
            try {
               if (var22 == null) {
                  this.certificates[var7] = new X509CertImpl(var23[var7]);
               } else {
                  byte[] var9 = var23[var7].toByteArray();
                  var8 = new ByteArrayInputStream(var9);
                  this.certificates[var7] = (X509Certificate)var22.generateCertificate(var8);
                  var8.close();
                  var8 = null;
               }
            } catch (CertificateException var17) {
               var10 = new ParsingException(var17.getMessage());
               var10.initCause(var17);
               throw var10;
            } catch (IOException var18) {
               var10 = new ParsingException(var18.getMessage());
               var10.initCause(var18);
               throw var10;
            }
         } finally {
            if (var8 != null) {
               var8.close();
            }

         }
      }

      var2.getSet(0);
      DerValue[] var24 = var2.getSet(1);
      var4 = var24.length;
      this.signerInfos = new SignerInfo[var4];

      for(int var25 = 0; var25 < var4; ++var25) {
         DerInputStream var26 = var24[var25].toDerInputStream();
         this.signerInfos[var25] = new SignerInfo(var26, true);
      }

   }

   public void encodeSignedData(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      this.encodeSignedData(var2);
      var1.write(var2.toByteArray());
   }

   public void encodeSignedData(DerOutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      var2.putInteger(this.version);
      var2.putOrderedSetOf((byte)49, this.digestAlgorithmIds);
      this.contentInfo.encode(var2);
      if (this.certificates != null && this.certificates.length != 0) {
         X509CertImpl[] var3 = new X509CertImpl[this.certificates.length];

         for(int var4 = 0; var4 < this.certificates.length; ++var4) {
            if (this.certificates[var4] instanceof X509CertImpl) {
               var3[var4] = (X509CertImpl)this.certificates[var4];
            } else {
               try {
                  byte[] var5 = this.certificates[var4].getEncoded();
                  var3[var4] = new X509CertImpl(var5);
               } catch (CertificateException var10) {
                  throw new IOException(var10);
               }
            }
         }

         var2.putOrderedSetOf((byte)-96, var3);
      }

      if (this.crls != null && this.crls.length != 0) {
         HashSet var11 = new HashSet(this.crls.length);
         X509CRL[] var13 = this.crls;
         int var15 = var13.length;

         for(int var6 = 0; var6 < var15; ++var6) {
            X509CRL var7 = var13[var6];
            if (var7 instanceof X509CRLImpl) {
               var11.add((X509CRLImpl)var7);
            } else {
               try {
                  byte[] var8 = var7.getEncoded();
                  var11.add(new X509CRLImpl(var8));
               } catch (CRLException var9) {
                  throw new IOException(var9);
               }
            }
         }

         var2.putOrderedSetOf((byte)-95, (DerEncoder[])var11.toArray(new X509CRLImpl[var11.size()]));
      }

      var2.putOrderedSetOf((byte)49, this.signerInfos);
      DerValue var12 = new DerValue((byte)48, var2.toByteArray());
      ContentInfo var14 = new ContentInfo(ContentInfo.SIGNED_DATA_OID, var12);
      var14.encode(var1);
   }

   public SignerInfo verify(SignerInfo var1, byte[] var2) throws NoSuchAlgorithmException, SignatureException {
      return var1.verify(this, var2);
   }

   public SignerInfo[] verify(byte[] var1) throws NoSuchAlgorithmException, SignatureException {
      Vector var2 = new Vector();

      for(int var3 = 0; var3 < this.signerInfos.length; ++var3) {
         SignerInfo var4 = this.verify(this.signerInfos[var3], var1);
         if (var4 != null) {
            var2.addElement(var4);
         }
      }

      if (!var2.isEmpty()) {
         SignerInfo[] var5 = new SignerInfo[var2.size()];
         var2.copyInto(var5);
         return var5;
      } else {
         return null;
      }
   }

   public SignerInfo[] verify() throws NoSuchAlgorithmException, SignatureException {
      return this.verify((byte[])null);
   }

   public BigInteger getVersion() {
      return this.version;
   }

   public AlgorithmId[] getDigestAlgorithmIds() {
      return this.digestAlgorithmIds;
   }

   public ContentInfo getContentInfo() {
      return this.contentInfo;
   }

   public X509Certificate[] getCertificates() {
      return this.certificates != null ? (X509Certificate[])this.certificates.clone() : null;
   }

   public X509CRL[] getCRLs() {
      return this.crls != null ? (X509CRL[])this.crls.clone() : null;
   }

   public SignerInfo[] getSignerInfos() {
      return this.signerInfos;
   }

   public X509Certificate getCertificate(BigInteger var1, X500Name var2) {
      if (this.certificates != null) {
         if (this.certIssuerNames == null) {
            this.populateCertIssuerNames();
         }

         for(int var3 = 0; var3 < this.certificates.length; ++var3) {
            X509Certificate var4 = this.certificates[var3];
            BigInteger var5 = var4.getSerialNumber();
            if (var1.equals(var5) && var2.equals(this.certIssuerNames[var3])) {
               return var4;
            }
         }
      }

      return null;
   }

   private void populateCertIssuerNames() {
      if (this.certificates != null) {
         this.certIssuerNames = new Principal[this.certificates.length];

         for(int var1 = 0; var1 < this.certificates.length; ++var1) {
            X509Certificate var2 = this.certificates[var1];
            Principal var3 = var2.getIssuerDN();
            if (!(var3 instanceof X500Name)) {
               try {
                  X509CertInfo var4 = new X509CertInfo(var2.getTBSCertificate());
                  var3 = (Principal)var4.get("issuer.dname");
               } catch (Exception var5) {
               }
            }

            this.certIssuerNames[var1] = var3;
         }

      }
   }

   public String toString() {
      String var1 = "";
      var1 = var1 + this.contentInfo + "\n";
      if (this.version != null) {
         var1 = var1 + "PKCS7 :: version: " + Debug.toHexString(this.version) + "\n";
      }

      int var2;
      if (this.digestAlgorithmIds != null) {
         var1 = var1 + "PKCS7 :: digest AlgorithmIds: \n";

         for(var2 = 0; var2 < this.digestAlgorithmIds.length; ++var2) {
            var1 = var1 + "\t" + this.digestAlgorithmIds[var2] + "\n";
         }
      }

      if (this.certificates != null) {
         var1 = var1 + "PKCS7 :: certificates: \n";

         for(var2 = 0; var2 < this.certificates.length; ++var2) {
            var1 = var1 + "\t" + var2 + ".   " + this.certificates[var2] + "\n";
         }
      }

      if (this.crls != null) {
         var1 = var1 + "PKCS7 :: crls: \n";

         for(var2 = 0; var2 < this.crls.length; ++var2) {
            var1 = var1 + "\t" + var2 + ".   " + this.crls[var2] + "\n";
         }
      }

      if (this.signerInfos != null) {
         var1 = var1 + "PKCS7 :: signer infos: \n";

         for(var2 = 0; var2 < this.signerInfos.length; ++var2) {
            var1 = var1 + "\t" + var2 + ".  " + this.signerInfos[var2] + "\n";
         }
      }

      return var1;
   }

   public boolean isOldStyle() {
      return this.oldStyle;
   }

   private static class SecureRandomHolder {
      static final SecureRandom RANDOM;

      static {
         SecureRandom var0 = null;

         try {
            var0 = SecureRandom.getInstance("SHA1PRNG");
         } catch (NoSuchAlgorithmException var2) {
         }

         RANDOM = var0;
      }
   }
}
