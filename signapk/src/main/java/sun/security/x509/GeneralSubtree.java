package sun.security.x509;

import java.io.IOException;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class GeneralSubtree {
   private static final byte TAG_MIN = 0;
   private static final byte TAG_MAX = 1;
   private static final int MIN_DEFAULT = 0;
   private GeneralName name;
   private int minimum = 0;
   private int maximum = -1;
   private int myhash = -1;

   public GeneralSubtree(GeneralName var1, int var2, int var3) {
      this.name = var1;
      this.minimum = var2;
      this.maximum = var3;
   }

   public GeneralSubtree(DerValue var1) throws IOException {
      if (var1.tag != 48) {
         throw new IOException("Invalid encoding for GeneralSubtree.");
      } else {
         this.name = new GeneralName(var1.data.getDerValue(), true);

         while(true) {
            while(var1.data.available() != 0) {
               DerValue var2 = var1.data.getDerValue();
               if (!var2.isContextSpecific((byte)0) || var2.isConstructed()) {
                  if (!var2.isContextSpecific((byte)1) || var2.isConstructed()) {
                     throw new IOException("Invalid encoding of GeneralSubtree.");
                  }

                  var2.resetTag((byte)2);
                  this.maximum = var2.getInteger();
               } else {
                  var2.resetTag((byte)2);
                  this.minimum = var2.getInteger();
               }
            }

            return;
         }
      }
   }

   public GeneralName getName() {
      return this.name;
   }

   public int getMinimum() {
      return this.minimum;
   }

   public int getMaximum() {
      return this.maximum;
   }

   public String toString() {
      String var1 = "\n   GeneralSubtree: [\n    GeneralName: " + (this.name == null ? "" : this.name.toString()) + "\n    Minimum: " + this.minimum;
      if (this.maximum == -1) {
         var1 = var1 + "\t    Maximum: undefined";
      } else {
         var1 = var1 + "\t    Maximum: " + this.maximum;
      }

      var1 = var1 + "    ]\n";
      return var1;
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof GeneralSubtree)) {
         return false;
      } else {
         GeneralSubtree var2 = (GeneralSubtree)var1;
         if (this.name == null) {
            if (var2.name != null) {
               return false;
            }
         } else if (!this.name.equals(var2.name)) {
            return false;
         }

         if (this.minimum != var2.minimum) {
            return false;
         } else {
            return this.maximum == var2.maximum;
         }
      }
   }

   public int hashCode() {
      if (this.myhash == -1) {
         this.myhash = 17;
         if (this.name != null) {
            this.myhash = 37 * this.myhash + this.name.hashCode();
         }

         if (this.minimum != 0) {
            this.myhash = 37 * this.myhash + this.minimum;
         }

         if (this.maximum != -1) {
            this.myhash = 37 * this.myhash + this.maximum;
         }
      }

      return this.myhash;
   }

   public void encode(DerOutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      this.name.encode(var2);
      DerOutputStream var3;
      if (this.minimum != 0) {
         var3 = new DerOutputStream();
         var3.putInteger(this.minimum);
         var2.writeImplicit(DerValue.createTag((byte)-128, false, (byte)0), var3);
      }

      if (this.maximum != -1) {
         var3 = new DerOutputStream();
         var3.putInteger(this.maximum);
         var2.writeImplicit(DerValue.createTag((byte)-128, false, (byte)1), var3);
      }

      var1.write((byte)48, (DerOutputStream)var2);
   }
}
