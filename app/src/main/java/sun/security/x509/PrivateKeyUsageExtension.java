package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Objects;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PrivateKeyUsageExtension extends Extension implements CertAttrSet<String> {
   public static final String IDENT = "x509.info.extensions.PrivateKeyUsage";
   public static final String NAME = "PrivateKeyUsage";
   public static final String NOT_BEFORE = "not_before";
   public static final String NOT_AFTER = "not_after";
   private static final byte TAG_BEFORE = 0;
   private static final byte TAG_AFTER = 1;
   private Date notBefore = null;
   private Date notAfter = null;

   private void encodeThis() throws IOException {
      if (this.notBefore == null && this.notAfter == null) {
         this.extensionValue = null;
      } else {
         DerOutputStream var1 = new DerOutputStream();
         DerOutputStream var2 = new DerOutputStream();
         DerOutputStream var3;
         if (this.notBefore != null) {
            var3 = new DerOutputStream();
            var3.putGeneralizedTime(this.notBefore);
            var2.writeImplicit(DerValue.createTag((byte)-128, false, (byte)0), var3);
         }

         if (this.notAfter != null) {
            var3 = new DerOutputStream();
            var3.putGeneralizedTime(this.notAfter);
            var2.writeImplicit(DerValue.createTag((byte)-128, false, (byte)1), var3);
         }

         var1.write((byte)48, (DerOutputStream)var2);
         this.extensionValue = var1.toByteArray();
      }
   }

   public PrivateKeyUsageExtension(Date var1, Date var2) throws IOException {
      this.notBefore = var1;
      this.notAfter = var2;
      this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
      this.critical = false;
      this.encodeThis();
   }

   public PrivateKeyUsageExtension(Boolean var1, Object var2) throws CertificateException, IOException {
      this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
      this.critical = var1;
      this.extensionValue = (byte[])((byte[])var2);
      DerInputStream var3 = new DerInputStream(this.extensionValue);
      DerValue[] var4 = var3.getSequence(2);

      for(int var5 = 0; var5 < var4.length; ++var5) {
         DerValue var6 = var4[var5];
         if (var6.isContextSpecific((byte)0) && !var6.isConstructed()) {
            if (this.notBefore != null) {
               throw new CertificateParsingException("Duplicate notBefore in PrivateKeyUsage.");
            }

            var6.resetTag((byte)24);
            var3 = new DerInputStream(var6.toByteArray());
            this.notBefore = var3.getGeneralizedTime();
         } else {
            if (!var6.isContextSpecific((byte)1) || var6.isConstructed()) {
               throw new IOException("Invalid encoding of PrivateKeyUsageExtension");
            }

            if (this.notAfter != null) {
               throw new CertificateParsingException("Duplicate notAfter in PrivateKeyUsage.");
            }

            var6.resetTag((byte)24);
            var3 = new DerInputStream(var6.toByteArray());
            this.notAfter = var3.getGeneralizedTime();
         }
      }

   }

   public String toString() {
      return super.toString() + "PrivateKeyUsage: [\n" + (this.notBefore == null ? "" : "From: " + this.notBefore.toString() + ", ") + (this.notAfter == null ? "" : "To: " + this.notAfter.toString()) + "]\n";
   }

   public void valid() throws CertificateNotYetValidException, CertificateExpiredException {
      Date var1 = new Date();
      this.valid(var1);
   }

   public void valid(Date var1) throws CertificateNotYetValidException, CertificateExpiredException {
      Objects.requireNonNull(var1);
      if (this.notBefore != null && this.notBefore.after(var1)) {
         throw new CertificateNotYetValidException("NotBefore: " + this.notBefore.toString());
      } else if (this.notAfter != null && this.notAfter.before(var1)) {
         throw new CertificateExpiredException("NotAfter: " + this.notAfter.toString());
      }
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
         this.critical = false;
         this.encodeThis();
      }

      super.encode(var2);
      var1.write(var2.toByteArray());
   }

   public void set(String var1, Object var2) throws CertificateException, IOException {
      if (!(var2 instanceof Date)) {
         throw new CertificateException("Attribute must be of type Date.");
      } else {
         if (var1.equalsIgnoreCase("not_before")) {
            this.notBefore = (Date)var2;
         } else {
            if (!var1.equalsIgnoreCase("not_after")) {
               throw new CertificateException("Attribute name not recognized by CertAttrSet:PrivateKeyUsage.");
            }

            this.notAfter = (Date)var2;
         }

         this.encodeThis();
      }
   }

   public Date get(String var1) throws CertificateException {
      if (var1.equalsIgnoreCase("not_before")) {
         return new Date(this.notBefore.getTime());
      } else if (var1.equalsIgnoreCase("not_after")) {
         return new Date(this.notAfter.getTime());
      } else {
         throw new CertificateException("Attribute name not recognized by CertAttrSet:PrivateKeyUsage.");
      }
   }

   public void delete(String var1) throws CertificateException, IOException {
      if (var1.equalsIgnoreCase("not_before")) {
         this.notBefore = null;
      } else {
         if (!var1.equalsIgnoreCase("not_after")) {
            throw new CertificateException("Attribute name not recognized by CertAttrSet:PrivateKeyUsage.");
         }

         this.notAfter = null;
      }

      this.encodeThis();
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("not_before");
      var1.addElement("not_after");
      return var1.elements();
   }

   public String getName() {
      return "PrivateKeyUsage";
   }
}
