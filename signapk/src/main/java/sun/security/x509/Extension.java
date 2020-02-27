package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class Extension implements java.security.cert.Extension {
   protected ObjectIdentifier extensionId = null;
   protected boolean critical = false;
   protected byte[] extensionValue = null;
   private static final int hashMagic = 31;

   public Extension() {
   }

   public Extension(DerValue var1) throws IOException {
      DerInputStream var2 = var1.toDerInputStream();
      this.extensionId = var2.getOID();
      DerValue var3 = var2.getDerValue();
      if (var3.tag == 1) {
         this.critical = var3.getBoolean();
         var3 = var2.getDerValue();
         this.extensionValue = var3.getOctetString();
      } else {
         this.critical = false;
         this.extensionValue = var3.getOctetString();
      }

   }

   public Extension(ObjectIdentifier var1, boolean var2, byte[] var3) throws IOException {
      this.extensionId = var1;
      this.critical = var2;
      DerValue var4 = new DerValue(var3);
      this.extensionValue = var4.getOctetString();
   }

   public Extension(Extension var1) {
      this.extensionId = var1.extensionId;
      this.critical = var1.critical;
      this.extensionValue = var1.extensionValue;
   }

   public static Extension newExtension(ObjectIdentifier var0, boolean var1, byte[] var2) throws IOException {
      Extension var3 = new Extension();
      var3.extensionId = var0;
      var3.critical = var1;
      var3.extensionValue = var2;
      return var3;
   }

   public void encode(OutputStream var1) throws IOException {
      if (var1 == null) {
         throw new NullPointerException();
      } else {
         DerOutputStream var2 = new DerOutputStream();
         DerOutputStream var3 = new DerOutputStream();
         var2.putOID(this.extensionId);
         if (this.critical) {
            var2.putBoolean(this.critical);
         }

         var2.putOctetString(this.extensionValue);
         var3.write((byte)48, (DerOutputStream)var2);
         var1.write(var3.toByteArray());
      }
   }

   public void encode(DerOutputStream var1) throws IOException {
      if (this.extensionId == null) {
         throw new IOException("Null OID to encode for the extension!");
      } else if (this.extensionValue == null) {
         throw new IOException("No value to encode for the extension!");
      } else {
         DerOutputStream var2 = new DerOutputStream();
         var2.putOID(this.extensionId);
         if (this.critical) {
            var2.putBoolean(this.critical);
         }

         var2.putOctetString(this.extensionValue);
         var1.write((byte)48, (DerOutputStream)var2);
      }
   }

   public boolean isCritical() {
      return this.critical;
   }

   public ObjectIdentifier getExtensionId() {
      return this.extensionId;
   }

   public byte[] getValue() {
      return (byte[])this.extensionValue.clone();
   }

   public byte[] getExtensionValue() {
      return this.extensionValue;
   }

   public String getId() {
      return this.extensionId.toString();
   }

   public String toString() {
      String var1 = "ObjectId: " + this.extensionId.toString();
      if (this.critical) {
         var1 = var1 + " Criticality=true\n";
      } else {
         var1 = var1 + " Criticality=false\n";
      }

      return var1;
   }

   public int hashCode() {
      int var1 = 0;
      if (this.extensionValue != null) {
         byte[] var2 = this.extensionValue;

         for(int var3 = var2.length; var3 > 0; var1 += var3-- * var2[var3]) {
         }
      }

      var1 = var1 * 31 + this.extensionId.hashCode();
      var1 = var1 * 31 + (this.critical ? 1231 : 1237);
      return var1;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof Extension)) {
         return false;
      } else {
         Extension var2 = (Extension)var1;
         if (this.critical != var2.critical) {
            return false;
         } else {
            return !this.extensionId.equals((Object)var2.extensionId) ? false : Arrays.equals(this.extensionValue, var2.extensionValue);
         }
      }
   }
}
