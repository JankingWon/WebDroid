package sun.security.x509;

import java.io.IOException;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class X400Address implements GeneralNameInterface {
   byte[] nameValue = null;

   public X400Address(byte[] var1) {
      this.nameValue = var1;
   }

   public X400Address(DerValue var1) throws IOException {
      this.nameValue = var1.toByteArray();
   }

   public int getType() {
      return 3;
   }

   public void encode(DerOutputStream var1) throws IOException {
      DerValue var2 = new DerValue(this.nameValue);
      var1.putDerValue(var2);
   }

   public String toString() {
      return "X400Address: <DER-encoded value>";
   }

   public int constrains(GeneralNameInterface var1) throws UnsupportedOperationException {
      byte var2;
      if (var1 == null) {
         var2 = -1;
      } else {
         if (var1.getType() == 3) {
            throw new UnsupportedOperationException("Narrowing, widening, and match are not supported for X400Address.");
         }

         var2 = -1;
      }

      return var2;
   }

   public int subtreeDepth() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("subtreeDepth not supported for X400Address");
   }
}
