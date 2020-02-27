package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CRLReason;
import java.util.Enumeration;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CRLReasonCodeExtension extends Extension implements CertAttrSet<String> {
   public static final String NAME = "CRLReasonCode";
   public static final String REASON = "reason";
   private static CRLReason[] values = CRLReason.values();
   private int reasonCode;

   private void encodeThis() throws IOException {
      if (this.reasonCode == 0) {
         this.extensionValue = null;
      } else {
         DerOutputStream var1 = new DerOutputStream();
         var1.putEnumerated(this.reasonCode);
         this.extensionValue = var1.toByteArray();
      }
   }

   public CRLReasonCodeExtension(int var1) throws IOException {
      this(false, var1);
   }

   public CRLReasonCodeExtension(boolean var1, int var2) throws IOException {
      this.reasonCode = 0;
      this.extensionId = PKIXExtensions.ReasonCode_Id;
      this.critical = var1;
      this.reasonCode = var2;
      this.encodeThis();
   }

   public CRLReasonCodeExtension(Boolean var1, Object var2) throws IOException {
      this.reasonCode = 0;
      this.extensionId = PKIXExtensions.ReasonCode_Id;
      this.critical = var1;
      this.extensionValue = (byte[])((byte[])var2);
      DerValue var3 = new DerValue(this.extensionValue);
      this.reasonCode = var3.getEnumerated();
   }

   public void set(String var1, Object var2) throws IOException {
      if (!(var2 instanceof Integer)) {
         throw new IOException("Attribute must be of type Integer.");
      } else if (var1.equalsIgnoreCase("reason")) {
         this.reasonCode = (Integer)var2;
         this.encodeThis();
      } else {
         throw new IOException("Name not supported by CRLReasonCodeExtension");
      }
   }

   public Integer get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("reason")) {
         return new Integer(this.reasonCode);
      } else {
         throw new IOException("Name not supported by CRLReasonCodeExtension");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("reason")) {
         this.reasonCode = 0;
         this.encodeThis();
      } else {
         throw new IOException("Name not supported by CRLReasonCodeExtension");
      }
   }

   public String toString() {
      return super.toString() + "    Reason Code: " + this.getReasonCode();
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = PKIXExtensions.ReasonCode_Id;
         this.critical = false;
         this.encodeThis();
      }

      super.encode(var2);
      var1.write(var2.toByteArray());
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("reason");
      return var1.elements();
   }

   public String getName() {
      return "CRLReasonCode";
   }

   public CRLReason getReasonCode() {
      return this.reasonCode > 0 && this.reasonCode < values.length ? values[this.reasonCode] : CRLReason.UNSPECIFIED;
   }
}
