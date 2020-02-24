package sun.security.x509;

import java.util.Comparator;

class AVAComparator implements Comparator<AVA> {
   private static final Comparator<AVA> INSTANCE = new AVAComparator();

   private AVAComparator() {
   }

   static Comparator<AVA> getInstance() {
      return INSTANCE;
   }

   public int compare(AVA var1, AVA var2) {
      boolean var3 = var1.hasRFC2253Keyword();
      boolean var4 = var2.hasRFC2253Keyword();
      if (var3 == var4) {
         return var1.toRFC2253CanonicalString().compareTo(var2.toRFC2253CanonicalString());
      } else {
         return var3 ? -1 : 1;
      }
   }
}
