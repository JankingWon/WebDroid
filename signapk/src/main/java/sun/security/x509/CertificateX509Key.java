package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.Enumeration;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateX509Key implements CertAttrSet<String> {
   public static final String IDENT = "x509.info.key";
   public static final String NAME = "key";
   public static final String KEY = "value";
   private PublicKey key;

   public CertificateX509Key(PublicKey var1) {
      this.key = var1;
   }

   public CertificateX509Key(DerInputStream var1) throws IOException {
      DerValue var2 = var1.getDerValue();
      this.key = X509Key.parse(var2);
   }

   public CertificateX509Key(InputStream var1) throws IOException {
      DerValue var2 = new DerValue(var1);
      this.key = X509Key.parse(var2);
   }

   public String toString() {
      return this.key == null ? "" : this.key.toString();
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      var2.write(this.key.getEncoded());
      var1.write(var2.toByteArray());
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("value")) {
         this.key = (PublicKey)var2;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet: CertificateX509Key.");
      }
   }

   public PublicKey get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("value")) {
         return this.key;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet: CertificateX509Key.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("value")) {
         this.key = null;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet: CertificateX509Key.");
      }
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("value");
      return var1.elements();
   }

   public String getName() {
      return "key";
   }
}
