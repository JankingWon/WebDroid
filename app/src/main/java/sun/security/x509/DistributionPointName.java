package sun.security.x509;

import java.io.IOException;
import java.util.Objects;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class DistributionPointName {
   private static final byte TAG_FULL_NAME = 0;
   private static final byte TAG_RELATIVE_NAME = 1;
   private GeneralNames fullName = null;
   private RDN relativeName = null;
   private volatile int hashCode;

   public DistributionPointName(GeneralNames var1) {
      if (var1 == null) {
         throw new IllegalArgumentException("fullName must not be null");
      } else {
         this.fullName = var1;
      }
   }

   public DistributionPointName(RDN var1) {
      if (var1 == null) {
         throw new IllegalArgumentException("relativeName must not be null");
      } else {
         this.relativeName = var1;
      }
   }

   public DistributionPointName(DerValue var1) throws IOException {
      if (var1.isContextSpecific((byte)0) && var1.isConstructed()) {
         var1.resetTag((byte)48);
         this.fullName = new GeneralNames(var1);
      } else {
         if (!var1.isContextSpecific((byte)1) || !var1.isConstructed()) {
            throw new IOException("Invalid encoding for DistributionPointName");
         }

         var1.resetTag((byte)49);
         this.relativeName = new RDN(var1);
      }

   }

   public GeneralNames getFullName() {
      return this.fullName;
   }

   public RDN getRelativeName() {
      return this.relativeName;
   }

   public void encode(DerOutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.fullName != null) {
         this.fullName.encode(var2);
         var1.writeImplicit(DerValue.createTag((byte)-128, true, (byte)0), var2);
      } else {
         this.relativeName.encode(var2);
         var1.writeImplicit(DerValue.createTag((byte)-128, true, (byte)1), var2);
      }

   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof DistributionPointName)) {
         return false;
      } else {
         DistributionPointName var2 = (DistributionPointName)var1;
         return Objects.equals(this.fullName, var2.fullName) && Objects.equals(this.relativeName, var2.relativeName);
      }
   }

   public int hashCode() {
      int var1 = this.hashCode;
      if (var1 == 0) {
         byte var2 = 1;
         if (this.fullName != null) {
            var1 = var2 + this.fullName.hashCode();
         } else {
            var1 = var2 + this.relativeName.hashCode();
         }

         this.hashCode = var1;
      }

      return var1;
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder();
      if (this.fullName != null) {
         var1.append("DistributionPointName:\n     " + this.fullName + "\n");
      } else {
         var1.append("DistributionPointName:\n     " + this.relativeName + "\n");
      }

      return var1.toString();
   }
}
