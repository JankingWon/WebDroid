package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import sun.misc.HexDumpEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class X509CertInfo implements CertAttrSet<String> {
   public static final String IDENT = "x509.info";
   public static final String NAME = "info";
   public static final String DN_NAME = "dname";
   public static final String VERSION = "version";
   public static final String SERIAL_NUMBER = "serialNumber";
   public static final String ALGORITHM_ID = "algorithmID";
   public static final String ISSUER = "issuer";
   public static final String SUBJECT = "subject";
   public static final String VALIDITY = "validity";
   public static final String KEY = "key";
   public static final String ISSUER_ID = "issuerID";
   public static final String SUBJECT_ID = "subjectID";
   public static final String EXTENSIONS = "extensions";
   protected CertificateVersion version = new CertificateVersion();
   protected CertificateSerialNumber serialNum = null;
   protected CertificateAlgorithmId algId = null;
   protected X500Name issuer = null;
   protected X500Name subject = null;
   protected CertificateValidity interval = null;
   protected CertificateX509Key pubKey = null;
   protected UniqueIdentity issuerUniqueId = null;
   protected UniqueIdentity subjectUniqueId = null;
   protected CertificateExtensions extensions = null;
   private static final int ATTR_VERSION = 1;
   private static final int ATTR_SERIAL = 2;
   private static final int ATTR_ALGORITHM = 3;
   private static final int ATTR_ISSUER = 4;
   private static final int ATTR_VALIDITY = 5;
   private static final int ATTR_SUBJECT = 6;
   private static final int ATTR_KEY = 7;
   private static final int ATTR_ISSUER_ID = 8;
   private static final int ATTR_SUBJECT_ID = 9;
   private static final int ATTR_EXTENSIONS = 10;
   private byte[] rawCertInfo = null;
   private static final Map<String, Integer> map = new HashMap();

   public X509CertInfo() {
   }

   public X509CertInfo(byte[] var1) throws CertificateParsingException {
      try {
         DerValue var2 = new DerValue(var1);
         this.parse(var2);
      } catch (IOException var3) {
         throw new CertificateParsingException(var3);
      }
   }

   public X509CertInfo(DerValue var1) throws CertificateParsingException {
      try {
         this.parse(var1);
      } catch (IOException var3) {
         throw new CertificateParsingException(var3);
      }
   }

   public void encode(OutputStream var1) throws CertificateException, IOException {
      if (this.rawCertInfo == null) {
         DerOutputStream var2 = new DerOutputStream();
         this.emit(var2);
         this.rawCertInfo = var2.toByteArray();
      }

      var1.write((byte[])this.rawCertInfo.clone());
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("version");
      var1.addElement("serialNumber");
      var1.addElement("algorithmID");
      var1.addElement("issuer");
      var1.addElement("validity");
      var1.addElement("subject");
      var1.addElement("key");
      var1.addElement("issuerID");
      var1.addElement("subjectID");
      var1.addElement("extensions");
      return var1.elements();
   }

   public String getName() {
      return "info";
   }

   public byte[] getEncodedInfo() throws CertificateEncodingException {
      try {
         if (this.rawCertInfo == null) {
            DerOutputStream var1 = new DerOutputStream();
            this.emit(var1);
            this.rawCertInfo = var1.toByteArray();
         }

         return (byte[])this.rawCertInfo.clone();
      } catch (IOException var2) {
         throw new CertificateEncodingException(var2.toString());
      } catch (CertificateException var3) {
         throw new CertificateEncodingException(var3.toString());
      }
   }

   public boolean equals(Object var1) {
      return var1 instanceof X509CertInfo ? this.equals((X509CertInfo)var1) : false;
   }

   public boolean equals(X509CertInfo var1) {
      if (this == var1) {
         return true;
      } else if (this.rawCertInfo != null && var1.rawCertInfo != null) {
         if (this.rawCertInfo.length != var1.rawCertInfo.length) {
            return false;
         } else {
            for(int var2 = 0; var2 < this.rawCertInfo.length; ++var2) {
               if (this.rawCertInfo[var2] != var1.rawCertInfo[var2]) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int var1 = 0;

      for(int var2 = 1; var2 < this.rawCertInfo.length; ++var2) {
         var1 += this.rawCertInfo[var2] * var2;
      }

      return var1;
   }

   public String toString() {
      if (this.subject != null && this.pubKey != null && this.interval != null && this.issuer != null && this.algId != null && this.serialNum != null) {
         StringBuilder var1 = new StringBuilder();
         var1.append("[\n");
         var1.append("  " + this.version.toString() + "\n");
         var1.append("  Subject: " + this.subject.toString() + "\n");
         var1.append("  Signature Algorithm: " + this.algId.toString() + "\n");
         var1.append("  Key:  " + this.pubKey.toString() + "\n");
         var1.append("  " + this.interval.toString() + "\n");
         var1.append("  Issuer: " + this.issuer.toString() + "\n");
         var1.append("  " + this.serialNum.toString() + "\n");
         if (this.issuerUniqueId != null) {
            var1.append("  Issuer Id:\n" + this.issuerUniqueId.toString() + "\n");
         }

         if (this.subjectUniqueId != null) {
            var1.append("  Subject Id:\n" + this.subjectUniqueId.toString() + "\n");
         }

         if (this.extensions != null) {
            Collection var2 = this.extensions.getAllExtensions();
            Extension[] var3 = (Extension[])var2.toArray(new Extension[0]);
            var1.append("\nCertificate Extensions: " + var3.length);

            for(int var4 = 0; var4 < var3.length; ++var4) {
               var1.append("\n[" + (var4 + 1) + "]: ");
               Extension var5 = var3[var4];

               try {
                  if (OIDMap.getClass(var5.getExtensionId()) == null) {
                     var1.append(var5.toString());
                     byte[] var6 = var5.getExtensionValue();
                     if (var6 != null) {
                        DerOutputStream var7 = new DerOutputStream();
                        var7.putOctetString(var6);
                        var6 = var7.toByteArray();
                        HexDumpEncoder var8 = new HexDumpEncoder();
                        var1.append("Extension unknown: DER encoded OCTET string =\n" + var8.encodeBuffer(var6) + "\n");
                     }
                  } else {
                     var1.append(var5.toString());
                  }
               } catch (Exception var9) {
                  var1.append(", Error parsing this extension");
               }
            }

            Map var10 = this.extensions.getUnparseableExtensions();
            if (!var10.isEmpty()) {
               var1.append("\nUnparseable certificate extensions: " + var10.size());
               int var11 = 1;
               Iterator var12 = var10.values().iterator();

               while(var12.hasNext()) {
                  Extension var13 = (Extension)var12.next();
                  var1.append("\n[" + var11++ + "]: ");
                  var1.append((Object)var13);
               }
            }
         }

         var1.append("\n]");
         return var1.toString();
      } else {
         throw new NullPointerException("X.509 cert is incomplete");
      }
   }

   public void set(String var1, Object var2) throws CertificateException, IOException {
      X509AttributeName var3 = new X509AttributeName(var1);
      int var4 = this.attributeMap(var3.getPrefix());
      if (var4 == 0) {
         throw new CertificateException("Attribute name not recognized: " + var1);
      } else {
         this.rawCertInfo = null;
         String var5 = var3.getSuffix();
         switch(var4) {
         case 1:
            if (var5 == null) {
               this.setVersion(var2);
            } else {
               this.version.set(var5, var2);
            }
            break;
         case 2:
            if (var5 == null) {
               this.setSerialNumber(var2);
            } else {
               this.serialNum.set(var5, var2);
            }
            break;
         case 3:
            if (var5 == null) {
               this.setAlgorithmId(var2);
            } else {
               this.algId.set(var5, var2);
            }
            break;
         case 4:
            this.setIssuer(var2);
            break;
         case 5:
            if (var5 == null) {
               this.setValidity(var2);
            } else {
               this.interval.set(var5, var2);
            }
            break;
         case 6:
            this.setSubject(var2);
            break;
         case 7:
            if (var5 == null) {
               this.setKey(var2);
            } else {
               this.pubKey.set(var5, var2);
            }
            break;
         case 8:
            this.setIssuerUniqueId(var2);
            break;
         case 9:
            this.setSubjectUniqueId(var2);
            break;
         case 10:
            if (var5 == null) {
               this.setExtensions(var2);
            } else {
               if (this.extensions == null) {
                  this.extensions = new CertificateExtensions();
               }

               this.extensions.set(var5, var2);
            }
         }

      }
   }

   public void delete(String var1) throws CertificateException, IOException {
      X509AttributeName var2 = new X509AttributeName(var1);
      int var3 = this.attributeMap(var2.getPrefix());
      if (var3 == 0) {
         throw new CertificateException("Attribute name not recognized: " + var1);
      } else {
         this.rawCertInfo = null;
         String var4 = var2.getSuffix();
         switch(var3) {
         case 1:
            if (var4 == null) {
               this.version = null;
            } else {
               this.version.delete(var4);
            }
            break;
         case 2:
            if (var4 == null) {
               this.serialNum = null;
            } else {
               this.serialNum.delete(var4);
            }
            break;
         case 3:
            if (var4 == null) {
               this.algId = null;
            } else {
               this.algId.delete(var4);
            }
            break;
         case 4:
            this.issuer = null;
            break;
         case 5:
            if (var4 == null) {
               this.interval = null;
            } else {
               this.interval.delete(var4);
            }
            break;
         case 6:
            this.subject = null;
            break;
         case 7:
            if (var4 == null) {
               this.pubKey = null;
            } else {
               this.pubKey.delete(var4);
            }
            break;
         case 8:
            this.issuerUniqueId = null;
            break;
         case 9:
            this.subjectUniqueId = null;
            break;
         case 10:
            if (var4 == null) {
               this.extensions = null;
            } else if (this.extensions != null) {
               this.extensions.delete(var4);
            }
         }

      }
   }

   public Object get(String var1) throws CertificateException, IOException {
      X509AttributeName var2 = new X509AttributeName(var1);
      int var3 = this.attributeMap(var2.getPrefix());
      if (var3 == 0) {
         throw new CertificateParsingException("Attribute name not recognized: " + var1);
      } else {
         String var4 = var2.getSuffix();
         switch(var3) {
         case 1:
            if (var4 == null) {
               return this.version;
            }

            return this.version.get(var4);
         case 2:
            if (var4 == null) {
               return this.serialNum;
            }

            return this.serialNum.get(var4);
         case 3:
            if (var4 == null) {
               return this.algId;
            }

            return this.algId.get(var4);
         case 4:
            if (var4 == null) {
               return this.issuer;
            }

            return this.getX500Name(var4, true);
         case 5:
            if (var4 == null) {
               return this.interval;
            }

            return this.interval.get(var4);
         case 6:
            if (var4 == null) {
               return this.subject;
            }

            return this.getX500Name(var4, false);
         case 7:
            if (var4 == null) {
               return this.pubKey;
            }

            return this.pubKey.get(var4);
         case 8:
            return this.issuerUniqueId;
         case 9:
            return this.subjectUniqueId;
         case 10:
            if (var4 == null) {
               return this.extensions;
            } else {
               if (this.extensions == null) {
                  return null;
               }

               return this.extensions.get(var4);
            }
         default:
            return null;
         }
      }
   }

   private Object getX500Name(String var1, boolean var2) throws IOException {
      if (var1.equalsIgnoreCase("dname")) {
         return var2 ? this.issuer : this.subject;
      } else if (var1.equalsIgnoreCase("x500principal")) {
         return var2 ? this.issuer.asX500Principal() : this.subject.asX500Principal();
      } else {
         throw new IOException("Attribute name not recognized.");
      }
   }

   private void parse(DerValue var1) throws CertificateParsingException, IOException {
      if (var1.tag != 48) {
         throw new CertificateParsingException("signed fields invalid");
      } else {
         this.rawCertInfo = var1.toByteArray();
         DerInputStream var2 = var1.data;
         DerValue var3 = var2.getDerValue();
         if (var3.isContextSpecific((byte)0)) {
            this.version = new CertificateVersion(var3);
            var3 = var2.getDerValue();
         }

         this.serialNum = new CertificateSerialNumber(var3);
         this.algId = new CertificateAlgorithmId(var2);
         this.issuer = new X500Name(var2);
         if (this.issuer.isEmpty()) {
            throw new CertificateParsingException("Empty issuer DN not allowed in X509Certificates");
         } else {
            this.interval = new CertificateValidity(var2);
            this.subject = new X500Name(var2);
            if (this.version.compare(0) == 0 && this.subject.isEmpty()) {
               throw new CertificateParsingException("Empty subject DN not allowed in v1 certificate");
            } else {
               this.pubKey = new CertificateX509Key(var2);
               if (var2.available() != 0) {
                  if (this.version.compare(0) == 0) {
                     throw new CertificateParsingException("no more data allowed for version 1 certificate");
                  } else {
                     var3 = var2.getDerValue();
                     if (var3.isContextSpecific((byte)1)) {
                        this.issuerUniqueId = new UniqueIdentity(var3);
                        if (var2.available() == 0) {
                           return;
                        }

                        var3 = var2.getDerValue();
                     }

                     if (var3.isContextSpecific((byte)2)) {
                        this.subjectUniqueId = new UniqueIdentity(var3);
                        if (var2.available() == 0) {
                           return;
                        }

                        var3 = var2.getDerValue();
                     }

                     if (this.version.compare(2) != 0) {
                        throw new CertificateParsingException("Extensions not allowed in v2 certificate");
                     } else {
                        if (var3.isConstructed() && var3.isContextSpecific((byte)3)) {
                           this.extensions = new CertificateExtensions(var3.data);
                        }

                        this.verifyCert(this.subject, this.extensions);
                     }
                  }
               }
            }
         }
      }
   }

   private void verifyCert(X500Name var1, CertificateExtensions var2) throws CertificateParsingException, IOException {
      if (var1.isEmpty()) {
         if (var2 == null) {
            throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and certificate has no extensions");
         }

         SubjectAlternativeNameExtension var3 = null;
         Object var4 = null;
         GeneralNames var5 = null;

         try {
            var3 = (SubjectAlternativeNameExtension)var2.get("SubjectAlternativeName");
            var5 = var3.get("subject_name");
         } catch (IOException var7) {
            throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and SubjectAlternativeName extension is absent");
         }

         if (var5 == null || var5.isEmpty()) {
            throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and SubjectAlternativeName extension is empty");
         }

         if (!var3.isCritical()) {
            throw new CertificateParsingException("X.509 Certificate is incomplete: SubjectAlternativeName extension MUST be marked critical when subject field is empty");
         }
      }

   }

   private void emit(DerOutputStream var1) throws CertificateException, IOException {
      DerOutputStream var2 = new DerOutputStream();
      this.version.encode(var2);
      this.serialNum.encode(var2);
      this.algId.encode(var2);
      if (this.version.compare(0) == 0 && this.issuer.toString() == null) {
         throw new CertificateParsingException("Null issuer DN not allowed in v1 certificate");
      } else {
         this.issuer.encode(var2);
         this.interval.encode(var2);
         if (this.version.compare(0) == 0 && this.subject.toString() == null) {
            throw new CertificateParsingException("Null subject DN not allowed in v1 certificate");
         } else {
            this.subject.encode(var2);
            this.pubKey.encode(var2);
            if (this.issuerUniqueId != null) {
               this.issuerUniqueId.encode(var2, DerValue.createTag((byte)-128, false, (byte)1));
            }

            if (this.subjectUniqueId != null) {
               this.subjectUniqueId.encode(var2, DerValue.createTag((byte)-128, false, (byte)2));
            }

            if (this.extensions != null) {
               this.extensions.encode(var2);
            }

            var1.write((byte)48, (DerOutputStream)var2);
         }
      }
   }

   private int attributeMap(String var1) {
      Integer var2 = (Integer)map.get(var1);
      return var2 == null ? 0 : var2;
   }

   private void setVersion(Object var1) throws CertificateException {
      if (!(var1 instanceof CertificateVersion)) {
         throw new CertificateException("Version class type invalid.");
      } else {
         this.version = (CertificateVersion)var1;
      }
   }

   private void setSerialNumber(Object var1) throws CertificateException {
      if (!(var1 instanceof CertificateSerialNumber)) {
         throw new CertificateException("SerialNumber class type invalid.");
      } else {
         this.serialNum = (CertificateSerialNumber)var1;
      }
   }

   private void setAlgorithmId(Object var1) throws CertificateException {
      if (!(var1 instanceof CertificateAlgorithmId)) {
         throw new CertificateException("AlgorithmId class type invalid.");
      } else {
         this.algId = (CertificateAlgorithmId)var1;
      }
   }

   private void setIssuer(Object var1) throws CertificateException {
      if (!(var1 instanceof X500Name)) {
         throw new CertificateException("Issuer class type invalid.");
      } else {
         this.issuer = (X500Name)var1;
      }
   }

   private void setValidity(Object var1) throws CertificateException {
      if (!(var1 instanceof CertificateValidity)) {
         throw new CertificateException("CertificateValidity class type invalid.");
      } else {
         this.interval = (CertificateValidity)var1;
      }
   }

   private void setSubject(Object var1) throws CertificateException {
      if (!(var1 instanceof X500Name)) {
         throw new CertificateException("Subject class type invalid.");
      } else {
         this.subject = (X500Name)var1;
      }
   }

   private void setKey(Object var1) throws CertificateException {
      if (!(var1 instanceof CertificateX509Key)) {
         throw new CertificateException("Key class type invalid.");
      } else {
         this.pubKey = (CertificateX509Key)var1;
      }
   }

   private void setIssuerUniqueId(Object var1) throws CertificateException {
      if (this.version.compare(1) < 0) {
         throw new CertificateException("Invalid version");
      } else if (!(var1 instanceof UniqueIdentity)) {
         throw new CertificateException("IssuerUniqueId class type invalid.");
      } else {
         this.issuerUniqueId = (UniqueIdentity)var1;
      }
   }

   private void setSubjectUniqueId(Object var1) throws CertificateException {
      if (this.version.compare(1) < 0) {
         throw new CertificateException("Invalid version");
      } else if (!(var1 instanceof UniqueIdentity)) {
         throw new CertificateException("SubjectUniqueId class type invalid.");
      } else {
         this.subjectUniqueId = (UniqueIdentity)var1;
      }
   }

   private void setExtensions(Object var1) throws CertificateException {
      if (this.version.compare(2) < 0) {
         throw new CertificateException("Invalid version");
      } else if (!(var1 instanceof CertificateExtensions)) {
         throw new CertificateException("Extensions class type invalid.");
      } else {
         this.extensions = (CertificateExtensions)var1;
      }
   }

   static {
      map.put("version", 1);
      map.put("serialNumber", 2);
      map.put("algorithmID", 3);
      map.put("issuer", 4);
      map.put("validity", 5);
      map.put("subject", 6);
      map.put("key", 7);
      map.put("issuerID", 8);
      map.put("subjectID", 9);
      map.put("extensions", 10);
   }
}
