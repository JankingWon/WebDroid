package sun.security.x509;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.x500.X500Principal;

import sun.misc.HexDumpEncoder;
import sun.security.provider.X509Factory;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.util.Pem;

public class X509CertImpl extends X509Certificate implements DerEncoder {
   private static final long serialVersionUID = -3457612960190864406L;
   private static final String DOT = ".";
   public static final String NAME = "x509";
   public static final String INFO = "info";
   public static final String ALG_ID = "algorithm";
   public static final String SIGNATURE = "signature";
   public static final String SIGNED_CERT = "signed_cert";
   public static final String SUBJECT_DN = "x509.info.subject.dname";
   public static final String ISSUER_DN = "x509.info.issuer.dname";
   public static final String SERIAL_ID = "x509.info.serialNumber.number";
   public static final String PUBLIC_KEY = "x509.info.key.value";
   public static final String VERSION = "x509.info.version.number";
   public static final String SIG_ALG = "x509.algorithm";
   public static final String SIG = "x509.signature";
   private boolean readOnly = false;
   private byte[] signedCert = null;
   protected X509CertInfo info = null;
   protected AlgorithmId algId = null;
   protected byte[] signature = null;
   private static final String KEY_USAGE_OID = "2.5.29.15";
   private static final String EXTENDED_KEY_USAGE_OID = "2.5.29.37";
   private static final String BASIC_CONSTRAINT_OID = "2.5.29.19";
   private static final String SUBJECT_ALT_NAME_OID = "2.5.29.17";
   private static final String ISSUER_ALT_NAME_OID = "2.5.29.18";
   private static final String AUTH_INFO_ACCESS_OID = "1.3.6.1.5.5.7.1.1";
   private static final int NUM_STANDARD_KEY_USAGE = 9;
   private Collection<List<?>> subjectAlternativeNames;
   private Collection<List<?>> issuerAlternativeNames;
   private List<String> extKeyUsage;
   private Set<AccessDescription> authInfoAccess;
   private PublicKey verifiedPublicKey;
   private String verifiedProvider;
   private boolean verificationResult;
   private ConcurrentHashMap<String, String> fingerprints = new ConcurrentHashMap(2);

   public X509CertImpl() {
   }

   public X509CertImpl(byte[] var1) throws CertificateException {
      try {
         this.parse(new DerValue(var1));
      } catch (IOException var3) {
         this.signedCert = null;
         throw new CertificateException("Unable to initialize, " + var3, var3);
      }
   }

   public X509CertImpl(InputStream var1) throws CertificateException {
      DerValue var2 = null;
      BufferedInputStream var3 = new BufferedInputStream(var1);

      try {
         var3.mark(Integer.MAX_VALUE);
         var2 = this.readRFC1421Cert(var3);
      } catch (IOException var8) {
         try {
            var3.reset();
            var2 = new DerValue(var3);
         } catch (IOException var7) {
            throw new CertificateException("Input stream must be either DER-encoded bytes or RFC1421 hex-encoded DER-encoded bytes: " + var7.getMessage(), var7);
         }
      }

      try {
         this.parse(var2);
      } catch (IOException var6) {
         this.signedCert = null;
         throw new CertificateException("Unable to parse DER value of certificate, " + var6, var6);
      }
   }

   private DerValue readRFC1421Cert(InputStream var1) throws IOException {
      DerValue var2 = null;
      String var3 = null;
      BufferedReader var4 = new BufferedReader(new InputStreamReader(var1, "ASCII"));

      try {
         var3 = var4.readLine();
      } catch (IOException var7) {
         throw new IOException("Unable to read InputStream: " + var7.getMessage());
      }

      if (!var3.equals("-----BEGIN CERTIFICATE-----")) {
         throw new IOException("InputStream is not RFC1421 hex-encoded DER bytes");
      } else {
         ByteArrayOutputStream var5 = new ByteArrayOutputStream();

         try {
            while((var3 = var4.readLine()) != null) {
               if (var3.equals("-----END CERTIFICATE-----")) {
                  var2 = new DerValue(var5.toByteArray());
                  break;
               }

               var5.write(Pem.decode(var3));
            }

            return var2;
         } catch (IOException var8) {
            throw new IOException("Unable to read InputStream: " + var8.getMessage());
         }
      }
   }

   public X509CertImpl(X509CertInfo var1) {
      this.info = var1;
   }

   public X509CertImpl(DerValue var1) throws CertificateException {
      try {
         this.parse(var1);
      } catch (IOException var3) {
         this.signedCert = null;
         throw new CertificateException("Unable to initialize, " + var3, var3);
      }
   }

   public void encode(OutputStream var1) throws CertificateEncodingException {
      if (this.signedCert == null) {
         throw new CertificateEncodingException("Null certificate to encode");
      } else {
         try {
            var1.write((byte[])this.signedCert.clone());
         } catch (IOException var3) {
            throw new CertificateEncodingException(var3.toString());
         }
      }
   }

   public void derEncode(OutputStream var1) throws IOException {
      if (this.signedCert == null) {
         throw new IOException("Null certificate to encode");
      } else {
         var1.write((byte[])this.signedCert.clone());
      }
   }

   public byte[] getEncoded() throws CertificateEncodingException {
      return (byte[])this.getEncodedInternal().clone();
   }

   public byte[] getEncodedInternal() throws CertificateEncodingException {
      if (this.signedCert == null) {
         throw new CertificateEncodingException("Null certificate to encode");
      } else {
         return this.signedCert;
      }
   }

   public void verify(PublicKey var1) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
      this.verify(var1, "");
   }

   public synchronized void verify(PublicKey var1, String var2) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
      if (var2 == null) {
         var2 = "";
      }

      if (this.verifiedPublicKey != null && this.verifiedPublicKey.equals(var1) && var2.equals(this.verifiedProvider)) {
         if (!this.verificationResult) {
            throw new SignatureException("Signature does not match.");
         }
      } else if (this.signedCert == null) {
         throw new CertificateEncodingException("Uninitialized certificate");
      } else {
         Signature var3 = null;
         if (var2.length() == 0) {
            var3 = Signature.getInstance(this.algId.getName());
         } else {
            var3 = Signature.getInstance(this.algId.getName(), var2);
         }

         var3.initVerify(var1);
         byte[] var4 = this.info.getEncodedInfo();
         var3.update(var4, 0, var4.length);
         this.verificationResult = var3.verify(this.signature);
         this.verifiedPublicKey = var1;
         this.verifiedProvider = var2;
         if (!this.verificationResult) {
            throw new SignatureException("Signature does not match.");
         }
      }
   }

   public synchronized void verify(PublicKey var1, Provider var2) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
      if (this.signedCert == null) {
         throw new CertificateEncodingException("Uninitialized certificate");
      } else {
         Signature var3 = null;
         if (var2 == null) {
            var3 = Signature.getInstance(this.algId.getName());
         } else {
            var3 = Signature.getInstance(this.algId.getName(), var2);
         }

         var3.initVerify(var1);
         byte[] var4 = this.info.getEncodedInfo();
         var3.update(var4, 0, var4.length);
         this.verificationResult = var3.verify(this.signature);
         this.verifiedPublicKey = var1;
         if (!this.verificationResult) {
            throw new SignatureException("Signature does not match.");
         }
      }
   }

   public static void verify(X509Certificate var0, PublicKey var1, Provider var2) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
      var0.verify(var1, var2);
   }

   public void sign(PrivateKey var1, String var2) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
      this.sign(var1, var2, (String)null);
   }

   public void sign(PrivateKey var1, String var2, String var3) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
      try {
         if (this.readOnly) {
            throw new CertificateEncodingException("cannot over-write existing certificate");
         } else {
            Signature var4 = null;
            if (var3 != null && var3.length() != 0) {
               var4 = Signature.getInstance(var2, var3);
            } else {
               var4 = Signature.getInstance(var2);
            }

            var4.initSign(var1);
            this.algId = AlgorithmId.get(var4.getAlgorithm());
            DerOutputStream var5 = new DerOutputStream();
            DerOutputStream var6 = new DerOutputStream();
            this.info.encode(var6);
            byte[] var7 = var6.toByteArray();
            this.algId.encode(var6);
            var4.update(var7, 0, var7.length);
            this.signature = var4.sign();
            var6.putBitString(this.signature);
            var5.write((byte)48, (DerOutputStream)var6);
            this.signedCert = var5.toByteArray();
            this.readOnly = true;
         }
      } catch (IOException var8) {
         throw new CertificateEncodingException(var8.toString());
      }
   }

   public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
      Date var1 = new Date();
      this.checkValidity(var1);
   }

   public void checkValidity(Date var1) throws CertificateExpiredException, CertificateNotYetValidException {
      CertificateValidity var2 = null;

      try {
         var2 = (CertificateValidity)this.info.get("validity");
      } catch (Exception var4) {
         throw new CertificateNotYetValidException("Incorrect validity period");
      }

      if (var2 == null) {
         throw new CertificateNotYetValidException("Null validity period");
      } else {
         var2.valid(var1);
      }
   }

   public Object get(String var1) throws CertificateParsingException {
      X509AttributeName var2 = new X509AttributeName(var1);
      String var3 = var2.getPrefix();
      if (!var3.equalsIgnoreCase("x509")) {
         throw new CertificateParsingException("Invalid root of attribute name, expected [x509], received [" + var3 + "]");
      } else {
         var2 = new X509AttributeName(var2.getSuffix());
         var3 = var2.getPrefix();
         if (var3.equalsIgnoreCase("info")) {
            if (this.info == null) {
               return null;
            } else if (var2.getSuffix() != null) {
               try {
                  return this.info.get(var2.getSuffix());
               } catch (IOException var5) {
                  throw new CertificateParsingException(var5.toString());
               } catch (CertificateException var6) {
                  throw new CertificateParsingException(var6.toString());
               }
            } else {
               return this.info;
            }
         } else if (var3.equalsIgnoreCase("algorithm")) {
            return this.algId;
         } else if (var3.equalsIgnoreCase("signature")) {
            return this.signature != null ? this.signature.clone() : null;
         } else if (var3.equalsIgnoreCase("signed_cert")) {
            return this.signedCert != null ? this.signedCert.clone() : null;
         } else {
            throw new CertificateParsingException("Attribute name not recognized or get() not allowed for the same: " + var3);
         }
      }
   }

   public void set(String var1, Object var2) throws CertificateException, IOException {
      if (this.readOnly) {
         throw new CertificateException("cannot over-write existing certificate");
      } else {
         X509AttributeName var3 = new X509AttributeName(var1);
         String var4 = var3.getPrefix();
         if (!var4.equalsIgnoreCase("x509")) {
            throw new CertificateException("Invalid root of attribute name, expected [x509], received " + var4);
         } else {
            var3 = new X509AttributeName(var3.getSuffix());
            var4 = var3.getPrefix();
            if (var4.equalsIgnoreCase("info")) {
               if (var3.getSuffix() == null) {
                  if (!(var2 instanceof X509CertInfo)) {
                     throw new CertificateException("Attribute value should be of type X509CertInfo.");
                  }

                  this.info = (X509CertInfo)var2;
                  this.signedCert = null;
               } else {
                  this.info.set(var3.getSuffix(), var2);
                  this.signedCert = null;
               }

            } else {
               throw new CertificateException("Attribute name not recognized or set() not allowed for the same: " + var4);
            }
         }
      }
   }

   public void delete(String var1) throws CertificateException, IOException {
      if (this.readOnly) {
         throw new CertificateException("cannot over-write existing certificate");
      } else {
         X509AttributeName var2 = new X509AttributeName(var1);
         String var3 = var2.getPrefix();
         if (!var3.equalsIgnoreCase("x509")) {
            throw new CertificateException("Invalid root of attribute name, expected [x509], received " + var3);
         } else {
            var2 = new X509AttributeName(var2.getSuffix());
            var3 = var2.getPrefix();
            if (var3.equalsIgnoreCase("info")) {
               if (var2.getSuffix() != null) {
                  this.info = null;
               } else {
                  this.info.delete(var2.getSuffix());
               }
            } else if (var3.equalsIgnoreCase("algorithm")) {
               this.algId = null;
            } else if (var3.equalsIgnoreCase("signature")) {
               this.signature = null;
            } else {
               if (!var3.equalsIgnoreCase("signed_cert")) {
                  throw new CertificateException("Attribute name not recognized or delete() not allowed for the same: " + var3);
               }

               this.signedCert = null;
            }

         }
      }
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("x509.info");
      var1.addElement("x509.algorithm");
      var1.addElement("x509.signature");
      var1.addElement("x509.signed_cert");
      return var1.elements();
   }

   public String getName() {
      return "x509";
   }

   public String toString() {
      if (this.info != null && this.algId != null && this.signature != null) {
         StringBuilder var1 = new StringBuilder();
         var1.append("[\n");
         var1.append(this.info.toString() + "\n");
         var1.append("  Algorithm: [" + this.algId.toString() + "]\n");
         HexDumpEncoder var2 = new HexDumpEncoder();
         var1.append("  Signature:\n" + var2.encodeBuffer(this.signature));
         var1.append("\n]");
         return var1.toString();
      } else {
         return "";
      }
   }

   public PublicKey getPublicKey() {
      if (this.info == null) {
         return null;
      } else {
         try {
            PublicKey var1 = (PublicKey)this.info.get("key.value");
            return var1;
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public int getVersion() {
      if (this.info == null) {
         return -1;
      } else {
         try {
            int var1 = (Integer)this.info.get("version.number");
            return var1 + 1;
         } catch (Exception var2) {
            return -1;
         }
      }
   }

   public BigInteger getSerialNumber() {
      SerialNumber var1 = this.getSerialNumberObject();
      return var1 != null ? var1.getNumber() : null;
   }

   public SerialNumber getSerialNumberObject() {
      if (this.info == null) {
         return null;
      } else {
         try {
            SerialNumber var1 = (SerialNumber)this.info.get("serialNumber.number");
            return var1;
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public Principal getSubjectDN() {
      if (this.info == null) {
         return null;
      } else {
         try {
            Principal var1 = (Principal)this.info.get("subject.dname");
            return var1;
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public X500Principal getSubjectX500Principal() {
      if (this.info == null) {
         return null;
      } else {
         try {
            X500Principal var1 = (X500Principal)this.info.get("subject.x500principal");
            return var1;
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public Principal getIssuerDN() {
      if (this.info == null) {
         return null;
      } else {
         try {
            Principal var1 = (Principal)this.info.get("issuer.dname");
            return var1;
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public X500Principal getIssuerX500Principal() {
      if (this.info == null) {
         return null;
      } else {
         try {
            X500Principal var1 = (X500Principal)this.info.get("issuer.x500principal");
            return var1;
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public Date getNotBefore() {
      if (this.info == null) {
         return null;
      } else {
         try {
            Date var1 = (Date)this.info.get("validity.notBefore");
            return var1;
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public Date getNotAfter() {
      if (this.info == null) {
         return null;
      } else {
         try {
            Date var1 = (Date)this.info.get("validity.notAfter");
            return var1;
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public byte[] getTBSCertificate() throws CertificateEncodingException {
      if (this.info != null) {
         return this.info.getEncodedInfo();
      } else {
         throw new CertificateEncodingException("Uninitialized certificate");
      }
   }

   public byte[] getSignature() {
      return this.signature == null ? null : (byte[])this.signature.clone();
   }

   public String getSigAlgName() {
      return this.algId == null ? null : this.algId.getName();
   }

   public String getSigAlgOID() {
      if (this.algId == null) {
         return null;
      } else {
         ObjectIdentifier var1 = this.algId.getOID();
         return var1.toString();
      }
   }

   public byte[] getSigAlgParams() {
      if (this.algId == null) {
         return null;
      } else {
         try {
            return this.algId.getEncodedParams();
         } catch (IOException var2) {
            return null;
         }
      }
   }

   public boolean[] getIssuerUniqueID() {
      if (this.info == null) {
         return null;
      } else {
         try {
            UniqueIdentity var1 = (UniqueIdentity)this.info.get("issuerID");
            return var1 == null ? null : var1.getId();
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public boolean[] getSubjectUniqueID() {
      if (this.info == null) {
         return null;
      } else {
         try {
            UniqueIdentity var1 = (UniqueIdentity)this.info.get("subjectID");
            return var1 == null ? null : var1.getId();
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public KeyIdentifier getAuthKeyId() {
      AuthorityKeyIdentifierExtension var1 = this.getAuthorityKeyIdentifierExtension();
      if (var1 != null) {
         try {
            return (KeyIdentifier)var1.get("key_id");
         } catch (IOException var3) {
         }
      }

      return null;
   }

   public KeyIdentifier getSubjectKeyId() {
      SubjectKeyIdentifierExtension var1 = this.getSubjectKeyIdentifierExtension();
      if (var1 != null) {
         try {
            return var1.get("key_id");
         } catch (IOException var3) {
         }
      }

      return null;
   }

   public AuthorityKeyIdentifierExtension getAuthorityKeyIdentifierExtension() {
      return (AuthorityKeyIdentifierExtension)this.getExtension(PKIXExtensions.AuthorityKey_Id);
   }

   public BasicConstraintsExtension getBasicConstraintsExtension() {
      return (BasicConstraintsExtension)this.getExtension(PKIXExtensions.BasicConstraints_Id);
   }

   public CertificatePoliciesExtension getCertificatePoliciesExtension() {
      return (CertificatePoliciesExtension)this.getExtension(PKIXExtensions.CertificatePolicies_Id);
   }

   public ExtendedKeyUsageExtension getExtendedKeyUsageExtension() {
      return (ExtendedKeyUsageExtension)this.getExtension(PKIXExtensions.ExtendedKeyUsage_Id);
   }

   public IssuerAlternativeNameExtension getIssuerAlternativeNameExtension() {
      return (IssuerAlternativeNameExtension)this.getExtension(PKIXExtensions.IssuerAlternativeName_Id);
   }

   public NameConstraintsExtension getNameConstraintsExtension() {
      return (NameConstraintsExtension)this.getExtension(PKIXExtensions.NameConstraints_Id);
   }

   public PolicyConstraintsExtension getPolicyConstraintsExtension() {
      return (PolicyConstraintsExtension)this.getExtension(PKIXExtensions.PolicyConstraints_Id);
   }

   public PolicyMappingsExtension getPolicyMappingsExtension() {
      return (PolicyMappingsExtension)this.getExtension(PKIXExtensions.PolicyMappings_Id);
   }

   public PrivateKeyUsageExtension getPrivateKeyUsageExtension() {
      return (PrivateKeyUsageExtension)this.getExtension(PKIXExtensions.PrivateKeyUsage_Id);
   }

   public SubjectAlternativeNameExtension getSubjectAlternativeNameExtension() {
      return (SubjectAlternativeNameExtension)this.getExtension(PKIXExtensions.SubjectAlternativeName_Id);
   }

   public SubjectKeyIdentifierExtension getSubjectKeyIdentifierExtension() {
      return (SubjectKeyIdentifierExtension)this.getExtension(PKIXExtensions.SubjectKey_Id);
   }

   public CRLDistributionPointsExtension getCRLDistributionPointsExtension() {
      return (CRLDistributionPointsExtension)this.getExtension(PKIXExtensions.CRLDistributionPoints_Id);
   }

   public boolean hasUnsupportedCriticalExtension() {
      if (this.info == null) {
         return false;
      } else {
         try {
            CertificateExtensions var1 = (CertificateExtensions)this.info.get("extensions");
            return var1 == null ? false : var1.hasUnsupportedCriticalExtension();
         } catch (Exception var2) {
            return false;
         }
      }
   }

   public Set<String> getCriticalExtensionOIDs() {
      if (this.info == null) {
         return null;
      } else {
         try {
            CertificateExtensions var1 = (CertificateExtensions)this.info.get("extensions");
            if (var1 == null) {
               return null;
            } else {
               TreeSet var2 = new TreeSet();
               Iterator var3 = var1.getAllExtensions().iterator();

               while(var3.hasNext()) {
                  Extension var4 = (Extension)var3.next();
                  if (var4.isCritical()) {
                     var2.add(var4.getExtensionId().toString());
                  }
               }

               return var2;
            }
         } catch (Exception var5) {
            return null;
         }
      }
   }

   public Set<String> getNonCriticalExtensionOIDs() {
      if (this.info == null) {
         return null;
      } else {
         try {
            CertificateExtensions var1 = (CertificateExtensions)this.info.get("extensions");
            if (var1 == null) {
               return null;
            } else {
               TreeSet var2 = new TreeSet();
               Iterator var3 = var1.getAllExtensions().iterator();

               while(var3.hasNext()) {
                  Extension var4 = (Extension)var3.next();
                  if (!var4.isCritical()) {
                     var2.add(var4.getExtensionId().toString());
                  }
               }

               var2.addAll(var1.getUnparseableExtensions().keySet());
               return var2;
            }
         } catch (Exception var5) {
            return null;
         }
      }
   }

   public Extension getExtension(ObjectIdentifier var1) {
      if (this.info == null) {
         return null;
      } else {
         try {
            CertificateExtensions var2;
            try {
               var2 = (CertificateExtensions)this.info.get("extensions");
            } catch (CertificateException var6) {
               return null;
            }

            if (var2 == null) {
               return null;
            } else {
               Extension var3 = var2.getExtension(var1.toString());
               if (var3 != null) {
                  return var3;
               } else {
                  Iterator var4 = var2.getAllExtensions().iterator();

                  Extension var5;
                  do {
                     if (!var4.hasNext()) {
                        return null;
                     }

                     var5 = (Extension)var4.next();
                  } while(!var5.getExtensionId().equals((Object)var1));

                  return var5;
               }
            }
         } catch (IOException var7) {
            return null;
         }
      }
   }

   public Extension getUnparseableExtension(ObjectIdentifier var1) {
      if (this.info == null) {
         return null;
      } else {
         try {
            CertificateExtensions var2;
            try {
               var2 = (CertificateExtensions)this.info.get("extensions");
            } catch (CertificateException var4) {
               return null;
            }

            return var2 == null ? null : (Extension)var2.getUnparseableExtensions().get(var1.toString());
         } catch (IOException var5) {
            return null;
         }
      }
   }

   public byte[] getExtensionValue(String var1) {
      try {
         ObjectIdentifier var2 = new ObjectIdentifier(var1);
         String var3 = OIDMap.getName(var2);
         Extension var4 = null;
         CertificateExtensions var5 = (CertificateExtensions)this.info.get("extensions");
         if (var3 == null) {
            if (var5 == null) {
               return null;
            }

            Iterator var6 = var5.getAllExtensions().iterator();

            while(var6.hasNext()) {
               Extension var7 = (Extension)var6.next();
               ObjectIdentifier var8 = var7.getExtensionId();
               if (var8.equals((Object)var2)) {
                  var4 = var7;
                  break;
               }
            }
         } else {
            try {
               var4 = (Extension)this.get(var3);
            } catch (CertificateException var9) {
            }
         }

         if (var4 == null) {
            if (var5 != null) {
               var4 = (Extension)var5.getUnparseableExtensions().get(var1);
            }

            if (var4 == null) {
               return null;
            }
         }

         byte[] var11 = var4.getExtensionValue();
         if (var11 == null) {
            return null;
         } else {
            DerOutputStream var12 = new DerOutputStream();
            var12.putOctetString(var11);
            return var12.toByteArray();
         }
      } catch (Exception var10) {
         return null;
      }
   }

   public boolean[] getKeyUsage() {
      try {
         String var1 = OIDMap.getName(PKIXExtensions.KeyUsage_Id);
         if (var1 == null) {
            return null;
         } else {
            KeyUsageExtension var2 = (KeyUsageExtension)this.get(var1);
            if (var2 == null) {
               return null;
            } else {
               boolean[] var3 = var2.getBits();
               if (var3.length < 9) {
                  boolean[] var4 = new boolean[9];
                  System.arraycopy(var3, 0, var4, 0, var3.length);
                  var3 = var4;
               }

               return var3;
            }
         }
      } catch (Exception var5) {
         return null;
      }
   }

   public synchronized List<String> getExtendedKeyUsage() throws CertificateParsingException {
      if (this.readOnly && this.extKeyUsage != null) {
         return this.extKeyUsage;
      } else {
         ExtendedKeyUsageExtension var1 = this.getExtendedKeyUsageExtension();
         if (var1 == null) {
            return null;
         } else {
            this.extKeyUsage = Collections.unmodifiableList(var1.getExtendedKeyUsage());
            return this.extKeyUsage;
         }
      }
   }

   public static List<String> getExtendedKeyUsage(X509Certificate var0) throws CertificateParsingException {
      try {
         byte[] var1 = var0.getExtensionValue("2.5.29.37");
         if (var1 == null) {
            return null;
         } else {
            DerValue var2 = new DerValue(var1);
            byte[] var3 = var2.getOctetString();
            ExtendedKeyUsageExtension var4 = new ExtendedKeyUsageExtension(Boolean.FALSE, var3);
            return Collections.unmodifiableList(var4.getExtendedKeyUsage());
         }
      } catch (IOException var5) {
         throw new CertificateParsingException(var5);
      }
   }

   public int getBasicConstraints() {
      try {
         String var1 = OIDMap.getName(PKIXExtensions.BasicConstraints_Id);
         if (var1 == null) {
            return -1;
         } else {
            BasicConstraintsExtension var2 = (BasicConstraintsExtension)this.get(var1);
            if (var2 == null) {
               return -1;
            } else {
               return (Boolean)var2.get("is_ca") ? (Integer)var2.get("path_len") : -1;
            }
         }
      } catch (Exception var3) {
         return -1;
      }
   }

   private static Collection<List<?>> makeAltNames(GeneralNames var0) {
      if (var0.isEmpty()) {
         return Collections.emptySet();
      } else {
         ArrayList var1 = new ArrayList();

         ArrayList var5;
         for(Iterator var2 = var0.names().iterator(); var2.hasNext(); var1.add(Collections.unmodifiableList(var5))) {
            GeneralName var3 = (GeneralName)var2.next();
            GeneralNameInterface var4 = var3.getName();
            var5 = new ArrayList(2);
            var5.add(var4.getType());
            switch(var4.getType()) {
            case 1:
               var5.add(((RFC822Name)var4).getName());
               break;
            case 2:
               var5.add(((DNSName)var4).getName());
               break;
            case 3:
            case 5:
            default:
               DerOutputStream var6 = new DerOutputStream();

               try {
                  var4.encode(var6);
               } catch (IOException var8) {
                  throw new RuntimeException("name cannot be encoded", var8);
               }

               var5.add(var6.toByteArray());
               break;
            case 4:
               var5.add(((X500Name)var4).getRFC2253Name());
               break;
            case 6:
               var5.add(((URIName)var4).getName());
               break;
            case 7:
               try {
                  var5.add(((IPAddressName)var4).getName());
                  break;
               } catch (IOException var9) {
                  throw new RuntimeException("IPAddress cannot be parsed", var9);
               }
            case 8:
               var5.add(((OIDName)var4).getOID().toString());
            }
         }

         return Collections.unmodifiableCollection(var1);
      }
   }

   private static Collection<List<?>> cloneAltNames(Collection<List<?>> var0) {
      boolean var1 = false;
      Iterator var2 = var0.iterator();

      while(var2.hasNext()) {
         List var3 = (List)var2.next();
         if (var3.get(1) instanceof byte[]) {
            var1 = true;
         }
      }

      if (var1) {
         ArrayList var7 = new ArrayList();
         Iterator var8 = var0.iterator();

         while(var8.hasNext()) {
            List var4 = (List)var8.next();
            Object var5 = var4.get(1);
            if (var5 instanceof byte[]) {
               ArrayList var6 = new ArrayList(var4);
               var6.set(1, ((byte[])((byte[])var5)).clone());
               var7.add(Collections.unmodifiableList(var6));
            } else {
               var7.add(var4);
            }
         }

         return Collections.unmodifiableCollection(var7);
      } else {
         return var0;
      }
   }

   public synchronized Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
      if (this.readOnly && this.subjectAlternativeNames != null) {
         return cloneAltNames(this.subjectAlternativeNames);
      } else {
         SubjectAlternativeNameExtension var1 = this.getSubjectAlternativeNameExtension();
         if (var1 == null) {
            return null;
         } else {
            GeneralNames var2;
            try {
               var2 = var1.get("subject_name");
            } catch (IOException var4) {
               return Collections.emptySet();
            }

            this.subjectAlternativeNames = makeAltNames(var2);
            return this.subjectAlternativeNames;
         }
      }
   }

   public static Collection<List<?>> getSubjectAlternativeNames(X509Certificate var0) throws CertificateParsingException {
      try {
         byte[] var1 = var0.getExtensionValue("2.5.29.17");
         if (var1 == null) {
            return null;
         } else {
            DerValue var2 = new DerValue(var1);
            byte[] var3 = var2.getOctetString();
            SubjectAlternativeNameExtension var4 = new SubjectAlternativeNameExtension(Boolean.FALSE, var3);

            GeneralNames var5;
            try {
               var5 = var4.get("subject_name");
            } catch (IOException var7) {
               return Collections.emptySet();
            }

            return makeAltNames(var5);
         }
      } catch (IOException var8) {
         throw new CertificateParsingException(var8);
      }
   }

   public synchronized Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
      if (this.readOnly && this.issuerAlternativeNames != null) {
         return cloneAltNames(this.issuerAlternativeNames);
      } else {
         IssuerAlternativeNameExtension var1 = this.getIssuerAlternativeNameExtension();
         if (var1 == null) {
            return null;
         } else {
            GeneralNames var2;
            try {
               var2 = var1.get("issuer_name");
            } catch (IOException var4) {
               return Collections.emptySet();
            }

            this.issuerAlternativeNames = makeAltNames(var2);
            return this.issuerAlternativeNames;
         }
      }
   }

   public static Collection<List<?>> getIssuerAlternativeNames(X509Certificate var0) throws CertificateParsingException {
      try {
         byte[] var1 = var0.getExtensionValue("2.5.29.18");
         if (var1 == null) {
            return null;
         } else {
            DerValue var2 = new DerValue(var1);
            byte[] var3 = var2.getOctetString();
            IssuerAlternativeNameExtension var4 = new IssuerAlternativeNameExtension(Boolean.FALSE, var3);

            GeneralNames var5;
            try {
               var5 = var4.get("issuer_name");
            } catch (IOException var7) {
               return Collections.emptySet();
            }

            return makeAltNames(var5);
         }
      } catch (IOException var8) {
         throw new CertificateParsingException(var8);
      }
   }

   public AuthorityInfoAccessExtension getAuthorityInfoAccessExtension() {
      return (AuthorityInfoAccessExtension)this.getExtension(PKIXExtensions.AuthInfoAccess_Id);
   }

   private void parse(DerValue var1) throws CertificateException, IOException {
      if (this.readOnly) {
         throw new CertificateParsingException("cannot over-write existing certificate");
      } else if (var1.data != null && var1.tag == 48) {
         this.signedCert = var1.toByteArray();
         DerValue[] var2 = new DerValue[]{var1.data.getDerValue(), var1.data.getDerValue(), var1.data.getDerValue()};
         if (var1.data.available() != 0) {
            throw new CertificateParsingException("signed overrun, bytes = " + var1.data.available());
         } else if (var2[0].tag != 48) {
            throw new CertificateParsingException("signed fields invalid");
         } else {
            this.algId = AlgorithmId.parse(var2[1]);
            this.signature = var2[2].getBitString();
            if (var2[1].data.available() != 0) {
               throw new CertificateParsingException("algid field overrun");
            } else if (var2[2].data.available() != 0) {
               throw new CertificateParsingException("signed fields overrun");
            } else {
               this.info = new X509CertInfo(var2[0]);
               AlgorithmId var3 = (AlgorithmId)this.info.get("algorithmID.algorithm");
               if (!this.algId.equals(var3)) {
                  throw new CertificateException("Signature algorithm mismatch");
               } else {
                  this.readOnly = true;
               }
            }
         }
      } else {
         throw new CertificateParsingException("invalid DER-encoded certificate data");
      }
   }

   private static X500Principal getX500Principal(X509Certificate var0, boolean var1) throws Exception {
      byte[] var2 = var0.getEncoded();
      DerInputStream var3 = new DerInputStream(var2);
      DerValue var4 = var3.getSequence(3)[0];
      DerInputStream var5 = var4.data;
      DerValue var6 = var5.getDerValue();
      if (var6.isContextSpecific((byte)0)) {
         var6 = var5.getDerValue();
      }

      var6 = var5.getDerValue();
      var6 = var5.getDerValue();
      if (!var1) {
         var6 = var5.getDerValue();
         var6 = var5.getDerValue();
      }

      byte[] var7 = var6.toByteArray();
      return new X500Principal(var7);
   }

   public static X500Principal getSubjectX500Principal(X509Certificate var0) {
      try {
         return getX500Principal(var0, false);
      } catch (Exception var2) {
         throw new RuntimeException("Could not parse subject", var2);
      }
   }

   public static X500Principal getIssuerX500Principal(X509Certificate var0) {
      try {
         return getX500Principal(var0, true);
      } catch (Exception var2) {
         throw new RuntimeException("Could not parse issuer", var2);
      }
   }

   public static byte[] getEncodedInternal(Certificate var0) throws CertificateEncodingException {
      return var0 instanceof X509CertImpl ? ((X509CertImpl)var0).getEncodedInternal() : var0.getEncoded();
   }

   public static X509CertImpl toImpl(X509Certificate var0) throws CertificateException {
      return var0 instanceof X509CertImpl ? (X509CertImpl)var0 : X509Factory.intern(var0);
   }

   public static boolean isSelfIssued(X509Certificate var0) {
      X500Principal var1 = var0.getSubjectX500Principal();
      X500Principal var2 = var0.getIssuerX500Principal();
      return var1.equals(var2);
   }

   public static boolean isSelfSigned(X509Certificate var0, String var1) {
      if (isSelfIssued(var0)) {
         try {
            if (var1 == null) {
               var0.verify(var0.getPublicKey());
            } else {
               var0.verify(var0.getPublicKey(), var1);
            }

            return true;
         } catch (Exception var3) {
         }
      }

      return false;
   }

   public String getFingerprint(String var1) {
      return (String)this.fingerprints.computeIfAbsent(var1, (var1x) -> {
         return getFingerprint(var1x, this);
      });
   }

   public static String getFingerprint(String var0, X509Certificate var1) {
      String var2 = "";

      try {
         byte[] var3 = var1.getEncoded();
         MessageDigest var4 = MessageDigest.getInstance(var0);
         byte[] var5 = var4.digest(var3);
         StringBuffer var6 = new StringBuffer();

         for(int var7 = 0; var7 < var5.length; ++var7) {
            byte2hex(var5[var7], var6);
         }

         var2 = var6.toString();
      } catch (CertificateEncodingException | NoSuchAlgorithmException var8) {
      }

      return var2;
   }

   private static void byte2hex(byte var0, StringBuffer var1) {
      char[] var2 = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
      int var3 = (var0 & 240) >> 4;
      int var4 = var0 & 15;
      var1.append(var2[var3]);
      var1.append(var2[var4]);
   }
}
