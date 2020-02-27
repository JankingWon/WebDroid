package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class SubjectKeyIdentifierExtension extends Extension implements CertAttrSet<String> {
   public static final String IDENT = "x509.info.extensions.SubjectKeyIdentifier";
   public static final String NAME = "SubjectKeyIdentifier";
   public static final String KEY_ID = "key_id";
   private KeyIdentifier id = null;

   private void encodeThis() throws IOException {
      if (this.id == null) {
         this.extensionValue = null;
      } else {
         DerOutputStream var1 = new DerOutputStream();
         this.id.encode(var1);
         this.extensionValue = var1.toByteArray();
      }
   }

   public SubjectKeyIdentifierExtension(byte[] var1) throws IOException {
      this.id = new KeyIdentifier(var1);
      this.extensionId = PKIXExtensions.SubjectKey_Id;
      this.critical = false;
      this.encodeThis();
   }

   public SubjectKeyIdentifierExtension(Boolean var1, Object var2) throws IOException {
      this.extensionId = PKIXExtensions.SubjectKey_Id;
      this.critical = var1;
      this.extensionValue = (byte[])((byte[])var2);
      DerValue var3 = new DerValue(this.extensionValue);
      this.id = new KeyIdentifier(var3);
   }

   public String toString() {
      return super.toString() + "SubjectKeyIdentifier [\n" + this.id + "]\n";
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = PKIXExtensions.SubjectKey_Id;
         this.critical = false;
         this.encodeThis();
      }

      super.encode(var2);
      var1.write(var2.toByteArray());
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("key_id")) {
         if (!(var2 instanceof KeyIdentifier)) {
            throw new IOException("Attribute value should be of type KeyIdentifier.");
         } else {
            this.id = (KeyIdentifier)var2;
            this.encodeThis();
         }
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:SubjectKeyIdentifierExtension.");
      }
   }

   public KeyIdentifier get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("key_id")) {
         return this.id;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:SubjectKeyIdentifierExtension.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("key_id")) {
         this.id = null;
         this.encodeThis();
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:SubjectKeyIdentifierExtension.");
      }
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("key_id");
      return var1.elements();
   }

   public String getName() {
      return "SubjectKeyIdentifier";
   }
}
