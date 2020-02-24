package sun.security.x509;

public class X509AttributeName {
   private static final char SEPARATOR = '.';
   private String prefix = null;
   private String suffix = null;

   public X509AttributeName(String var1) {
      int var2 = var1.indexOf(46);
      if (var2 < 0) {
         this.prefix = var1;
      } else {
         this.prefix = var1.substring(0, var2);
         this.suffix = var1.substring(var2 + 1);
      }

   }

   public String getPrefix() {
      return this.prefix;
   }

   public String getSuffix() {
      return this.suffix;
   }
}
