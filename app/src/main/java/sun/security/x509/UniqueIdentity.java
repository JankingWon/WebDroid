package sun.security.x509;

import java.io.IOException;

import sun.security.util.BitArray;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class UniqueIdentity {
   private BitArray id;

   public UniqueIdentity(BitArray var1) {
      this.id = var1;
   }

   public UniqueIdentity(byte[] var1) {
      this.id = new BitArray(var1.length * 8, var1);
   }

   public UniqueIdentity(DerInputStream var1) throws IOException {
      DerValue var2 = var1.getDerValue();
      this.id = var2.getUnalignedBitString(true);
   }

   public UniqueIdentity(DerValue var1) throws IOException {
      this.id = var1.getUnalignedBitString(true);
   }

   public String toString() {
      return "UniqueIdentity:" + this.id.toString() + "\n";
   }

   public void encode(DerOutputStream var1, byte var2) throws IOException {
      byte[] var3 = this.id.toByteArray();
      int var4 = var3.length * 8 - this.id.length();
      var1.write(var2);
      var1.putLength(var3.length + 1);
      var1.write(var4);
      var1.write(var3);
   }

   public boolean[] getId() {
      return this.id == null ? null : this.id.toBooleanArray();
   }
}
