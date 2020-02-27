package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateIssuerExtension extends Extension implements CertAttrSet<String> {
   public static final String NAME = "CertificateIssuer";
   public static final String ISSUER = "issuer";
   private GeneralNames names;

   private void encodeThis() throws IOException {
      if (this.names != null && !this.names.isEmpty()) {
         DerOutputStream var1 = new DerOutputStream();
         this.names.encode(var1);
         this.extensionValue = var1.toByteArray();
      } else {
         this.extensionValue = null;
      }
   }

   public CertificateIssuerExtension(GeneralNames var1) throws IOException {
      this.extensionId = PKIXExtensions.CertificateIssuer_Id;
      this.critical = true;
      this.names = var1;
      this.encodeThis();
   }

   public CertificateIssuerExtension(Boolean var1, Object var2) throws IOException {
      this.extensionId = PKIXExtensions.CertificateIssuer_Id;
      this.critical = var1;
      this.extensionValue = (byte[])((byte[])var2);
      DerValue var3 = new DerValue(this.extensionValue);
      this.names = new GeneralNames(var3);
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("issuer")) {
         if (!(var2 instanceof GeneralNames)) {
            throw new IOException("Attribute value must be of type GeneralNames");
         } else {
            this.names = (GeneralNames)var2;
            this.encodeThis();
         }
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:CertificateIssuer");
      }
   }

   public GeneralNames get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("issuer")) {
         return this.names;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:CertificateIssuer");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("issuer")) {
         this.names = null;
         this.encodeThis();
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:CertificateIssuer");
      }
   }

   public String toString() {
      return super.toString() + "Certificate Issuer [\n" + this.names + "]\n";
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = PKIXExtensions.CertificateIssuer_Id;
         this.critical = true;
         this.encodeThis();
      }

      super.encode(var2);
      var1.write(var2.toByteArray());
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("issuer");
      return var1.elements();
   }

   public String getName() {
      return "CertificateIssuer";
   }
}
