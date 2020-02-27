package sun.security.x509;

import java.io.IOException;
import java.security.cert.PolicyQualifierInfo;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PolicyInformation {
   public static final String NAME = "PolicyInformation";
   public static final String ID = "id";
   public static final String QUALIFIERS = "qualifiers";
   private CertificatePolicyId policyIdentifier;
   private Set<PolicyQualifierInfo> policyQualifiers;

   public PolicyInformation(CertificatePolicyId var1, Set<PolicyQualifierInfo> var2) throws IOException {
      if (var2 == null) {
         throw new NullPointerException("policyQualifiers is null");
      } else {
         this.policyQualifiers = new LinkedHashSet(var2);
         this.policyIdentifier = var1;
      }
   }

   public PolicyInformation(DerValue var1) throws IOException {
      if (var1.tag != 48) {
         throw new IOException("Invalid encoding of PolicyInformation");
      } else {
         this.policyIdentifier = new CertificatePolicyId(var1.data.getDerValue());
         if (var1.data.available() != 0) {
            this.policyQualifiers = new LinkedHashSet();
            DerValue var2 = var1.data.getDerValue();
            if (var2.tag != 48) {
               throw new IOException("Invalid encoding of PolicyInformation");
            }

            if (var2.data.available() == 0) {
               throw new IOException("No data available in policyQualifiers");
            }

            while(var2.data.available() != 0) {
               this.policyQualifiers.add(new PolicyQualifierInfo(var2.data.getDerValue().toByteArray()));
            }
         } else {
            this.policyQualifiers = Collections.emptySet();
         }

      }
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof PolicyInformation)) {
         return false;
      } else {
         PolicyInformation var2 = (PolicyInformation)var1;
         return !this.policyIdentifier.equals(var2.getPolicyIdentifier()) ? false : this.policyQualifiers.equals(var2.getPolicyQualifiers());
      }
   }

   public int hashCode() {
      int var1 = 37 + this.policyIdentifier.hashCode();
      var1 = 37 * var1 + this.policyQualifiers.hashCode();
      return var1;
   }

   public CertificatePolicyId getPolicyIdentifier() {
      return this.policyIdentifier;
   }

   public Set<PolicyQualifierInfo> getPolicyQualifiers() {
      return this.policyQualifiers;
   }

   public Object get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("id")) {
         return this.policyIdentifier;
      } else if (var1.equalsIgnoreCase("qualifiers")) {
         return this.policyQualifiers;
      } else {
         throw new IOException("Attribute name [" + var1 + "] not recognized by PolicyInformation.");
      }
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("id")) {
         if (!(var2 instanceof CertificatePolicyId)) {
            throw new IOException("Attribute value must be instance of CertificatePolicyId.");
         }

         this.policyIdentifier = (CertificatePolicyId)var2;
      } else {
         if (!var1.equalsIgnoreCase("qualifiers")) {
            throw new IOException("Attribute name [" + var1 + "] not recognized by PolicyInformation");
         }

         if (this.policyIdentifier == null) {
            throw new IOException("Attribute must have a CertificatePolicyIdentifier value before PolicyQualifierInfo can be set.");
         }

         if (!(var2 instanceof Set)) {
            throw new IOException("Attribute value must be of type Set.");
         }

         Iterator var3 = ((Set)var2).iterator();

         while(var3.hasNext()) {
            Object var4 = var3.next();
            if (!(var4 instanceof PolicyQualifierInfo)) {
               throw new IOException("Attribute value must be aSet of PolicyQualifierInfo objects.");
            }
         }

         this.policyQualifiers = (Set)var2;
      }

   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("qualifiers")) {
         this.policyQualifiers = Collections.emptySet();
      } else if (var1.equalsIgnoreCase("id")) {
         throw new IOException("Attribute ID may not be deleted from PolicyInformation.");
      } else {
         throw new IOException("Attribute name [" + var1 + "] not recognized by PolicyInformation.");
      }
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("id");
      var1.addElement("qualifiers");
      return var1.elements();
   }

   public String getName() {
      return "PolicyInformation";
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder("  [" + this.policyIdentifier.toString());
      var1.append(this.policyQualifiers + "  ]\n");
      return var1.toString();
   }

   public void encode(DerOutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      this.policyIdentifier.encode(var2);
      if (!this.policyQualifiers.isEmpty()) {
         DerOutputStream var3 = new DerOutputStream();
         Iterator var4 = this.policyQualifiers.iterator();

         while(var4.hasNext()) {
            PolicyQualifierInfo var5 = (PolicyQualifierInfo)var4.next();
            var3.write(var5.getEncoded());
         }

         var2.write((byte)48, (DerOutputStream)var3);
      }

      var1.write((byte)48, (DerOutputStream)var2);
   }
}
