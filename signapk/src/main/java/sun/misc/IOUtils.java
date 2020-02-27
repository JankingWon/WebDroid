package sun.misc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class IOUtils {
   private static final int DEFAULT_BUFFER_SIZE = 8192;
   private static final int MAX_BUFFER_SIZE = 2147483639;

   public static byte[] readExactlyNBytes(InputStream var0, int var1) throws IOException {
      byte[] var2 = readNBytesOrEOF(var0, var1);
      if (var2.length < var1) {
         throw new EOFException();
      } else {
         return var2;
      }
   }

   public static byte[] readNBytesOrEOF(InputStream var0, int var1) throws IOException {
      if (var1 < 0) {
         throw new IOException("length cannot be negative: " + var1);
      } else {
         ArrayList var2 = null;
         byte[] var3 = null;
         int var4 = 0;
         int var5 = var1;

         int var6;
         do {
            byte[] var7 = new byte[Math.min(var5, 8192)];

            int var8;
            for(var8 = 0; (var6 = var0.read(var7, var8, Math.min(var7.length - var8, var5))) > 0; var5 -= var6) {
               var8 += var6;
            }

            if (var8 > 0) {
               if (2147483639 - var4 < var8) {
                  throw new OutOfMemoryError("Required array size too large");
               }

               var4 += var8;
               if (var3 == null) {
                  var3 = var7;
               } else {
                  if (var2 == null) {
                     var2 = new ArrayList();
                     var2.add(var3);
                  }

                  var2.add(var7);
               }
            }
         } while(var6 >= 0 && var5 > 0);

         if (var2 == null) {
            if (var3 == null) {
               return new byte[0];
            } else {
               return var3.length == var4 ? var3 : Arrays.copyOf(var3, var4);
            }
         } else {
            var3 = new byte[var4];
            int var11 = 0;
            var5 = var4;

            int var10;
            for(Iterator var12 = var2.iterator(); var12.hasNext(); var5 -= var10) {
               byte[] var9 = (byte[])var12.next();
               var10 = Math.min(var9.length, var5);
               System.arraycopy(var9, 0, var3, var11, var10);
               var11 += var10;
            }

            return var3;
         }
      }
   }

   public static byte[] readFully(InputStream var0, int var1, boolean var2) throws IOException {
      byte[] var3 = new byte[0];
      if (var1 == -1) {
         var1 = Integer.MAX_VALUE;
      }

      int var6;
      for(int var4 = 0; var4 < var1; var4 += var6) {
         int var5;
         if (var4 >= var3.length) {
            var5 = Math.min(var1 - var4, var3.length + 1024);
            if (var3.length < var4 + var5) {
               var3 = Arrays.copyOf(var3, var4 + var5);
            }
         } else {
            var5 = var3.length - var4;
         }

         var6 = var0.read(var3, var4, var5);
         if (var6 < 0) {
            if (var2 && var1 != Integer.MAX_VALUE) {
               throw new EOFException("Detect premature EOF");
            }

            if (var3.length != var4) {
               var3 = Arrays.copyOf(var3, var4);
            }
            break;
         }
      }

      return var3;
   }

   public static byte[] readNBytes(InputStream var0, int var1) throws IOException {
      if (var1 < 0) {
         throw new IOException("length cannot be negative: " + var1);
      } else {
         return readFully(var0, var1, true);
      }
   }
}
