package sun.security.util;

import java.security.AccessController;
import java.security.AlgorithmConstraints;
import java.security.PrivilegedAction;
import java.security.Security;
import java.util.Iterator;
import java.util.Set;

public abstract class AbstractAlgorithmConstraints implements AlgorithmConstraints {
   protected final AlgorithmDecomposer decomposer;

   protected AbstractAlgorithmConstraints(AlgorithmDecomposer var1) {
      this.decomposer = var1;
   }

   static String[] getAlgorithms(final String var0) {
      String var1 = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
         public String run() {
            return Security.getProperty(var0);
         }
      });
      String[] var2 = null;
      if (var1 != null && !var1.isEmpty()) {
         if (var1.length() >= 2 && var1.charAt(0) == '"' && var1.charAt(var1.length() - 1) == '"') {
            var1 = var1.substring(1, var1.length() - 1);
         }

         var2 = var1.split(",");

         for(int var3 = 0; var3 < var2.length; ++var3) {
            var2[var3] = var2[var3].trim();
         }
      }

      if (var2 == null) {
         var2 = new String[0];
      }

      return var2;
   }

   static boolean checkAlgorithm(String[] var0, String var1, AlgorithmDecomposer var2) {
      if (var1 != null && var1.length() != 0) {
         Set var3 = null;
         String[] var4 = var0;
         int var5 = var0.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String var7 = var4[var6];
            if (var7 != null && !var7.isEmpty()) {
               if (var7.equalsIgnoreCase(var1)) {
                  return false;
               }

               if (var3 == null) {
                  var3 = var2.decompose(var1);
               }

               Iterator var8 = var3.iterator();

               while(var8.hasNext()) {
                  String var9 = (String)var8.next();
                  if (var7.equalsIgnoreCase(var9)) {
                     return false;
                  }
               }
            }
         }

         return true;
      } else {
         throw new IllegalArgumentException("No algorithm name specified");
      }
   }
}
