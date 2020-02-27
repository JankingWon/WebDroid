package sun.security.x509;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class DistributionPoint {
   public static final int KEY_COMPROMISE = 1;
   public static final int CA_COMPROMISE = 2;
   public static final int AFFILIATION_CHANGED = 3;
   public static final int SUPERSEDED = 4;
   public static final int CESSATION_OF_OPERATION = 5;
   public static final int CERTIFICATE_HOLD = 6;
   public static final int PRIVILEGE_WITHDRAWN = 7;
   public static final int AA_COMPROMISE = 8;
   private static final String[] REASON_STRINGS = new String[]{null, "key compromise", "CA compromise", "affiliation changed", "superseded", "cessation of operation", "certificate hold", "privilege withdrawn", "AA compromise"};
   private static final byte TAG_DIST_PT = 0;
   private static final byte TAG_REASONS = 1;
   private static final byte TAG_ISSUER = 2;
   private static final byte TAG_FULL_NAME = 0;
   private static final byte TAG_REL_NAME = 1;
   private GeneralNames fullName;
   private RDN relativeName;
   private boolean[] reasonFlags;
   private GeneralNames crlIssuer;
   private volatile int hashCode;

   public DistributionPoint(GeneralNames var1, boolean[] var2, GeneralNames var3) {
      if (var1 == null && var3 == null) {
         throw new IllegalArgumentException("fullName and crlIssuer may not both be null");
      } else {
         this.fullName = var1;
         this.reasonFlags = var2;
         this.crlIssuer = var3;
      }
   }

   public DistributionPoint(RDN var1, boolean[] var2, GeneralNames var3) {
      if (var1 == null && var3 == null) {
         throw new IllegalArgumentException("relativeName and crlIssuer may not both be null");
      } else {
         this.relativeName = var1;
         this.reasonFlags = var2;
         this.crlIssuer = var3;
      }
   }

   public DistributionPoint(DerValue var1) throws IOException {
      if (var1.tag != 48) {
         throw new IOException("Invalid encoding of DistributionPoint.");
      } else {
         while(true) {
            if (var1.data != null && var1.data.available() != 0) {
               DerValue var2 = var1.data.getDerValue();
               if (var2.isContextSpecific((byte)0) && var2.isConstructed()) {
                  if (this.fullName == null && this.relativeName == null) {
                     DerValue var3 = var2.data.getDerValue();
                     if (var3.isContextSpecific((byte)0) && var3.isConstructed()) {
                        var3.resetTag((byte)48);
                        this.fullName = new GeneralNames(var3);
                        continue;
                     }

                     if (var3.isContextSpecific((byte)1) && var3.isConstructed()) {
                        var3.resetTag((byte)49);
                        this.relativeName = new RDN(var3);
                        continue;
                     }

                     throw new IOException("Invalid DistributionPointName in DistributionPoint");
                  }

                  throw new IOException("Duplicate DistributionPointName in DistributionPoint.");
               }

               if (var2.isContextSpecific((byte)1) && !var2.isConstructed()) {
                  if (this.reasonFlags != null) {
                     throw new IOException("Duplicate Reasons in DistributionPoint.");
                  }

                  var2.resetTag((byte)3);
                  this.reasonFlags = var2.getUnalignedBitString().toBooleanArray();
                  continue;
               }

               if (var2.isContextSpecific((byte)2) && var2.isConstructed()) {
                  if (this.crlIssuer != null) {
                     throw new IOException("Duplicate CRLIssuer in DistributionPoint.");
                  }

                  var2.resetTag((byte)48);
                  this.crlIssuer = new GeneralNames(var2);
                  continue;
               }

               throw new IOException("Invalid encoding of DistributionPoint.");
            }

            if (this.crlIssuer == null && this.fullName == null && this.relativeName == null) {
               throw new IOException("One of fullName, relativeName,  and crlIssuer has to be set");
            }

            return;
         }
      }
   }

   public GeneralNames getFullName() {
      return this.fullName;
   }

   public RDN getRelativeName() {
      return this.relativeName;
   }

   public boolean[] getReasonFlags() {
      return this.reasonFlags;
   }

   public GeneralNames getCRLIssuer() {
      return this.crlIssuer;
   }

   public void encode(DerOutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      DerOutputStream var3;
      if (this.fullName != null || this.relativeName != null) {
         var3 = new DerOutputStream();
         DerOutputStream var4;
         if (this.fullName != null) {
            var4 = new DerOutputStream();
            this.fullName.encode(var4);
            var3.writeImplicit(DerValue.createTag((byte)-128, true, (byte)0), var4);
         } else if (this.relativeName != null) {
            var4 = new DerOutputStream();
            this.relativeName.encode(var4);
            var3.writeImplicit(DerValue.createTag((byte)-128, true, (byte)1), var4);
         }

         var2.write(DerValue.createTag((byte)-128, true, (byte)0), var3);
      }

      if (this.reasonFlags != null) {
         var3 = new DerOutputStream();
         BitArray var5 = new BitArray(this.reasonFlags);
         var3.putTruncatedUnalignedBitString(var5);
         var2.writeImplicit(DerValue.createTag((byte)-128, false, (byte)1), var3);
      }

      if (this.crlIssuer != null) {
         var3 = new DerOutputStream();
         this.crlIssuer.encode(var3);
         var2.writeImplicit(DerValue.createTag((byte)-128, true, (byte)2), var3);
      }

      var1.write((byte)48, (DerOutputStream)var2);
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof DistributionPoint)) {
         return false;
      } else {
         DistributionPoint var2 = (DistributionPoint)var1;
         boolean var3 = Objects.equals(this.fullName, var2.fullName) && Objects.equals(this.relativeName, var2.relativeName) && Objects.equals(this.crlIssuer, var2.crlIssuer) && Arrays.equals(this.reasonFlags, var2.reasonFlags);
         return var3;
      }
   }

   public int hashCode() {
      int var1 = this.hashCode;
      if (var1 == 0) {
         var1 = 1;
         if (this.fullName != null) {
            var1 += this.fullName.hashCode();
         }

         if (this.relativeName != null) {
            var1 += this.relativeName.hashCode();
         }

         if (this.crlIssuer != null) {
            var1 += this.crlIssuer.hashCode();
         }

         if (this.reasonFlags != null) {
            for(int var2 = 0; var2 < this.reasonFlags.length; ++var2) {
               if (this.reasonFlags[var2]) {
                  var1 += var2;
               }
            }
         }

         this.hashCode = var1;
      }

      return var1;
   }

   private static String reasonToString(int var0) {
      return var0 > 0 && var0 < REASON_STRINGS.length ? REASON_STRINGS[var0] : "Unknown reason " + var0;
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder();
      if (this.fullName != null) {
         var1.append("DistributionPoint:\n     " + this.fullName + "\n");
      }

      if (this.relativeName != null) {
         var1.append("DistributionPoint:\n     " + this.relativeName + "\n");
      }

      if (this.reasonFlags != null) {
         var1.append("   ReasonFlags:\n");

         for(int var2 = 0; var2 < this.reasonFlags.length; ++var2) {
            if (this.reasonFlags[var2]) {
               var1.append("    " + reasonToString(var2) + "\n");
            }
         }
      }

      if (this.crlIssuer != null) {
         var1.append("   CRLIssuer:" + this.crlIssuer + "\n");
      }

      return var1.toString();
   }
}
