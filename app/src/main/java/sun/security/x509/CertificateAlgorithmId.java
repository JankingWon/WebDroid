package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateAlgorithmId implements CertAttrSet<String> {
   private AlgorithmId algId;
   public static final String IDENT = "x509.info.algorithmID";
   public static final String NAME = "algorithmID";
   public static final String ALGORITHM = "algorithm";

   public CertificateAlgorithmId(AlgorithmId var1) {
      this.algId = var1;
   }

   public CertificateAlgorithmId(DerInputStream var1) throws IOException {
      DerValue var2 = var1.getDerValue();
      this.algId = AlgorithmId.parse(var2);
   }

   public CertificateAlgorithmId(InputStream var1) throws IOException {
      DerValue var2 = new DerValue(var1);
      this.algId = AlgorithmId.parse(var2);
   }

   public String toString() {
      return this.algId == null ? "" : this.algId.toString() + ", OID = " + this.algId.getOID().toString() + "\n";
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      this.algId.encode(var2);
      var1.write(var2.toByteArray());
   }

   public void set(String var1, Object var2) throws IOException {
      if (!(var2 instanceof AlgorithmId)) {
         throw new IOException("Attribute must be of type AlgorithmId.");
      } else if (var1.equalsIgnoreCase("algorithm")) {
         this.algId = (AlgorithmId)var2;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:CertificateAlgorithmId.");
      }
   }

   public AlgorithmId get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("algorithm")) {
         return this.algId;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:CertificateAlgorithmId.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("algorithm")) {
         this.algId = null;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:CertificateAlgorithmId.");
      }
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("algorithm");
      return var1.elements();
   }

   public String getName() {
      return "algorithmID";
   }
}
