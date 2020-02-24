package sun.security.util;

import java.util.Comparator;

public class ByteArrayLexOrder implements Comparator<byte[]> {
   public final int compare(byte[] var1, byte[] var2) {
      for(int var4 = 0; var4 < var1.length && var4 < var2.length; ++var4) {
         int var3 = (var1[var4] & 255) - (var2[var4] & 255);
         if (var3 != 0) {
            return var3;
         }
      }

      return var1.length - var2.length;
   }
}
