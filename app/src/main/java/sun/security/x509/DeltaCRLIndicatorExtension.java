package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import sun.security.util.DerOutputStream;

public class DeltaCRLIndicatorExtension extends CRLNumberExtension {
   public static final String NAME = "DeltaCRLIndicator";
   private static final String LABEL = "Base CRL Number";

   public DeltaCRLIndicatorExtension(int var1) throws IOException {
      super(PKIXExtensions.DeltaCRLIndicator_Id, true, BigInteger.valueOf((long)var1), "DeltaCRLIndicator", "Base CRL Number");
   }

   public DeltaCRLIndicatorExtension(BigInteger var1) throws IOException {
      super(PKIXExtensions.DeltaCRLIndicator_Id, true, var1, "DeltaCRLIndicator", "Base CRL Number");
   }

   public DeltaCRLIndicatorExtension(Boolean var1, Object var2) throws IOException {
      super(PKIXExtensions.DeltaCRLIndicator_Id, var1, var2, "DeltaCRLIndicator", "Base CRL Number");
   }

   public void encode(OutputStream var1) throws IOException {
      new DerOutputStream();
      super.encode(var1, PKIXExtensions.DeltaCRLIndicator_Id, true);
   }
}
