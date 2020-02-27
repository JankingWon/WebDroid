package sun.security.pkcs;

import java.io.IOException;

import sun.security.util.DerValue;

public class SigningCertificateInfo {
   private byte[] ber = null;
   private ESSCertId[] certId = null;

   public SigningCertificateInfo(byte[] var1) throws IOException {
      this.parse(var1);
   }

   public String toString() {
      StringBuffer var1 = new StringBuffer();
      var1.append("[\n");

      for(int var2 = 0; var2 < this.certId.length; ++var2) {
         var1.append(this.certId[var2].toString());
      }

      var1.append("\n]");
      return var1.toString();
   }

   public void parse(byte[] var1) throws IOException {
      DerValue var2 = new DerValue(var1);
      if (var2.tag != 48) {
         throw new IOException("Bad encoding for signingCertificate");
      } else {
         DerValue[] var3 = var2.data.getSequence(1);
         this.certId = new ESSCertId[var3.length];

         for(int var4 = 0; var4 < var3.length; ++var4) {
            this.certId[var4] = new ESSCertId(var3[var4]);
         }

         if (var2.data.available() > 0) {
            DerValue[] var6 = var2.data.getSequence(1);

            for(int var5 = 0; var5 < var6.length; ++var5) {
            }
         }

      }
   }
}
