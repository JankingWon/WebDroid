package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class SubjectAlternativeNameExtension extends Extension implements CertAttrSet<String> {
   public static final String IDENT = "x509.info.extensions.SubjectAlternativeName";
   public static final String NAME = "SubjectAlternativeName";
   public static final String SUBJECT_NAME = "subject_name";
   GeneralNames names;

   private void encodeThis() throws IOException {
      if (this.names != null && !this.names.isEmpty()) {
         DerOutputStream var1 = new DerOutputStream();
         this.names.encode(var1);
         this.extensionValue = var1.toByteArray();
      } else {
         this.extensionValue = null;
      }
   }

   public SubjectAlternativeNameExtension(GeneralNames var1) throws IOException {
      this(Boolean.FALSE, var1);
   }

   public SubjectAlternativeNameExtension(Boolean var1, GeneralNames var2) throws IOException {
      this.names = null;
      this.names = var2;
      this.extensionId = PKIXExtensions.SubjectAlternativeName_Id;
      this.critical = var1;
      this.encodeThis();
   }

   public SubjectAlternativeNameExtension() {
      this.names = null;
      this.extensionId = PKIXExtensions.SubjectAlternativeName_Id;
      this.critical = false;
      this.names = new GeneralNames();
   }

   public SubjectAlternativeNameExtension(Boolean var1, Object var2) throws IOException {
      this.names = null;
      this.extensionId = PKIXExtensions.SubjectAlternativeName_Id;
      this.critical = var1;
      this.extensionValue = (byte[])((byte[])var2);
      DerValue var3 = new DerValue(this.extensionValue);
      if (var3.data == null) {
         this.names = new GeneralNames();
      } else {
         this.names = new GeneralNames(var3);
      }
   }

   public String toString() {
      String var1 = super.toString() + "SubjectAlternativeName [\n";
      GeneralName var3;
      if (this.names == null) {
         var1 = var1 + "  null\n";
      } else {
         for(Iterator var2 = this.names.names().iterator(); var2.hasNext(); var1 = var1 + "  " + var3 + "\n") {
            var3 = (GeneralName)var2.next();
         }
      }

      var1 = var1 + "]\n";
      return var1;
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = PKIXExtensions.SubjectAlternativeName_Id;
         this.critical = false;
         this.encodeThis();
      }

      super.encode(var2);
      var1.write(var2.toByteArray());
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("subject_name")) {
         if (!(var2 instanceof GeneralNames)) {
            throw new IOException("Attribute value should be of type GeneralNames.");
         } else {
            this.names = (GeneralNames)var2;
            this.encodeThis();
         }
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:SubjectAlternativeName.");
      }
   }

   public GeneralNames get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("subject_name")) {
         return this.names;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:SubjectAlternativeName.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("subject_name")) {
         this.names = null;
         this.encodeThis();
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:SubjectAlternativeName.");
      }
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("subject_name");
      return var1.elements();
   }

   public String getName() {
      return "SubjectAlternativeName";
   }
}
