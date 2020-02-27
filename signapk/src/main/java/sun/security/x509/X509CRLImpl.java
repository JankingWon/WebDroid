package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.security.auth.x500.X500Principal;

import sun.misc.HexDumpEncoder;
import sun.security.provider.X509Factory;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class X509CRLImpl extends X509CRL implements DerEncoder {
   private byte[] signedCRL;
   private byte[] signature;
   private byte[] tbsCertList;
   private AlgorithmId sigAlgId;
   private int version;
   private AlgorithmId infoSigAlgId;
   private X500Name issuer;
   private X500Principal issuerPrincipal;
   private Date thisUpdate;
   private Date nextUpdate;
   private Map<X509IssuerSerial, X509CRLEntry> revokedMap;
   private List<X509CRLEntry> revokedList;
   private CRLExtensions extensions;
   private static final boolean isExplicit = true;
   private static final long YR_2050 = 2524636800000L;
   private boolean readOnly;
   private PublicKey verifiedPublicKey;
   private String verifiedProvider;

   private X509CRLImpl() {
      this.signedCRL = null;
      this.signature = null;
      this.tbsCertList = null;
      this.sigAlgId = null;
      this.issuer = null;
      this.issuerPrincipal = null;
      this.thisUpdate = null;
      this.nextUpdate = null;
      this.revokedMap = new TreeMap();
      this.revokedList = new LinkedList();
      this.extensions = null;
      this.readOnly = false;
   }

   public X509CRLImpl(byte[] var1) throws CRLException {
      this.signedCRL = null;
      this.signature = null;
      this.tbsCertList = null;
      this.sigAlgId = null;
      this.issuer = null;
      this.issuerPrincipal = null;
      this.thisUpdate = null;
      this.nextUpdate = null;
      this.revokedMap = new TreeMap();
      this.revokedList = new LinkedList();
      this.extensions = null;
      this.readOnly = false;

      try {
         this.parse(new DerValue(var1));
      } catch (IOException var3) {
         this.signedCRL = null;
         throw new CRLException("Parsing error: " + var3.getMessage());
      }
   }

   public X509CRLImpl(DerValue var1) throws CRLException {
      this.signedCRL = null;
      this.signature = null;
      this.tbsCertList = null;
      this.sigAlgId = null;
      this.issuer = null;
      this.issuerPrincipal = null;
      this.thisUpdate = null;
      this.nextUpdate = null;
      this.revokedMap = new TreeMap();
      this.revokedList = new LinkedList();
      this.extensions = null;
      this.readOnly = false;

      try {
         this.parse(var1);
      } catch (IOException var3) {
         this.signedCRL = null;
         throw new CRLException("Parsing error: " + var3.getMessage());
      }
   }

   public X509CRLImpl(InputStream var1) throws CRLException {
      this.signedCRL = null;
      this.signature = null;
      this.tbsCertList = null;
      this.sigAlgId = null;
      this.issuer = null;
      this.issuerPrincipal = null;
      this.thisUpdate = null;
      this.nextUpdate = null;
      this.revokedMap = new TreeMap();
      this.revokedList = new LinkedList();
      this.extensions = null;
      this.readOnly = false;

      try {
         this.parse(new DerValue(var1));
      } catch (IOException var3) {
         this.signedCRL = null;
         throw new CRLException("Parsing error: " + var3.getMessage());
      }
   }

   public X509CRLImpl(X500Name var1, Date var2, Date var3) {
      this.signedCRL = null;
      this.signature = null;
      this.tbsCertList = null;
      this.sigAlgId = null;
      this.issuer = null;
      this.issuerPrincipal = null;
      this.thisUpdate = null;
      this.nextUpdate = null;
      this.revokedMap = new TreeMap();
      this.revokedList = new LinkedList();
      this.extensions = null;
      this.readOnly = false;
      this.issuer = var1;
      this.thisUpdate = var2;
      this.nextUpdate = var3;
   }

   public X509CRLImpl(X500Name var1, Date var2, Date var3, X509CRLEntry[] var4) throws CRLException {
      this.signedCRL = null;
      this.signature = null;
      this.tbsCertList = null;
      this.sigAlgId = null;
      this.issuer = null;
      this.issuerPrincipal = null;
      this.thisUpdate = null;
      this.nextUpdate = null;
      this.revokedMap = new TreeMap();
      this.revokedList = new LinkedList();
      this.extensions = null;
      this.readOnly = false;
      this.issuer = var1;
      this.thisUpdate = var2;
      this.nextUpdate = var3;
      if (var4 != null) {
         X500Principal var5 = this.getIssuerX500Principal();
         X500Principal var6 = var5;

         for(int var7 = 0; var7 < var4.length; ++var7) {
            X509CRLEntryImpl var8 = (X509CRLEntryImpl)var4[var7];

            try {
               var6 = this.getCertIssuer(var8, var6);
            } catch (IOException var10) {
               throw new CRLException(var10);
            }

            var8.setCertificateIssuer(var5, var6);
            X509IssuerSerial var9 = new X509IssuerSerial(var6, var8.getSerialNumber());
            this.revokedMap.put(var9, var8);
            this.revokedList.add(var8);
            if (var8.hasExtensions()) {
               this.version = 1;
            }
         }
      }

   }

   public X509CRLImpl(X500Name var1, Date var2, Date var3, X509CRLEntry[] var4, CRLExtensions var5) throws CRLException {
      this(var1, var2, var3, var4);
      if (var5 != null) {
         this.extensions = var5;
         this.version = 1;
      }

   }

   public byte[] getEncodedInternal() throws CRLException {
      if (this.signedCRL == null) {
         throw new CRLException("Null CRL to encode");
      } else {
         return this.signedCRL;
      }
   }

   public byte[] getEncoded() throws CRLException {
      return (byte[])this.getEncodedInternal().clone();
   }

   public void encodeInfo(OutputStream var1) throws CRLException {
      try {
         DerOutputStream var2 = new DerOutputStream();
         DerOutputStream var3 = new DerOutputStream();
         DerOutputStream var4 = new DerOutputStream();
         if (this.version != 0) {
            var2.putInteger(this.version);
         }

         this.infoSigAlgId.encode(var2);
         if (this.version == 0 && this.issuer.toString() == null) {
            throw new CRLException("Null Issuer DN not allowed in v1 CRL");
         } else {
            this.issuer.encode(var2);
            if (this.thisUpdate.getTime() < 2524636800000L) {
               var2.putUTCTime(this.thisUpdate);
            } else {
               var2.putGeneralizedTime(this.thisUpdate);
            }

            if (this.nextUpdate != null) {
               if (this.nextUpdate.getTime() < 2524636800000L) {
                  var2.putUTCTime(this.nextUpdate);
               } else {
                  var2.putGeneralizedTime(this.nextUpdate);
               }
            }

            if (!this.revokedList.isEmpty()) {
               Iterator var5 = this.revokedList.iterator();

               while(var5.hasNext()) {
                  X509CRLEntry var6 = (X509CRLEntry)var5.next();
                  ((X509CRLEntryImpl)var6).encode(var3);
               }

               var2.write((byte)48, (DerOutputStream)var3);
            }

            if (this.extensions != null) {
               this.extensions.encode(var2, true);
            }

            var4.write((byte)48, (DerOutputStream)var2);
            this.tbsCertList = var4.toByteArray();
            var1.write(this.tbsCertList);
         }
      } catch (IOException var7) {
         throw new CRLException("Encoding error: " + var7.getMessage());
      }
   }

   public void verify(PublicKey var1) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
      this.verify(var1, "");
   }

   public synchronized void verify(PublicKey var1, String var2) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
      if (var2 == null) {
         var2 = "";
      }

      if (this.verifiedPublicKey == null || !this.verifiedPublicKey.equals(var1) || !var2.equals(this.verifiedProvider)) {
         if (this.signedCRL == null) {
            throw new CRLException("Uninitialized CRL");
         } else {
            Signature var3 = null;
            if (var2.length() == 0) {
               var3 = Signature.getInstance(this.sigAlgId.getName());
            } else {
               var3 = Signature.getInstance(this.sigAlgId.getName(), var2);
            }

            var3.initVerify(var1);
            if (this.tbsCertList == null) {
               throw new CRLException("Uninitialized CRL");
            } else {
               var3.update(this.tbsCertList, 0, this.tbsCertList.length);
               if (!var3.verify(this.signature)) {
                  throw new SignatureException("Signature does not match.");
               } else {
                  this.verifiedPublicKey = var1;
                  this.verifiedProvider = var2;
               }
            }
         }
      }
   }

   public synchronized void verify(PublicKey var1, Provider var2) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
      if (this.signedCRL == null) {
         throw new CRLException("Uninitialized CRL");
      } else {
         Signature var3 = null;
         if (var2 == null) {
            var3 = Signature.getInstance(this.sigAlgId.getName());
         } else {
            var3 = Signature.getInstance(this.sigAlgId.getName(), var2);
         }

         var3.initVerify(var1);
         if (this.tbsCertList == null) {
            throw new CRLException("Uninitialized CRL");
         } else {
            var3.update(this.tbsCertList, 0, this.tbsCertList.length);
            if (!var3.verify(this.signature)) {
               throw new SignatureException("Signature does not match.");
            } else {
               this.verifiedPublicKey = var1;
            }
         }
      }
   }

   public static void verify(X509CRL var0, PublicKey var1, Provider var2) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
      var0.verify(var1, var2);
   }

   public void sign(PrivateKey var1, String var2) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
      this.sign(var1, var2, (String)null);
   }

   public void sign(PrivateKey var1, String var2, String var3) throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
      try {
         if (this.readOnly) {
            throw new CRLException("cannot over-write existing CRL");
         } else {
            Signature var4 = null;
            if (var3 != null && var3.length() != 0) {
               var4 = Signature.getInstance(var2, var3);
            } else {
               var4 = Signature.getInstance(var2);
            }

            var4.initSign(var1);
            this.sigAlgId = AlgorithmId.get(var4.getAlgorithm());
            this.infoSigAlgId = this.sigAlgId;
            DerOutputStream var5 = new DerOutputStream();
            DerOutputStream var6 = new DerOutputStream();
            this.encodeInfo(var6);
            this.sigAlgId.encode(var6);
            var4.update(this.tbsCertList, 0, this.tbsCertList.length);
            this.signature = var4.sign();
            var6.putBitString(this.signature);
            var5.write((byte)48, (DerOutputStream)var6);
            this.signedCRL = var5.toByteArray();
            this.readOnly = true;
         }
      } catch (IOException var7) {
         throw new CRLException("Error while encoding data: " + var7.getMessage());
      }
   }

   public String toString() {
      return this.toStringWithAlgName("" + this.sigAlgId);
   }

   public String toStringWithAlgName(String var1) {
      StringBuffer var2 = new StringBuffer();
      var2.append("X.509 CRL v" + (this.version + 1) + "\n");
      if (this.sigAlgId != null) {
         var2.append("Signature Algorithm: " + var1.toString() + ", OID=" + this.sigAlgId.getOID().toString() + "\n");
      }

      if (this.issuer != null) {
         var2.append("Issuer: " + this.issuer.toString() + "\n");
      }

      if (this.thisUpdate != null) {
         var2.append("\nThis Update: " + this.thisUpdate.toString() + "\n");
      }

      if (this.nextUpdate != null) {
         var2.append("Next Update: " + this.nextUpdate.toString() + "\n");
      }

      if (this.revokedList.isEmpty()) {
         var2.append("\nNO certificates have been revoked\n");
      } else {
         var2.append("\nRevoked Certificates: " + this.revokedList.size());
         int var3 = 1;
         Iterator var4 = this.revokedList.iterator();

         while(var4.hasNext()) {
            X509CRLEntry var5 = (X509CRLEntry)var4.next();
            var2.append("\n[" + var3++ + "] " + var5.toString());
         }
      }

      if (this.extensions != null) {
         Collection var11 = this.extensions.getAllExtensions();
         Object[] var13 = var11.toArray();
         var2.append("\nCRL Extensions: " + var13.length);

         for(int var14 = 0; var14 < var13.length; ++var14) {
            var2.append("\n[" + (var14 + 1) + "]: ");
            Extension var6 = (Extension)var13[var14];

            try {
               if (OIDMap.getClass(var6.getExtensionId()) == null) {
                  var2.append(var6.toString());
                  byte[] var7 = var6.getExtensionValue();
                  if (var7 != null) {
                     DerOutputStream var8 = new DerOutputStream();
                     var8.putOctetString(var7);
                     var7 = var8.toByteArray();
                     HexDumpEncoder var9 = new HexDumpEncoder();
                     var2.append("Extension unknown: DER encoded OCTET string =\n" + var9.encodeBuffer(var7) + "\n");
                  }
               } else {
                  var2.append(var6.toString());
               }
            } catch (Exception var10) {
               var2.append(", Error parsing this extension");
            }
         }
      }

      if (this.signature != null) {
         HexDumpEncoder var12 = new HexDumpEncoder();
         var2.append("\nSignature:\n" + var12.encodeBuffer(this.signature) + "\n");
      } else {
         var2.append("NOT signed yet\n");
      }

      return var2.toString();
   }

   public boolean isRevoked(Certificate var1) {
      if (!this.revokedMap.isEmpty() && var1 instanceof X509Certificate) {
         X509Certificate var2 = (X509Certificate)var1;
         X509IssuerSerial var3 = new X509IssuerSerial(var2);
         return this.revokedMap.containsKey(var3);
      } else {
         return false;
      }
   }

   public int getVersion() {
      return this.version + 1;
   }

   public Principal getIssuerDN() {
      return this.issuer;
   }

   public X500Principal getIssuerX500Principal() {
      if (this.issuerPrincipal == null) {
         this.issuerPrincipal = this.issuer.asX500Principal();
      }

      return this.issuerPrincipal;
   }

   public Date getThisUpdate() {
      return new Date(this.thisUpdate.getTime());
   }

   public Date getNextUpdate() {
      return this.nextUpdate == null ? null : new Date(this.nextUpdate.getTime());
   }

   public X509CRLEntry getRevokedCertificate(BigInteger var1) {
      if (this.revokedMap.isEmpty()) {
         return null;
      } else {
         X509IssuerSerial var2 = new X509IssuerSerial(this.getIssuerX500Principal(), var1);
         return (X509CRLEntry)this.revokedMap.get(var2);
      }
   }

   public X509CRLEntry getRevokedCertificate(X509Certificate var1) {
      if (this.revokedMap.isEmpty()) {
         return null;
      } else {
         X509IssuerSerial var2 = new X509IssuerSerial(var1);
         return (X509CRLEntry)this.revokedMap.get(var2);
      }
   }

   public Set<X509CRLEntry> getRevokedCertificates() {
      return this.revokedList.isEmpty() ? null : new TreeSet(this.revokedList);
   }

   public byte[] getTBSCertList() throws CRLException {
      if (this.tbsCertList == null) {
         throw new CRLException("Uninitialized CRL");
      } else {
         return (byte[])this.tbsCertList.clone();
      }
   }

   public byte[] getSignature() {
      return this.signature == null ? null : (byte[])this.signature.clone();
   }

   public String getSigAlgName() {
      return this.sigAlgId == null ? null : this.sigAlgId.getName();
   }

   public String getSigAlgOID() {
      if (this.sigAlgId == null) {
         return null;
      } else {
         ObjectIdentifier var1 = this.sigAlgId.getOID();
         return var1.toString();
      }
   }

   public byte[] getSigAlgParams() {
      if (this.sigAlgId == null) {
         return null;
      } else {
         try {
            return this.sigAlgId.getEncodedParams();
         } catch (IOException var2) {
            return null;
         }
      }
   }

   public AlgorithmId getSigAlgId() {
      return this.sigAlgId;
   }

   public KeyIdentifier getAuthKeyId() throws IOException {
      AuthorityKeyIdentifierExtension var1 = this.getAuthKeyIdExtension();
      if (var1 != null) {
         KeyIdentifier var2 = (KeyIdentifier)var1.get("key_id");
         return var2;
      } else {
         return null;
      }
   }

   public AuthorityKeyIdentifierExtension getAuthKeyIdExtension() throws IOException {
      Object var1 = this.getExtension(PKIXExtensions.AuthorityKey_Id);
      return (AuthorityKeyIdentifierExtension)var1;
   }

   public CRLNumberExtension getCRLNumberExtension() throws IOException {
      Object var1 = this.getExtension(PKIXExtensions.CRLNumber_Id);
      return (CRLNumberExtension)var1;
   }

   public BigInteger getCRLNumber() throws IOException {
      CRLNumberExtension var1 = this.getCRLNumberExtension();
      if (var1 != null) {
         BigInteger var2 = var1.get("value");
         return var2;
      } else {
         return null;
      }
   }

   public DeltaCRLIndicatorExtension getDeltaCRLIndicatorExtension() throws IOException {
      Object var1 = this.getExtension(PKIXExtensions.DeltaCRLIndicator_Id);
      return (DeltaCRLIndicatorExtension)var1;
   }

   public BigInteger getBaseCRLNumber() throws IOException {
      DeltaCRLIndicatorExtension var1 = this.getDeltaCRLIndicatorExtension();
      if (var1 != null) {
         BigInteger var2 = var1.get("value");
         return var2;
      } else {
         return null;
      }
   }

   public IssuerAlternativeNameExtension getIssuerAltNameExtension() throws IOException {
      Object var1 = this.getExtension(PKIXExtensions.IssuerAlternativeName_Id);
      return (IssuerAlternativeNameExtension)var1;
   }

   public IssuingDistributionPointExtension getIssuingDistributionPointExtension() throws IOException {
      Object var1 = this.getExtension(PKIXExtensions.IssuingDistributionPoint_Id);
      return (IssuingDistributionPointExtension)var1;
   }

   public boolean hasUnsupportedCriticalExtension() {
      return this.extensions == null ? false : this.extensions.hasUnsupportedCriticalExtension();
   }

   public Set<String> getCriticalExtensionOIDs() {
      if (this.extensions == null) {
         return null;
      } else {
         TreeSet var1 = new TreeSet();
         Iterator var2 = this.extensions.getAllExtensions().iterator();

         while(var2.hasNext()) {
            Extension var3 = (Extension)var2.next();
            if (var3.isCritical()) {
               var1.add(var3.getExtensionId().toString());
            }
         }

         return var1;
      }
   }

   public Set<String> getNonCriticalExtensionOIDs() {
      if (this.extensions == null) {
         return null;
      } else {
         TreeSet var1 = new TreeSet();
         Iterator var2 = this.extensions.getAllExtensions().iterator();

         while(var2.hasNext()) {
            Extension var3 = (Extension)var2.next();
            if (!var3.isCritical()) {
               var1.add(var3.getExtensionId().toString());
            }
         }

         return var1;
      }
   }

   public byte[] getExtensionValue(String var1) {
      if (this.extensions == null) {
         return null;
      } else {
         try {
            String var2 = OIDMap.getName(new ObjectIdentifier(var1));
            Extension var3 = null;
            if (var2 == null) {
               ObjectIdentifier var4 = new ObjectIdentifier(var1);
               Extension var5 = null;
               Enumeration var7 = this.extensions.getElements();

               while(var7.hasMoreElements()) {
                  var5 = (Extension)var7.nextElement();
                  ObjectIdentifier var6 = var5.getExtensionId();
                  if (var6.equals((Object)var4)) {
                     var3 = var5;
                     break;
                  }
               }
            } else {
               var3 = this.extensions.get(var2);
            }

            if (var3 == null) {
               return null;
            } else {
               byte[] var9 = var3.getExtensionValue();
               if (var9 == null) {
                  return null;
               } else {
                  DerOutputStream var10 = new DerOutputStream();
                  var10.putOctetString(var9);
                  return var10.toByteArray();
               }
            }
         } catch (Exception var8) {
            return null;
         }
      }
   }

   public Object getExtension(ObjectIdentifier var1) {
      return this.extensions == null ? null : this.extensions.get(OIDMap.getName(var1));
   }

   private void parse(DerValue var1) throws CRLException, IOException {
      if (this.readOnly) {
         throw new CRLException("cannot over-write existing CRL");
      } else if (var1.getData() != null && var1.tag == 48) {
         this.signedCRL = var1.toByteArray();
         DerValue[] var2 = new DerValue[]{var1.data.getDerValue(), var1.data.getDerValue(), var1.data.getDerValue()};
         if (var1.data.available() != 0) {
            throw new CRLException("signed overrun, bytes = " + var1.data.available());
         } else if (var2[0].tag != 48) {
            throw new CRLException("signed CRL fields invalid");
         } else {
            this.sigAlgId = AlgorithmId.parse(var2[1]);
            this.signature = var2[2].getBitString();
            if (var2[1].data.available() != 0) {
               throw new CRLException("AlgorithmId field overrun");
            } else if (var2[2].data.available() != 0) {
               throw new CRLException("Signature field overrun");
            } else {
               this.tbsCertList = var2[0].toByteArray();
               DerInputStream var3 = var2[0].data;
               this.version = 0;
               byte var5 = (byte)var3.peekByte();
               if (var5 == 2) {
                  this.version = var3.getInteger();
                  if (this.version != 1) {
                     throw new CRLException("Invalid version");
                  }
               }

               DerValue var4 = var3.getDerValue();
               AlgorithmId var6 = AlgorithmId.parse(var4);
               if (!var6.equals(this.sigAlgId)) {
                  throw new CRLException("Signature algorithm mismatch");
               } else {
                  this.infoSigAlgId = var6;
                  this.issuer = new X500Name(var3);
                  if (this.issuer.isEmpty()) {
                     throw new CRLException("Empty issuer DN not allowed in X509CRLs");
                  } else {
                     var5 = (byte)var3.peekByte();
                     if (var5 == 23) {
                        this.thisUpdate = var3.getUTCTime();
                     } else {
                        if (var5 != 24) {
                           throw new CRLException("Invalid encoding for thisUpdate (tag=" + var5 + ")");
                        }

                        this.thisUpdate = var3.getGeneralizedTime();
                     }

                     if (var3.available() != 0) {
                        var5 = (byte)var3.peekByte();
                        if (var5 == 23) {
                           this.nextUpdate = var3.getUTCTime();
                        } else if (var5 == 24) {
                           this.nextUpdate = var3.getGeneralizedTime();
                        }

                        if (var3.available() != 0) {
                           var5 = (byte)var3.peekByte();
                           if (var5 == 48 && (var5 & 192) != 128) {
                              DerValue[] var7 = var3.getSequence(4);
                              X500Principal var8 = this.getIssuerX500Principal();
                              X500Principal var9 = var8;

                              for(int var10 = 0; var10 < var7.length; ++var10) {
                                 X509CRLEntryImpl var11 = new X509CRLEntryImpl(var7[var10]);
                                 var9 = this.getCertIssuer(var11, var9);
                                 var11.setCertificateIssuer(var8, var9);
                                 X509IssuerSerial var12 = new X509IssuerSerial(var9, var11.getSerialNumber());
                                 this.revokedMap.put(var12, var11);
                                 this.revokedList.add(var11);
                              }
                           }

                           if (var3.available() != 0) {
                              var4 = var3.getDerValue();
                              if (var4.isConstructed() && var4.isContextSpecific((byte)0)) {
                                 this.extensions = new CRLExtensions(var4.data);
                              }

                              this.readOnly = true;
                           }
                        }
                     }
                  }
               }
            }
         }
      } else {
         throw new CRLException("Invalid DER-encoded CRL data");
      }
   }

   public static X500Principal getIssuerX500Principal(X509CRL var0) {
      try {
         byte[] var1 = var0.getEncoded();
         DerInputStream var2 = new DerInputStream(var1);
         DerValue var3 = var2.getSequence(3)[0];
         DerInputStream var4 = var3.data;
         byte var6 = (byte)var4.peekByte();
         DerValue var5;
         if (var6 == 2) {
            var5 = var4.getDerValue();
         }

         var5 = var4.getDerValue();
         var5 = var4.getDerValue();
         byte[] var7 = var5.toByteArray();
         return new X500Principal(var7);
      } catch (Exception var8) {
         throw new RuntimeException("Could not parse issuer", var8);
      }
   }

   public static byte[] getEncodedInternal(X509CRL var0) throws CRLException {
      return var0 instanceof X509CRLImpl ? ((X509CRLImpl)var0).getEncodedInternal() : var0.getEncoded();
   }

   public static X509CRLImpl toImpl(X509CRL var0) throws CRLException {
      return var0 instanceof X509CRLImpl ? (X509CRLImpl)var0 : X509Factory.intern(var0);
   }

   private X500Principal getCertIssuer(X509CRLEntryImpl var1, X500Principal var2) throws IOException {
      CertificateIssuerExtension var3 = var1.getCertificateIssuerExtension();
      if (var3 != null) {
         GeneralNames var4 = var3.get("issuer");
         X500Name var5 = (X500Name)var4.get(0).getName();
         return var5.asX500Principal();
      } else {
         return var2;
      }
   }

   public void derEncode(OutputStream var1) throws IOException {
      if (this.signedCRL == null) {
         throw new IOException("Null CRL to encode");
      } else {
         var1.write((byte[])this.signedCRL.clone());
      }
   }

   private static final class X509IssuerSerial implements Comparable<X509IssuerSerial> {
      final X500Principal issuer;
      final BigInteger serial;
      volatile int hashcode;

      X509IssuerSerial(X500Principal var1, BigInteger var2) {
         this.hashcode = 0;
         this.issuer = var1;
         this.serial = var2;
      }

      X509IssuerSerial(X509Certificate var1) {
         this(var1.getIssuerX500Principal(), var1.getSerialNumber());
      }

      X500Principal getIssuer() {
         return this.issuer;
      }

      BigInteger getSerial() {
         return this.serial;
      }

      public boolean equals(Object var1) {
         if (var1 == this) {
            return true;
         } else if (!(var1 instanceof X509IssuerSerial)) {
            return false;
         } else {
            X509IssuerSerial var2 = (X509IssuerSerial)var1;
            return this.serial.equals(var2.getSerial()) && this.issuer.equals(var2.getIssuer());
         }
      }

      public int hashCode() {
         if (this.hashcode == 0) {
            byte var1 = 17;
            int var2 = 37 * var1 + this.issuer.hashCode();
            var2 = 37 * var2 + this.serial.hashCode();
            this.hashcode = var2;
         }

         return this.hashcode;
      }

      public int compareTo(X509IssuerSerial var1) {
         int var2 = this.issuer.toString().compareTo(var1.issuer.toString());
         return var2 != 0 ? var2 : this.serial.compareTo(var1.serial);
      }
   }
}
