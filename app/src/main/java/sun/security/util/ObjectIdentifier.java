package sun.security.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

public final class ObjectIdentifier implements Serializable {
   private byte[] encoding = null;
   private transient volatile String stringForm;
   private static final long serialVersionUID = 8697030238860181294L;
   private Object components = null;
   private int componentLen = -1;
   private transient boolean componentsCalculated = false;

   private void readObject(ObjectInputStream var1) throws IOException, ClassNotFoundException {
      var1.defaultReadObject();
      if (this.encoding == null) {
         int[] var2 = (int[])((int[])this.components);
         if (this.componentLen > var2.length) {
            this.componentLen = var2.length;
         }

         this.init(var2, this.componentLen);
      }

   }

   private void writeObject(ObjectOutputStream var1) throws IOException {
      if (!this.componentsCalculated) {
         int[] var2 = this.toIntArray();
         if (var2 != null) {
            this.components = var2;
            this.componentLen = var2.length;
         } else {
            this.components = HugeOidNotSupportedByOldJDK.theOne;
         }

         this.componentsCalculated = true;
      }

      var1.defaultWriteObject();
   }

   public ObjectIdentifier(String var1) throws IOException {
      byte var2 = 46;
      int var3 = 0;
      boolean var4 = false;
      int var5 = 0;
      byte[] var6 = new byte[var1.length()];
      int var7 = 0;
      int var9 = 0;

      try {
         String var10 = null;

         int var15;
         do {
            boolean var11 = false;
            var15 = var1.indexOf(var2, var3);
            int var16;
            if (var15 == -1) {
               var10 = var1.substring(var3);
               var16 = var1.length() - var3;
            } else {
               var10 = var1.substring(var3, var15);
               var16 = var15 - var3;
            }

            if (var16 > 9) {
               BigInteger var12 = new BigInteger(var10);
               if (var9 == 0) {
                  checkFirstComponent(var12);
                  var7 = var12.intValue();
               } else {
                  if (var9 == 1) {
                     checkSecondComponent(var7, var12);
                     var12 = var12.add(BigInteger.valueOf((long)(40 * var7)));
                  } else {
                     checkOtherComponent(var9, var12);
                  }

                  var5 += pack7Oid(var12, var6, var5);
               }
            } else {
               int var17 = Integer.parseInt(var10);
               if (var9 == 0) {
                  checkFirstComponent(var17);
                  var7 = var17;
               } else {
                  if (var9 == 1) {
                     checkSecondComponent(var7, var17);
                     var17 += 40 * var7;
                  } else {
                     checkOtherComponent(var9, var17);
                  }

                  var5 += pack7Oid(var17, var6, var5);
               }
            }

            var3 = var15 + 1;
            ++var9;
         } while(var15 != -1);

         checkCount(var9);
         this.encoding = new byte[var5];
         System.arraycopy(var6, 0, this.encoding, 0, var5);
         this.stringForm = var1;
      } catch (IOException var13) {
         throw var13;
      } catch (Exception var14) {
         throw new IOException("ObjectIdentifier() -- Invalid format: " + var14.toString(), var14);
      }
   }

   public ObjectIdentifier(int[] var1) throws IOException {
      checkCount(var1.length);
      checkFirstComponent(var1[0]);
      checkSecondComponent(var1[0], var1[1]);

      for(int var2 = 2; var2 < var1.length; ++var2) {
         checkOtherComponent(var2, var1[var2]);
      }

      this.init(var1, var1.length);
   }

   public ObjectIdentifier(DerInputStream var1) throws IOException {
      byte var2 = (byte)var1.getByte();
      if (var2 != 6) {
         throw new IOException("ObjectIdentifier() -- data isn't an object ID (tag = " + var2 + ")");
      } else {
         int var4 = var1.getLength();
         if (var4 > var1.available()) {
            throw new IOException("ObjectIdentifier() -- length exceedsdata available.  Length: " + var4 + ", Available: " + var1.available());
         } else {
            this.encoding = new byte[var4];
            var1.getBytes(this.encoding);
            check(this.encoding);
         }
      }
   }

   ObjectIdentifier(DerInputBuffer var1) throws IOException {
      DerInputStream var2 = new DerInputStream(var1);
      this.encoding = new byte[var2.available()];
      var2.getBytes(this.encoding);
      check(this.encoding);
   }

   private void init(int[] var1, int var2) {
      byte var3 = 0;
      byte[] var4 = new byte[var2 * 5 + 1];
      int var6;
      if (var1[1] < Integer.MAX_VALUE - var1[0] * 40) {
         var6 = var3 + pack7Oid(var1[0] * 40 + var1[1], var4, var3);
      } else {
         BigInteger var5 = BigInteger.valueOf((long)var1[1]);
         var5 = var5.add(BigInteger.valueOf((long)(var1[0] * 40)));
         var6 = var3 + pack7Oid(var5, var4, var3);
      }

      for(int var7 = 2; var7 < var2; ++var7) {
         var6 += pack7Oid(var1[var7], var4, var6);
      }

      this.encoding = new byte[var6];
      System.arraycopy(var4, 0, this.encoding, 0, var6);
   }

   public static ObjectIdentifier newInternal(int[] var0) {
      try {
         return new ObjectIdentifier(var0);
      } catch (IOException var2) {
         throw new RuntimeException(var2);
      }
   }

   void encode(DerOutputStream var1) throws IOException {
      var1.write((byte)6, (byte[])this.encoding);
   }

   /** @deprecated */
   @Deprecated
   public boolean equals(ObjectIdentifier var1) {
      return this.equals((Object)var1);
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof ObjectIdentifier)) {
         return false;
      } else {
         ObjectIdentifier var2 = (ObjectIdentifier)var1;
         return Arrays.equals(this.encoding, var2.encoding);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.encoding);
   }

   private int[] toIntArray() {
      int var1 = this.encoding.length;
      int[] var2 = new int[20];
      int var3 = 0;
      int var4 = 0;

      for(int var5 = 0; var5 < var1; ++var5) {
         if ((this.encoding[var5] & 128) == 0) {
            if (var5 - var4 + 1 > 4) {
               BigInteger var6 = new BigInteger(pack(this.encoding, var4, var5 - var4 + 1, 7, 8));
               if (var4 == 0) {
                  var2[var3++] = 2;
                  BigInteger var7 = var6.subtract(BigInteger.valueOf(80L));
                  if (var7.compareTo(BigInteger.valueOf(2147483647L)) == 1) {
                     return null;
                  }

                  var2[var3++] = var7.intValue();
               } else {
                  if (var6.compareTo(BigInteger.valueOf(2147483647L)) == 1) {
                     return null;
                  }

                  var2[var3++] = var6.intValue();
               }
            } else {
               int var9 = 0;

               for(int var10 = var4; var10 <= var5; ++var10) {
                  var9 <<= 7;
                  byte var8 = this.encoding[var10];
                  var9 |= var8 & 127;
               }

               if (var4 == 0) {
                  if (var9 < 80) {
                     var2[var3++] = var9 / 40;
                     var2[var3++] = var9 % 40;
                  } else {
                     var2[var3++] = 2;
                     var2[var3++] = var9 - 80;
                  }
               } else {
                  var2[var3++] = var9;
               }
            }

            var4 = var5 + 1;
         }

         if (var3 >= var2.length) {
            var2 = Arrays.copyOf(var2, var3 + 10);
         }
      }

      return Arrays.copyOf(var2, var3);
   }

   public String toString() {
      String var1 = this.stringForm;
      if (var1 == null) {
         int var2 = this.encoding.length;
         StringBuffer var3 = new StringBuffer(var2 * 4);
         int var4 = 0;

         for(int var5 = 0; var5 < var2; ++var5) {
            if ((this.encoding[var5] & 128) == 0) {
               if (var4 != 0) {
                  var3.append('.');
               }

               if (var5 - var4 + 1 > 4) {
                  BigInteger var6 = new BigInteger(pack(this.encoding, var4, var5 - var4 + 1, 7, 8));
                  if (var4 == 0) {
                     var3.append("2.");
                     var3.append((Object)var6.subtract(BigInteger.valueOf(80L)));
                  } else {
                     var3.append((Object)var6);
                  }
               } else {
                  int var9 = 0;

                  for(int var7 = var4; var7 <= var5; ++var7) {
                     var9 <<= 7;
                     byte var8 = this.encoding[var7];
                     var9 |= var8 & 127;
                  }

                  if (var4 == 0) {
                     if (var9 < 80) {
                        var3.append(var9 / 40);
                        var3.append('.');
                        var3.append(var9 % 40);
                     } else {
                        var3.append("2.");
                        var3.append(var9 - 80);
                     }
                  } else {
                     var3.append(var9);
                  }
               }

               var4 = var5 + 1;
            }
         }

         var1 = var3.toString();
         this.stringForm = var1;
      }

      return var1;
   }

   private static byte[] pack(byte[] var0, int var1, int var2, int var3, int var4) {
      assert var3 > 0 && var3 <= 8 : "input NUB must be between 1 and 8";

      assert var4 > 0 && var4 <= 8 : "output NUB must be between 1 and 8";

      if (var3 == var4) {
         return (byte[])var0.clone();
      } else {
         int var5 = var2 * var3;
         byte[] var6 = new byte[(var5 + var4 - 1) / var4];
         int var7 = 0;

         int var9;
         for(int var8 = (var5 + var4 - 1) / var4 * var4 - var5; var7 < var5; var8 += var9) {
            var9 = var3 - var7 % var3;
            if (var9 > var4 - var8 % var4) {
               var9 = var4 - var8 % var4;
            }

            var6[var8 / var4] = (byte)(var6[var8 / var4] | (var0[var1 + var7 / var3] + 256 >> var3 - var7 % var3 - var9 & (1 << var9) - 1) << var4 - var8 % var4 - var9);
            var7 += var9;
         }

         return var6;
      }
   }

   private static int pack7Oid(byte[] var0, int var1, int var2, byte[] var3, int var4) {
      byte[] var5 = pack(var0, var1, var2, 8, 7);
      int var6 = var5.length - 1;

      for(int var7 = var5.length - 2; var7 >= 0; --var7) {
         if (var5[var7] != 0) {
            var6 = var7;
         }

         var5[var7] = (byte)(var5[var7] | 128);
      }

      System.arraycopy(var5, var6, var3, var4, var5.length - var6);
      return var5.length - var6;
   }

   private static int pack8(byte[] var0, int var1, int var2, byte[] var3, int var4) {
      byte[] var5 = pack(var0, var1, var2, 7, 8);
      int var6 = var5.length - 1;

      for(int var7 = var5.length - 2; var7 >= 0; --var7) {
         if (var5[var7] != 0) {
            var6 = var7;
         }
      }

      System.arraycopy(var5, var6, var3, var4, var5.length - var6);
      return var5.length - var6;
   }

   private static int pack7Oid(int var0, byte[] var1, int var2) {
      byte[] var3 = new byte[]{(byte)(var0 >> 24), (byte)(var0 >> 16), (byte)(var0 >> 8), (byte)var0};
      return pack7Oid(var3, 0, 4, var1, var2);
   }

   private static int pack7Oid(BigInteger var0, byte[] var1, int var2) {
      byte[] var3 = var0.toByteArray();
      return pack7Oid(var3, 0, var3.length, var1, var2);
   }

   private static void check(byte[] var0) throws IOException {
      int var1 = var0.length;
      if (var1 >= 1 && (var0[var1 - 1] & 128) == 0) {
         for(int var2 = 0; var2 < var1; ++var2) {
            if (var0[var2] == -128 && (var2 == 0 || (var0[var2 - 1] & 128) == 0)) {
               throw new IOException("ObjectIdentifier() -- Invalid DER encoding, useless extra octet detected");
            }
         }

      } else {
         throw new IOException("ObjectIdentifier() -- Invalid DER encoding, not ended");
      }
   }

   private static void checkCount(int var0) throws IOException {
      if (var0 < 2) {
         throw new IOException("ObjectIdentifier() -- Must be at least two oid components ");
      }
   }

   private static void checkFirstComponent(int var0) throws IOException {
      if (var0 < 0 || var0 > 2) {
         throw new IOException("ObjectIdentifier() -- First oid component is invalid ");
      }
   }

   private static void checkFirstComponent(BigInteger var0) throws IOException {
      if (var0.signum() == -1 || var0.compareTo(BigInteger.valueOf(2L)) == 1) {
         throw new IOException("ObjectIdentifier() -- First oid component is invalid ");
      }
   }

   private static void checkSecondComponent(int var0, int var1) throws IOException {
      if (var1 < 0 || var0 != 2 && var1 > 39) {
         throw new IOException("ObjectIdentifier() -- Second oid component is invalid ");
      }
   }

   private static void checkSecondComponent(int var0, BigInteger var1) throws IOException {
      if (var1.signum() == -1 || var0 != 2 && var1.compareTo(BigInteger.valueOf(39L)) == 1) {
         throw new IOException("ObjectIdentifier() -- Second oid component is invalid ");
      }
   }

   private static void checkOtherComponent(int var0, int var1) throws IOException {
      if (var1 < 0) {
         throw new IOException("ObjectIdentifier() -- oid component #" + (var0 + 1) + " must be non-negative ");
      }
   }

   private static void checkOtherComponent(int var0, BigInteger var1) throws IOException {
      if (var1.signum() == -1) {
         throw new IOException("ObjectIdentifier() -- oid component #" + (var0 + 1) + " must be non-negative ");
      }
   }

   static class HugeOidNotSupportedByOldJDK implements Serializable {
      private static final long serialVersionUID = 1L;
      static HugeOidNotSupportedByOldJDK theOne = new HugeOidNotSupportedByOldJDK();
   }
}
