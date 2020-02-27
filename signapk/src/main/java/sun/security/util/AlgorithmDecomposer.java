package sun.security.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class AlgorithmDecomposer {
   private static final Pattern transPattern = Pattern.compile("/");
   private static final Pattern pattern = Pattern.compile("with|and", 2);

   private static Set<String> decomposeImpl(String var0) {
      String[] var1 = transPattern.split(var0);
      HashSet var2 = new HashSet();
      String[] var3 = var1;
      int var4 = var1.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String var6 = var3[var5];
         if (var6 != null && var6.length() != 0) {
            String[] var7 = pattern.split(var6);
            String[] var8 = var7;
            int var9 = var7.length;

            for(int var10 = 0; var10 < var9; ++var10) {
               String var11 = var8[var10];
               if (var11 != null && var11.length() != 0) {
                  var2.add(var11);
               }
            }
         }
      }

      return var2;
   }

   public Set<String> decompose(String var1) {
      if (var1 != null && var1.length() != 0) {
         Set var2 = decomposeImpl(var1);
         if (var2.contains("SHA1") && !var2.contains("SHA-1")) {
            var2.add("SHA-1");
         }

         if (var2.contains("SHA-1") && !var2.contains("SHA1")) {
            var2.add("SHA1");
         }

         if (var2.contains("SHA224") && !var2.contains("SHA-224")) {
            var2.add("SHA-224");
         }

         if (var2.contains("SHA-224") && !var2.contains("SHA224")) {
            var2.add("SHA224");
         }

         if (var2.contains("SHA256") && !var2.contains("SHA-256")) {
            var2.add("SHA-256");
         }

         if (var2.contains("SHA-256") && !var2.contains("SHA256")) {
            var2.add("SHA256");
         }

         if (var2.contains("SHA384") && !var2.contains("SHA-384")) {
            var2.add("SHA-384");
         }

         if (var2.contains("SHA-384") && !var2.contains("SHA384")) {
            var2.add("SHA384");
         }

         if (var2.contains("SHA512") && !var2.contains("SHA-512")) {
            var2.add("SHA-512");
         }

         if (var2.contains("SHA-512") && !var2.contains("SHA512")) {
            var2.add("SHA512");
         }

         return var2;
      } else {
         return new HashSet();
      }
   }

   public static Collection<String> getAliases(String var0) {
      String[] var1;
      if (!var0.equalsIgnoreCase("DH") && !var0.equalsIgnoreCase("DiffieHellman")) {
         var1 = new String[]{var0};
      } else {
         var1 = new String[]{"DH", "DiffieHellman"};
      }

      return Arrays.asList(var1);
   }

   private static void hasLoop(Set<String> var0, String var1, String var2) {
      if (var0.contains(var1)) {
         if (!var0.contains(var2)) {
            var0.add(var2);
         }

         var0.remove(var1);
      }

   }

   public static Set<String> decomposeOneHash(String var0) {
      if (var0 != null && var0.length() != 0) {
         Set var1 = decomposeImpl(var0);
         hasLoop(var1, "SHA-1", "SHA1");
         hasLoop(var1, "SHA-224", "SHA224");
         hasLoop(var1, "SHA-256", "SHA256");
         hasLoop(var1, "SHA-384", "SHA384");
         hasLoop(var1, "SHA-512", "SHA512");
         return var1;
      } else {
         return new HashSet();
      }
   }

   public static String hashName(String var0) {
      return var0.replace("-", "");
   }
}
