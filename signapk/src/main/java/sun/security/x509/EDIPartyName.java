package sun.security.x509;

import java.io.IOException;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class EDIPartyName implements GeneralNameInterface {
   private static final byte TAG_ASSIGNER = 0;
   private static final byte TAG_PARTYNAME = 1;
   private String assigner = null;
   private String party = null;
   private int myhash = -1;

   public EDIPartyName(String var1, String var2) {
      this.assigner = var1;
      this.party = var2;
   }

   public EDIPartyName(String var1) {
      this.party = var1;
   }

   public EDIPartyName(DerValue var1) throws IOException {
      DerInputStream var2 = new DerInputStream(var1.toByteArray());
      DerValue[] var3 = var2.getSequence(2);
      int var4 = var3.length;
      if (var4 >= 1 && var4 <= 2) {
         for(int var5 = 0; var5 < var4; ++var5) {
            DerValue var6 = var3[var5];
            if (var6.isContextSpecific((byte)0) && !var6.isConstructed()) {
               if (this.assigner != null) {
                  throw new IOException("Duplicate nameAssigner found in EDIPartyName");
               }

               var6 = var6.data.getDerValue();
               this.assigner = var6.getAsString();
            }

            if (var6.isContextSpecific((byte)1) && !var6.isConstructed()) {
               if (this.party != null) {
                  throw new IOException("Duplicate partyName found in EDIPartyName");
               }

               var6 = var6.data.getDerValue();
               this.party = var6.getAsString();
            }
         }

      } else {
         throw new IOException("Invalid encoding of EDIPartyName");
      }
   }

   public int getType() {
      return 5;
   }

   public void encode(DerOutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      DerOutputStream var3 = new DerOutputStream();
      if (this.assigner != null) {
         DerOutputStream var4 = new DerOutputStream();
         var4.putPrintableString(this.assigner);
         var2.write(DerValue.createTag((byte)-128, false, (byte)0), var4);
      }

      if (this.party == null) {
         throw new IOException("Cannot have null partyName");
      } else {
         var3.putPrintableString(this.party);
         var2.write(DerValue.createTag((byte)-128, false, (byte)1), var3);
         var1.write((byte)48, (DerOutputStream)var2);
      }
   }

   public String getAssignerName() {
      return this.assigner;
   }

   public String getPartyName() {
      return this.party;
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof EDIPartyName)) {
         return false;
      } else {
         String var2 = ((EDIPartyName)var1).assigner;
         if (this.assigner == null) {
            if (var2 != null) {
               return false;
            }
         } else if (!this.assigner.equals(var2)) {
            return false;
         }

         String var3 = ((EDIPartyName)var1).party;
         if (this.party == null) {
            if (var3 != null) {
               return false;
            }
         } else if (!this.party.equals(var3)) {
            return false;
         }

         return true;
      }
   }

   public int hashCode() {
      if (this.myhash == -1) {
         this.myhash = 37 + (this.party == null ? 1 : this.party.hashCode());
         if (this.assigner != null) {
            this.myhash = 37 * this.myhash + this.assigner.hashCode();
         }
      }

      return this.myhash;
   }

   public String toString() {
      return "EDIPartyName: " + (this.assigner == null ? "" : "  nameAssigner = " + this.assigner + ",") + "  partyName = " + this.party;
   }

   public int constrains(GeneralNameInterface var1) throws UnsupportedOperationException {
      byte var2;
      if (var1 == null) {
         var2 = -1;
      } else {
         if (var1.getType() == 5) {
            throw new UnsupportedOperationException("Narrowing, widening, and matching of names not supported for EDIPartyName");
         }

         var2 = -1;
      }

      return var2;
   }

   public int subtreeDepth() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("subtreeDepth() not supported for EDIPartyName");
   }
}
