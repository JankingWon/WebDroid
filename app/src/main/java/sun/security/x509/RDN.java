package sun.security.x509;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class RDN {
   final AVA[] assertion;
   private volatile List<AVA> avaList;
   private volatile String canonicalString;

   public RDN(String var1) throws IOException {
      this(var1, Collections.emptyMap());
   }

   public RDN(String var1, Map<String, String> var2) throws IOException {
      int var3 = 0;
      int var4 = 0;
      int var5 = 0;
      ArrayList var6 = new ArrayList(3);

      String var8;
      AVA var9;
      for(int var7 = var1.indexOf(43); var7 >= 0; var7 = var1.indexOf(43, var4)) {
         var3 += X500Name.countQuotes(var1, var4, var7);
         if (var7 > 0 && var1.charAt(var7 - 1) != '\\' && var3 != 1) {
            var8 = var1.substring(var5, var7);
            if (var8.length() == 0) {
               throw new IOException("empty AVA in RDN \"" + var1 + "\"");
            }

            var9 = new AVA(new StringReader(var8), var2);
            var6.add(var9);
            var5 = var7 + 1;
            var3 = 0;
         }

         var4 = var7 + 1;
      }

      var8 = var1.substring(var5);
      if (var8.length() == 0) {
         throw new IOException("empty AVA in RDN \"" + var1 + "\"");
      } else {
         var9 = new AVA(new StringReader(var8), var2);
         var6.add(var9);
         this.assertion = (AVA[])var6.toArray(new AVA[var6.size()]);
      }
   }

   RDN(String var1, String var2) throws IOException {
      this(var1, var2, Collections.emptyMap());
   }

   RDN(String var1, String var2, Map<String, String> var3) throws IOException {
      if (!var2.equalsIgnoreCase("RFC2253")) {
         throw new IOException("Unsupported format " + var2);
      } else {
         boolean var4 = false;
         int var5 = 0;
         ArrayList var6 = new ArrayList(3);

         String var8;
         AVA var9;
         int var10;
         for(int var7 = var1.indexOf(43); var7 >= 0; var7 = var1.indexOf(43, var10)) {
            if (var7 > 0 && var1.charAt(var7 - 1) != '\\') {
               var8 = var1.substring(var5, var7);
               if (var8.length() == 0) {
                  throw new IOException("empty AVA in RDN \"" + var1 + "\"");
               }

               var9 = new AVA(new StringReader(var8), 3, var3);
               var6.add(var9);
               var5 = var7 + 1;
            }

            var10 = var7 + 1;
         }

         var8 = var1.substring(var5);
         if (var8.length() == 0) {
            throw new IOException("empty AVA in RDN \"" + var1 + "\"");
         } else {
            var9 = new AVA(new StringReader(var8), 3, var3);
            var6.add(var9);
            this.assertion = (AVA[])var6.toArray(new AVA[var6.size()]);
         }
      }
   }

   RDN(DerValue var1) throws IOException {
      if (var1.tag != 49) {
         throw new IOException("X500 RDN");
      } else {
         DerInputStream var2 = new DerInputStream(var1.toByteArray());
         DerValue[] var3 = var2.getSet(5);
         this.assertion = new AVA[var3.length];

         for(int var4 = 0; var4 < var3.length; ++var4) {
            this.assertion[var4] = new AVA(var3[var4]);
         }

      }
   }

   RDN(int var1) {
      this.assertion = new AVA[var1];
   }

   public RDN(AVA var1) {
      if (var1 == null) {
         throw new NullPointerException();
      } else {
         this.assertion = new AVA[]{var1};
      }
   }

   public RDN(AVA[] var1) {
      this.assertion = (AVA[])var1.clone();

      for(int var2 = 0; var2 < this.assertion.length; ++var2) {
         if (this.assertion[var2] == null) {
            throw new NullPointerException();
         }
      }

   }

   public List<AVA> avas() {
      List var1 = this.avaList;
      if (var1 == null) {
         var1 = Collections.unmodifiableList(Arrays.asList(this.assertion));
         this.avaList = var1;
      }

      return var1;
   }

   public int size() {
      return this.assertion.length;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof RDN)) {
         return false;
      } else {
         RDN var2 = (RDN)var1;
         if (this.assertion.length != var2.assertion.length) {
            return false;
         } else {
            String var3 = this.toRFC2253String(true);
            String var4 = var2.toRFC2253String(true);
            return var3.equals(var4);
         }
      }
   }

   public int hashCode() {
      return this.toRFC2253String(true).hashCode();
   }

   DerValue findAttribute(ObjectIdentifier var1) {
      for(int var2 = 0; var2 < this.assertion.length; ++var2) {
         if (this.assertion[var2].oid.equals((Object)var1)) {
            return this.assertion[var2].value;
         }
      }

      return null;
   }

   void encode(DerOutputStream var1) throws IOException {
      var1.putOrderedSetOf((byte)49, this.assertion);
   }

   public String toString() {
      if (this.assertion.length == 1) {
         return this.assertion[0].toString();
      } else {
         StringBuilder var1 = new StringBuilder();

         for(int var2 = 0; var2 < this.assertion.length; ++var2) {
            if (var2 != 0) {
               var1.append(" + ");
            }

            var1.append(this.assertion[var2].toString());
         }

         return var1.toString();
      }
   }

   public String toRFC1779String() {
      return this.toRFC1779String(Collections.emptyMap());
   }

   public String toRFC1779String(Map<String, String> var1) {
      if (this.assertion.length == 1) {
         return this.assertion[0].toRFC1779String(var1);
      } else {
         StringBuilder var2 = new StringBuilder();

         for(int var3 = 0; var3 < this.assertion.length; ++var3) {
            if (var3 != 0) {
               var2.append(" + ");
            }

            var2.append(this.assertion[var3].toRFC1779String(var1));
         }

         return var2.toString();
      }
   }

   public String toRFC2253String() {
      return this.toRFC2253StringInternal(false, Collections.emptyMap());
   }

   public String toRFC2253String(Map<String, String> var1) {
      return this.toRFC2253StringInternal(false, var1);
   }

   public String toRFC2253String(boolean var1) {
      if (!var1) {
         return this.toRFC2253StringInternal(false, Collections.emptyMap());
      } else {
         String var2 = this.canonicalString;
         if (var2 == null) {
            var2 = this.toRFC2253StringInternal(true, Collections.emptyMap());
            this.canonicalString = var2;
         }

         return var2;
      }
   }

   private String toRFC2253StringInternal(boolean var1, Map<String, String> var2) {
      if (this.assertion.length == 1) {
         return var1 ? this.assertion[0].toRFC2253CanonicalString() : this.assertion[0].toRFC2253String(var2);
      } else {
         AVA[] var3 = this.assertion;
         if (var1) {
            var3 = (AVA[])this.assertion.clone();
            Arrays.sort(var3, AVAComparator.getInstance());
         }

         StringJoiner var4 = new StringJoiner("+");
         AVA[] var5 = var3;
         int var6 = var3.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            AVA var8 = var5[var7];
            var4.add(var1 ? var8.toRFC2253CanonicalString() : var8.toRFC2253String(var2));
         }

         return var4.toString();
      }
   }
}
