package sun.security.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Pem {
   public static byte[] decode(String var0) throws IOException {
      byte[] var1 = var0.replaceAll("\\s+", "").getBytes(StandardCharsets.ISO_8859_1);

      try {
         return Base64.getDecoder().decode(var1);
      } catch (IllegalArgumentException var3) {
         throw new IOException(var3);
      }
   }
}
