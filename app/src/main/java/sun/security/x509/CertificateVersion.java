package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateVersion implements CertAttrSet<String> {
   public static final int V1 = 0;
   public static final int V2 = 1;
   public static final int V3 = 2;
   public static final String IDENT = "x509.info.version";
   public static final String NAME = "version";
   public static final String VERSION = "number";
   int version = 0;

   private int getVersion() {
      return this.version;
   }

   private void construct(DerValue var1) throws IOException {
      if (var1.isConstructed() && var1.isContextSpecific()) {
         var1 = var1.data.getDerValue();
         this.version = var1.getInteger();
         if (var1.data.available() != 0) {
            throw new IOException("X.509 version, bad format");
         }
      }

   }

   public CertificateVersion() {
      this.version = 0;
   }

   public CertificateVersion(int var1) throws IOException {
      if (var1 != 0 && var1 != 1 && var1 != 2) {
         throw new IOException("X.509 Certificate version " + var1 + " not supported.\n");
      } else {
         this.version = var1;
      }
   }

   public CertificateVersion(DerInputStream var1) throws IOException {
      this.version = 0;
      DerValue var2 = var1.getDerValue();
      this.construct(var2);
   }

   public CertificateVersion(InputStream var1) throws IOException {
      this.version = 0;
      DerValue var2 = new DerValue(var1);
      this.construct(var2);
   }

   public CertificateVersion(DerValue var1) throws IOException {
      this.version = 0;
      this.construct(var1);
   }

   public String toString() {
      return "Version: V" + (this.version + 1);
   }

   public void encode(OutputStream var1) throws IOException {
      if (this.version != 0) {
         DerOutputStream var2 = new DerOutputStream();
         var2.putInteger(this.version);
         DerOutputStream var3 = new DerOutputStream();
         var3.write(DerValue.createTag((byte)-128, true, (byte)0), var2);
         var1.write(var3.toByteArray());
      }
   }

   public void set(String var1, Object var2) throws IOException {
      if (!(var2 instanceof Integer)) {
         throw new IOException("Attribute must be of type Integer.");
      } else if (var1.equalsIgnoreCase("number")) {
         this.version = (Integer)var2;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet: CertificateVersion.");
      }
   }

   public Integer get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("number")) {
         return new Integer(this.getVersion());
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet: CertificateVersion.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("number")) {
         this.version = 0;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet: CertificateVersion.");
      }
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("number");
      return var1.elements();
   }

   public String getName() {
      return "version";
   }

   public int compare(int var1) {
      return this.version - var1;
   }
}
