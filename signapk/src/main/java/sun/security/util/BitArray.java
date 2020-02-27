package sun.security.util;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class BitArray {
   private byte[] repn;
   private int length;
   private static final int BITS_PER_UNIT = 8;
   private static final byte[][] NYBBLE = new byte[][]{{48, 48, 48, 48}, {48, 48, 48, 49}, {48, 48, 49, 48}, {48, 48, 49, 49}, {48, 49, 48, 48}, {48, 49, 48, 49}, {48, 49, 49, 48}, {48, 49, 49, 49}, {49, 48, 48, 48}, {49, 48, 48, 49}, {49, 48, 49, 48}, {49, 48, 49, 49}, {49, 49, 48, 48}, {49, 49, 48, 49}, {49, 49, 49, 48}, {49, 49, 49, 49}};
   private static final int BYTES_PER_LINE = 8;

   private static int subscript(int var0) {
      return var0 / 8;
   }

   private static int position(int var0) {
      return 1 << 7 - var0 % 8;
   }

   public BitArray(int var1) throws IllegalArgumentException {
      if (var1 < 0) {
         throw new IllegalArgumentException("Negative length for BitArray");
      } else {
         this.length = var1;
         this.repn = new byte[(var1 + 8 - 1) / 8];
      }
   }

   public BitArray(int var1, byte[] var2) throws IllegalArgumentException {
      if (var1 < 0) {
         throw new IllegalArgumentException("Negative length for BitArray");
      } else if (var2.length * 8 < var1) {
         throw new IllegalArgumentException("Byte array too short to represent bit array of given length");
      } else {
         this.length = var1;
         int var3 = (var1 + 8 - 1) / 8;
         int var4 = var3 * 8 - var1;
         byte var5 = (byte)(255 << var4);
         this.repn = new byte[var3];
         System.arraycopy(var2, 0, this.repn, 0, var3);
         if (var3 > 0) {
            byte[] var10000 = this.repn;
            var10000[var3 - 1] &= var5;
         }

      }
   }

   public BitArray(boolean[] var1) {
      this.length = var1.length;
      this.repn = new byte[(this.length + 7) / 8];

      for(int var2 = 0; var2 < this.length; ++var2) {
         this.set(var2, var1[var2]);
      }

   }

   private BitArray(BitArray var1) {
      this.length = var1.length;
      this.repn = (byte[])var1.repn.clone();
   }

   public boolean get(int var1) throws ArrayIndexOutOfBoundsException {
      if (var1 >= 0 && var1 < this.length) {
         return (this.repn[subscript(var1)] & position(var1)) != 0;
      } else {
         throw new ArrayIndexOutOfBoundsException(Integer.toString(var1));
      }
   }

   public void set(int var1, boolean var2) throws ArrayIndexOutOfBoundsException {
      if (var1 >= 0 && var1 < this.length) {
         int var3 = subscript(var1);
         int var4 = position(var1);
         byte[] var10000;
         if (var2) {
            var10000 = this.repn;
            var10000[var3] = (byte)(var10000[var3] | var4);
         } else {
            var10000 = this.repn;
            var10000[var3] = (byte)(var10000[var3] & ~var4);
         }

      } else {
         throw new ArrayIndexOutOfBoundsException(Integer.toString(var1));
      }
   }

   public int length() {
      return this.length;
   }

   public byte[] toByteArray() {
      return (byte[])this.repn.clone();
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (var1 != null && var1 instanceof BitArray) {
         BitArray var2 = (BitArray)var1;
         if (var2.length != this.length) {
            return false;
         } else {
            for(int var3 = 0; var3 < this.repn.length; ++var3) {
               if (this.repn[var3] != var2.repn[var3]) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean[] toBooleanArray() {
      boolean[] var1 = new boolean[this.length];

      for(int var2 = 0; var2 < this.length; ++var2) {
         var1[var2] = this.get(var2);
      }

      return var1;
   }

   public int hashCode() {
      int var1 = 0;

      for(int var2 = 0; var2 < this.repn.length; ++var2) {
         var1 = 31 * var1 + this.repn[var2];
      }

      return var1 ^ this.length;
   }

   public Object clone() {
      return new BitArray(this);
   }

   public String toString() {
      ByteArrayOutputStream var1 = new ByteArrayOutputStream();

      int var2;
      for(var2 = 0; var2 < this.repn.length - 1; ++var2) {
         var1.write(NYBBLE[this.repn[var2] >> 4 & 15], 0, 4);
         var1.write(NYBBLE[this.repn[var2] & 15], 0, 4);
         if (var2 % 8 == 7) {
            var1.write(10);
         } else {
            var1.write(32);
         }
      }

      for(var2 = 8 * (this.repn.length - 1); var2 < this.length; ++var2) {
         var1.write(this.get(var2) ? 49 : 48);
      }

      return new String(var1.toByteArray());
   }

   public BitArray truncate() {
      for(int var1 = this.length - 1; var1 >= 0; --var1) {
         if (this.get(var1)) {
            return new BitArray(var1 + 1, Arrays.copyOf(this.repn, (var1 + 8) / 8));
         }
      }

      return new BitArray(1);
   }
}
