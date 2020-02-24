package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Enumeration;

import sun.security.util.Debug;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CRLNumberExtension extends Extension implements CertAttrSet<String> {
   public static final String NAME = "CRLNumber";
   public static final String NUMBER = "value";
   private static final String LABEL = "CRL Number";
   private BigInteger crlNumber;
   private String extensionName;
   private String extensionLabel;

   private void encodeThis() throws IOException {
      if (this.crlNumber == null) {
         this.extensionValue = null;
      } else {
         DerOutputStream var1 = new DerOutputStream();
         var1.putInteger(this.crlNumber);
         this.extensionValue = var1.toByteArray();
      }
   }

   public CRLNumberExtension(int var1) throws IOException {
      this(PKIXExtensions.CRLNumber_Id, false, BigInteger.valueOf((long)var1), "CRLNumber", "CRL Number");
   }

   public CRLNumberExtension(BigInteger var1) throws IOException {
      this(PKIXExtensions.CRLNumber_Id, false, var1, "CRLNumber", "CRL Number");
   }

   protected CRLNumberExtension(ObjectIdentifier var1, boolean var2, BigInteger var3, String var4, String var5) throws IOException {
      this.crlNumber = null;
      this.extensionId = var1;
      this.critical = var2;
      this.crlNumber = var3;
      this.extensionName = var4;
      this.extensionLabel = var5;
      this.encodeThis();
   }

   public CRLNumberExtension(Boolean var1, Object var2) throws IOException {
      this(PKIXExtensions.CRLNumber_Id, var1, var2, "CRLNumber", "CRL Number");
   }

   protected CRLNumberExtension(ObjectIdentifier var1, Boolean var2, Object var3, String var4, String var5) throws IOException {
      this.crlNumber = null;
      this.extensionId = var1;
      this.critical = var2;
      this.extensionValue = (byte[])((byte[])var3);
      DerValue var6 = new DerValue(this.extensionValue);
      this.crlNumber = var6.getBigInteger();
      this.extensionName = var4;
      this.extensionLabel = var5;
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("value")) {
         if (!(var2 instanceof BigInteger)) {
            throw new IOException("Attribute must be of type BigInteger.");
         } else {
            this.crlNumber = (BigInteger)var2;
            this.encodeThis();
         }
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:" + this.extensionName + ".");
      }
   }

   public BigInteger get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("value")) {
         return this.crlNumber;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:" + this.extensionName + '.');
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("value")) {
         this.crlNumber = null;
         this.encodeThis();
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:" + this.extensionName + ".");
      }
   }

   public String toString() {
      String var1 = super.toString() + this.extensionLabel + ": " + (this.crlNumber == null ? "" : Debug.toHexString(this.crlNumber)) + "\n";
      return var1;
   }

   public void encode(OutputStream var1) throws IOException {
      new DerOutputStream();
      this.encode(var1, PKIXExtensions.CRLNumber_Id, true);
   }

   protected void encode(OutputStream var1, ObjectIdentifier var2, boolean var3) throws IOException {
      DerOutputStream var4 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = var2;
         this.critical = var3;
         this.encodeThis();
      }

      super.encode(var4);
      var1.write(var4.toByteArray());
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("value");
      return var1.elements();
   }

   public String getName() {
      return this.extensionName;
   }
}
