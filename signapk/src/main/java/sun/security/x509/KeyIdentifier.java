package sun.security.x509;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

import sun.misc.HexDumpEncoder;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KeyIdentifier {
   private byte[] octetString;

   public KeyIdentifier(byte[] var1) {
      this.octetString = (byte[])var1.clone();
   }

   public KeyIdentifier(DerValue var1) throws IOException {
      this.octetString = var1.getOctetString();
   }

   public KeyIdentifier(PublicKey var1) throws IOException {
      DerValue var2 = new DerValue(var1.getEncoded());
      if (var2.tag != 48) {
         throw new IOException("PublicKey value is not a valid X.509 public key");
      } else {
         AlgorithmId var3 = AlgorithmId.parse(var2.data.getDerValue());
         byte[] var4 = var2.data.getUnalignedBitString().toByteArray();
         MessageDigest var5 = null;

         try {
            var5 = MessageDigest.getInstance("SHA1");
         } catch (NoSuchAlgorithmException var7) {
            throw new IOException("SHA1 not supported");
         }

         var5.update(var4);
         this.octetString = var5.digest();
      }
   }

   public byte[] getIdentifier() {
      return (byte[])this.octetString.clone();
   }

   public String toString() {
      String var1 = "KeyIdentifier [\n";
      HexDumpEncoder var2 = new HexDumpEncoder();
      var1 = var1 + var2.encodeBuffer(this.octetString);
      var1 = var1 + "]\n";
      return var1;
   }

   void encode(DerOutputStream var1) throws IOException {
      var1.putOctetString(this.octetString);
   }

   public int hashCode() {
      int var1 = 0;

      for(int var2 = 0; var2 < this.octetString.length; ++var2) {
         var1 += this.octetString[var2] * var2;
      }

      return var1;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof KeyIdentifier)) {
         return false;
      } else {
         byte[] var2 = ((KeyIdentifier)var1).octetString;
         return Arrays.equals(this.octetString, var2);
      }
   }
}
