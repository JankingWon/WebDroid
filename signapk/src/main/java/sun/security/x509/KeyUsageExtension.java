package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KeyUsageExtension extends Extension implements CertAttrSet<String> {
   public static final String IDENT = "x509.info.extensions.KeyUsage";
   public static final String NAME = "KeyUsage";
   public static final String DIGITAL_SIGNATURE = "digital_signature";
   public static final String NON_REPUDIATION = "non_repudiation";
   public static final String KEY_ENCIPHERMENT = "key_encipherment";
   public static final String DATA_ENCIPHERMENT = "data_encipherment";
   public static final String KEY_AGREEMENT = "key_agreement";
   public static final String KEY_CERTSIGN = "key_certsign";
   public static final String CRL_SIGN = "crl_sign";
   public static final String ENCIPHER_ONLY = "encipher_only";
   public static final String DECIPHER_ONLY = "decipher_only";
   private boolean[] bitString;

   private void encodeThis() throws IOException {
      DerOutputStream var1 = new DerOutputStream();
      var1.putTruncatedUnalignedBitString(new BitArray(this.bitString));
      this.extensionValue = var1.toByteArray();
   }

   private boolean isSet(int var1) {
      return var1 < this.bitString.length && this.bitString[var1];
   }

   private void set(int var1, boolean var2) {
      if (var1 >= this.bitString.length) {
         boolean[] var3 = new boolean[var1 + 1];
         System.arraycopy(this.bitString, 0, var3, 0, this.bitString.length);
         this.bitString = var3;
      }

      this.bitString[var1] = var2;
   }

   public KeyUsageExtension(byte[] var1) throws IOException {
      this.bitString = (new BitArray(var1.length * 8, var1)).toBooleanArray();
      this.extensionId = PKIXExtensions.KeyUsage_Id;
      this.critical = true;
      this.encodeThis();
   }

   public KeyUsageExtension(boolean[] var1) throws IOException {
      this.bitString = var1;
      this.extensionId = PKIXExtensions.KeyUsage_Id;
      this.critical = true;
      this.encodeThis();
   }

   public KeyUsageExtension(BitArray var1) throws IOException {
      this.bitString = var1.toBooleanArray();
      this.extensionId = PKIXExtensions.KeyUsage_Id;
      this.critical = true;
      this.encodeThis();
   }

   public KeyUsageExtension(Boolean var1, Object var2) throws IOException {
      this.extensionId = PKIXExtensions.KeyUsage_Id;
      this.critical = var1;
      byte[] var3 = (byte[])((byte[])var2);
      if (var3[0] == 4) {
         this.extensionValue = (new DerValue(var3)).getOctetString();
      } else {
         this.extensionValue = var3;
      }

      DerValue var4 = new DerValue(this.extensionValue);
      this.bitString = var4.getUnalignedBitString().toBooleanArray();
   }

   public KeyUsageExtension() {
      this.extensionId = PKIXExtensions.KeyUsage_Id;
      this.critical = true;
      this.bitString = new boolean[0];
   }

   public void set(String var1, Object var2) throws IOException {
      if (!(var2 instanceof Boolean)) {
         throw new IOException("Attribute must be of type Boolean.");
      } else {
         boolean var3 = (Boolean)var2;
         if (var1.equalsIgnoreCase("digital_signature")) {
            this.set(0, var3);
         } else if (var1.equalsIgnoreCase("non_repudiation")) {
            this.set(1, var3);
         } else if (var1.equalsIgnoreCase("key_encipherment")) {
            this.set(2, var3);
         } else if (var1.equalsIgnoreCase("data_encipherment")) {
            this.set(3, var3);
         } else if (var1.equalsIgnoreCase("key_agreement")) {
            this.set(4, var3);
         } else if (var1.equalsIgnoreCase("key_certsign")) {
            this.set(5, var3);
         } else if (var1.equalsIgnoreCase("crl_sign")) {
            this.set(6, var3);
         } else if (var1.equalsIgnoreCase("encipher_only")) {
            this.set(7, var3);
         } else {
            if (!var1.equalsIgnoreCase("decipher_only")) {
               throw new IOException("Attribute name not recognized by CertAttrSet:KeyUsage.");
            }

            this.set(8, var3);
         }

         this.encodeThis();
      }
   }

   public Boolean get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("digital_signature")) {
         return this.isSet(0);
      } else if (var1.equalsIgnoreCase("non_repudiation")) {
         return this.isSet(1);
      } else if (var1.equalsIgnoreCase("key_encipherment")) {
         return this.isSet(2);
      } else if (var1.equalsIgnoreCase("data_encipherment")) {
         return this.isSet(3);
      } else if (var1.equalsIgnoreCase("key_agreement")) {
         return this.isSet(4);
      } else if (var1.equalsIgnoreCase("key_certsign")) {
         return this.isSet(5);
      } else if (var1.equalsIgnoreCase("crl_sign")) {
         return this.isSet(6);
      } else if (var1.equalsIgnoreCase("encipher_only")) {
         return this.isSet(7);
      } else if (var1.equalsIgnoreCase("decipher_only")) {
         return this.isSet(8);
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:KeyUsage.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("digital_signature")) {
         this.set(0, false);
      } else if (var1.equalsIgnoreCase("non_repudiation")) {
         this.set(1, false);
      } else if (var1.equalsIgnoreCase("key_encipherment")) {
         this.set(2, false);
      } else if (var1.equalsIgnoreCase("data_encipherment")) {
         this.set(3, false);
      } else if (var1.equalsIgnoreCase("key_agreement")) {
         this.set(4, false);
      } else if (var1.equalsIgnoreCase("key_certsign")) {
         this.set(5, false);
      } else if (var1.equalsIgnoreCase("crl_sign")) {
         this.set(6, false);
      } else if (var1.equalsIgnoreCase("encipher_only")) {
         this.set(7, false);
      } else {
         if (!var1.equalsIgnoreCase("decipher_only")) {
            throw new IOException("Attribute name not recognized by CertAttrSet:KeyUsage.");
         }

         this.set(8, false);
      }

      this.encodeThis();
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder();
      var1.append(super.toString());
      var1.append("KeyUsage [\n");
      if (this.isSet(0)) {
         var1.append("  DigitalSignature\n");
      }

      if (this.isSet(1)) {
         var1.append("  Non_repudiation\n");
      }

      if (this.isSet(2)) {
         var1.append("  Key_Encipherment\n");
      }

      if (this.isSet(3)) {
         var1.append("  Data_Encipherment\n");
      }

      if (this.isSet(4)) {
         var1.append("  Key_Agreement\n");
      }

      if (this.isSet(5)) {
         var1.append("  Key_CertSign\n");
      }

      if (this.isSet(6)) {
         var1.append("  Crl_Sign\n");
      }

      if (this.isSet(7)) {
         var1.append("  Encipher_Only\n");
      }

      if (this.isSet(8)) {
         var1.append("  Decipher_Only\n");
      }

      var1.append("]\n");
      return var1.toString();
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = PKIXExtensions.KeyUsage_Id;
         this.critical = true;
         this.encodeThis();
      }

      super.encode(var2);
      var1.write(var2.toByteArray());
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("digital_signature");
      var1.addElement("non_repudiation");
      var1.addElement("key_encipherment");
      var1.addElement("data_encipherment");
      var1.addElement("key_agreement");
      var1.addElement("key_certsign");
      var1.addElement("crl_sign");
      var1.addElement("encipher_only");
      var1.addElement("decipher_only");
      return var1.elements();
   }

   public boolean[] getBits() {
      return (boolean[])this.bitString.clone();
   }

   public String getName() {
      return "KeyUsage";
   }
}
