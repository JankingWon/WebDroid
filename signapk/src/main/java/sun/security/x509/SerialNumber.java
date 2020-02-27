package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class SerialNumber {
   private BigInteger serialNum;

   private void construct(DerValue var1) throws IOException {
      this.serialNum = var1.getBigInteger();
      if (var1.data.available() != 0) {
         throw new IOException("Excess SerialNumber data");
      }
   }

   public SerialNumber(BigInteger var1) {
      this.serialNum = var1;
   }

   public SerialNumber(int var1) {
      this.serialNum = BigInteger.valueOf((long)var1);
   }

   public SerialNumber(DerInputStream var1) throws IOException {
      DerValue var2 = var1.getDerValue();
      this.construct(var2);
   }

   public SerialNumber(DerValue var1) throws IOException {
      this.construct(var1);
   }

   public SerialNumber(InputStream var1) throws IOException {
      DerValue var2 = new DerValue(var1);
      this.construct(var2);
   }

   public String toString() {
      return "SerialNumber: [" + Debug.toHexString(this.serialNum) + "]";
   }

   public void encode(DerOutputStream var1) throws IOException {
      var1.putInteger(this.serialNum);
   }

   public BigInteger getNumber() {
      return this.serialNum;
   }
}
