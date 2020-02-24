package sun.security.x509;

import java.io.IOException;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CertificatePolicyId {
   private ObjectIdentifier id;

   public CertificatePolicyId(ObjectIdentifier var1) {
      this.id = var1;
   }

   public CertificatePolicyId(DerValue var1) throws IOException {
      this.id = var1.getOID();
   }

   public ObjectIdentifier getIdentifier() {
      return this.id;
   }

   public String toString() {
      String var1 = "CertificatePolicyId: [" + this.id.toString() + "]\n";
      return var1;
   }

   public void encode(DerOutputStream var1) throws IOException {
      var1.putOID(this.id);
   }

   public boolean equals(Object var1) {
      return var1 instanceof CertificatePolicyId ? this.id.equals((Object)((CertificatePolicyId)var1).getIdentifier()) : false;
   }

   public int hashCode() {
      return this.id.hashCode();
   }
}
