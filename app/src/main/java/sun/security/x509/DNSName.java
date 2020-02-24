package sun.security.x509;

import java.io.IOException;
import java.util.Locale;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class DNSName implements GeneralNameInterface {
   private String name;
   private static final String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
   private static final String digitsAndHyphen = "0123456789-";
   private static final String alphaDigitsAndHyphen = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-";

   public DNSName(DerValue var1) throws IOException {
      this.name = var1.getIA5String();
   }

   public DNSName(String var1) throws IOException {
      this(var1, false);
   }

   public DNSName(String var1, boolean var2) throws IOException {
      if (var1 != null && var1.length() != 0) {
         if (var1.indexOf(32) != -1) {
            throw new IOException("DNS names or NameConstraints with blank components are not permitted");
         } else if (var1.charAt(0) != '.' && var1.charAt(var1.length() - 1) != '.') {
            int var3;
            for(int var4 = 0; var4 < var1.length(); var4 = var3 + 1) {
               var3 = var1.indexOf(46, var4);
               if (var3 < 0) {
                  var3 = var1.length();
               }

               if (var3 - var4 < 1) {
                  throw new IOException("DNSName SubjectAltNames with empty components are not permitted");
               }

               if (!var2) {
                  if ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".indexOf(var1.charAt(var4)) < 0) {
                     throw new IOException("DNSName components must begin with a letter");
                  }
               } else {
                  char var5 = var1.charAt(var4);
                  if ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".indexOf(var5) < 0 && !Character.isDigit(var5)) {
                     throw new IOException("DNSName components must begin with a letter or digit");
                  }
               }

               for(int var7 = var4 + 1; var7 < var3; ++var7) {
                  char var6 = var1.charAt(var7);
                  if ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-".indexOf(var6) < 0) {
                     throw new IOException("DNSName components must consist of letters, digits, and hyphens");
                  }
               }
            }

            this.name = var1;
         } else {
            throw new IOException("DNS names or NameConstraints may not begin or end with a .");
         }
      } else {
         throw new IOException("DNS name must not be null");
      }
   }

   public int getType() {
      return 2;
   }

   public String getName() {
      return this.name;
   }

   public void encode(DerOutputStream var1) throws IOException {
      var1.putIA5String(this.name);
   }

   public String toString() {
      return "DNSName: " + this.name;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof DNSName)) {
         return false;
      } else {
         DNSName var2 = (DNSName)var1;
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
      } else if (var1.getType() != 2) {
         var2 = -1;
      } else {
         String var3 = ((DNSName)var1).getName().toLowerCase(Locale.ENGLISH);
         String var4 = this.name.toLowerCase(Locale.ENGLISH);
         if (var3.equals(var4)) {
            var2 = 0;
         } else {
            int var5;
            if (var4.endsWith(var3)) {
               var5 = var4.lastIndexOf(var3);
               if (var4.charAt(var5 - 1) == '.') {
                  var2 = 2;
               } else {
                  var2 = 3;
               }
            } else if (var3.endsWith(var4)) {
               var5 = var3.lastIndexOf(var4);
               if (var3.charAt(var5 - 1) == '.') {
                  var2 = 1;
               } else {
                  var2 = 3;
               }
            } else {
               var2 = 3;
            }
         }
      }

      return var2;
   }

   public int subtreeDepth() throws UnsupportedOperationException {
      int var1 = 1;

      for(int var2 = this.name.indexOf(46); var2 >= 0; var2 = this.name.indexOf(46, var2 + 1)) {
         ++var1;
      }

      return var1;
   }
}
