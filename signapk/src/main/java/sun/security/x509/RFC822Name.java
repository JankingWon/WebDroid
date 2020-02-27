package sun.security.x509;

import java.io.IOException;
import java.util.Locale;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class RFC822Name implements GeneralNameInterface {
   private String name;

   public RFC822Name(DerValue var1) throws IOException {
      this.name = var1.getIA5String();
      this.parseName(this.name);
   }

   public RFC822Name(String var1) throws IOException {
      this.parseName(var1);
      this.name = var1;
   }

   public void parseName(String var1) throws IOException {
      if (var1 != null && var1.length() != 0) {
         String var2 = var1.substring(var1.indexOf(64) + 1);
         if (var2.length() == 0) {
            throw new IOException("RFC822Name may not end with @");
         } else if (var2.startsWith(".") && var2.length() == 1) {
            throw new IOException("RFC822Name domain may not be just .");
         }
      } else {
         throw new IOException("RFC822Name may not be null or empty");
      }
   }

   public int getType() {
      return 1;
   }

   public String getName() {
      return this.name;
   }

   public void encode(DerOutputStream var1) throws IOException {
      var1.putIA5String(this.name);
   }

   public String toString() {
      return "RFC822Name: " + this.name;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof RFC822Name)) {
         return false;
      } else {
         RFC822Name var2 = (RFC822Name)var1;
         return this.name.equalsIgnoreCase(var2.name);
      }
   }

   public int hashCode() {
      return this.name.toUpperCase(Locale.ENGLISH).hashCode();
   }

   public int constrains(GeneralNameInterface var1) throws UnsupportedOperationException {
      byte var2;
      if (var1 == null) {
         var2 = -1;
      } else if (var1.getType() != 1) {
         var2 = -1;
      } else {
         String var3 = ((RFC822Name)var1).getName().toLowerCase(Locale.ENGLISH);
         String var4 = this.name.toLowerCase(Locale.ENGLISH);
         if (var3.equals(var4)) {
            var2 = 0;
         } else {
            int var5;
            if (var4.endsWith(var3)) {
               if (var3.indexOf(64) != -1) {
                  var2 = 3;
               } else if (var3.startsWith(".")) {
                  var2 = 2;
               } else {
                  var5 = var4.lastIndexOf(var3);
                  if (var4.charAt(var5 - 1) == '@') {
                     var2 = 2;
                  } else {
                     var2 = 3;
                  }
               }
            } else if (var3.endsWith(var4)) {
               if (var4.indexOf(64) != -1) {
                  var2 = 3;
               } else if (var4.startsWith(".")) {
                  var2 = 1;
               } else {
                  var5 = var3.lastIndexOf(var4);
                  if (var3.charAt(var5 - 1) == '@') {
                     var2 = 1;
                  } else {
                     var2 = 3;
                  }
               }
            } else {
               var2 = 3;
            }
         }
      }

      return var2;
   }

   public int subtreeDepth() throws UnsupportedOperationException {
      String var1 = this.name;
      int var2 = 1;
      int var3 = var1.lastIndexOf(64);
      if (var3 >= 0) {
         ++var2;
         var1 = var1.substring(var3 + 1);
      }

      while(var1.lastIndexOf(46) >= 0) {
         var1 = var1.substring(0, var1.lastIndexOf(46));
         ++var2;
      }

      return var2;
   }
}
