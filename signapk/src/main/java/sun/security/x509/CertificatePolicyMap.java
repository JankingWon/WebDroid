package sun.security.x509;

import java.io.IOException;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificatePolicyMap {
   private CertificatePolicyId issuerDomain;
   private CertificatePolicyId subjectDomain;

   public CertificatePolicyMap(CertificatePolicyId var1, CertificatePolicyId var2) {
      this.issuerDomain = var1;
      this.subjectDomain = var2;
   }

   public CertificatePolicyMap(DerValue var1) throws IOException {
      if (var1.tag != 48) {
         throw new IOException("Invalid encoding for CertificatePolicyMap");
      } else {
         this.issuerDomain = new CertificatePolicyId(var1.data.getDerValue());
         this.subjectDomain = new CertificatePolicyId(var1.data.getDerValue());
      }
   }

   public CertificatePolicyId getIssuerIdentifier() {
      return this.issuerDomain;
   }

   public CertificatePolicyId getSubjectIdentifier() {
      return this.subjectDomain;
   }

   public String toString() {
      String var1 = "CertificatePolicyMap: [\nIssuerDomain:" + this.issuerDomain.toString() + "SubjectDomain:" + this.subjectDomain.toString() + "]\n";
      return var1;
   }

   public void encode(DerOutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      this.issuerDomain.encode(var2);
      this.subjectDomain.encode(var2);
      var1.write((byte)48, (DerOutputStream)var2);
   }
}
