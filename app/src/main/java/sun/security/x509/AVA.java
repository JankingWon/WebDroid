package sun.security.x509;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sun.security.action.GetBooleanAction;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class AVA implements DerEncoder {
   private static final Debug debug = Debug.getInstance("x509", "\t[AVA]");
   private static final boolean PRESERVE_OLD_DC_ENCODING = (Boolean) AccessController.doPrivileged((PrivilegedAction)(new GetBooleanAction("com.sun.security.preserveOldDCEncoding")));
   static final int DEFAULT = 1;
   static final int RFC1779 = 2;
   static final int RFC2253 = 3;
   final ObjectIdentifier oid;
   final DerValue value;
   private static final String specialChars1779 = ",=\n+<>#;\\\"";
   private static final String specialChars2253 = ",=+<>#;\\\"";
   private static final String specialCharsDefault = ",=\n+<>#;\\\" ";
   private static final String escapedDefault = ",+<>;\"";
   private static final String hexDigits = "0123456789ABCDEF";

   public AVA(ObjectIdentifier var1, DerValue var2) {
      if (var1 != null && var2 != null) {
         this.oid = var1;
         this.value = var2;
      } else {
         throw new NullPointerException();
      }
   }

   AVA(Reader var1) throws IOException {
      this(var1, 1);
   }

   AVA(Reader var1, Map<String, String> var2) throws IOException {
      this(var1, 1, var2);
   }

   AVA(Reader var1, int var2) throws IOException {
      this(var1, var2, Collections.emptyMap());
   }

   AVA(Reader var1, int var2, Map<String, String> var3) throws IOException {
      StringBuilder var4 = new StringBuilder();

      while(true) {
         int var5 = readChar(var1, "Incorrect AVA format");
         if (var5 == 61) {
            this.oid = AVAKeyword.getOID(var4.toString(), var2, var3);
            var4.setLength(0);
            if (var2 == 3) {
               var5 = var1.read();
               if (var5 == 32) {
                  throw new IOException("Incorrect AVA RFC2253 format - leading space must be escaped");
               }
            } else {
               do {
                  do {
                     var5 = var1.read();
                  } while(var5 == 32);
               } while(var5 == 10);
            }

            if (var5 == -1) {
               this.value = new DerValue("");
               return;
            } else {
               if (var5 == 35) {
                  this.value = parseHexString(var1, var2);
               } else if (var5 == 34 && var2 != 3) {
                  this.value = this.parseQuotedString(var1, var4);
               } else {
                  this.value = this.parseString(var1, var5, var2, var4);
               }

               return;
            }
         }

         var4.append((char)var5);
      }
   }

   public ObjectIdentifier getObjectIdentifier() {
      return this.oid;
   }

   public DerValue getDerValue() {
      return this.value;
   }

   public String getValueString() {
      try {
         String var1 = this.value.getAsString();
         if (var1 == null) {
            throw new RuntimeException("AVA string is null");
         } else {
            return var1;
         }
      } catch (IOException var2) {
         throw new RuntimeException("AVA error: " + var2, var2);
      }
   }

   private static DerValue parseHexString(Reader var0, int var1) throws IOException {
      ByteArrayOutputStream var3 = new ByteArrayOutputStream();
      byte var4 = 0;
      int var5 = 0;

      while(true) {
         int var2 = var0.read();
         if (isTerminator(var2, var1)) {
            if (var5 == 0) {
               throw new IOException("AVA parse, zero hex digits");
            } else if (var5 % 2 == 1) {
               throw new IOException("AVA parse, odd number of hex digits");
            } else {
               return new DerValue(var3.toByteArray());
            }
         }

         int var6 = "0123456789ABCDEF".indexOf(Character.toUpperCase((char)var2));
         if (var6 == -1) {
            throw new IOException("AVA parse, invalid hex digit: " + (char)var2);
         }

         if (var5 % 2 == 1) {
            var4 = (byte)(var4 * 16 + (byte)var6);
            var3.write(var4);
         } else {
            var4 = (byte)var6;
         }

         ++var5;
      }
   }

   private DerValue parseQuotedString(Reader var1, StringBuilder var2) throws IOException {
      int var3 = readChar(var1, "Quoted string did not end in quote");
      ArrayList var4 = new ArrayList();
      boolean var5 = true;

      while(true) {
         String var7;
         while(var3 != 34) {
            if (var3 == 92) {
               var3 = readChar(var1, "Quoted string did not end in quote");
               Byte var6 = null;
               if ((var6 = getEmbeddedHexPair(var3, var1)) != null) {
                  var5 = false;
                  var4.add(var6);
                  var3 = var1.read();
                  continue;
               }

               if (",=\n+<>#;\\\"".indexOf((char)var3) < 0) {
                  throw new IOException("Invalid escaped character in AVA: " + (char)var3);
               }
            }

            if (var4.size() > 0) {
               var7 = getEmbeddedHexString(var4);
               var2.append(var7);
               var4.clear();
            }

            var5 &= DerValue.isPrintableStringChar((char)var3);
            var2.append((char)var3);
            var3 = readChar(var1, "Quoted string did not end in quote");
         }

         if (var4.size() > 0) {
            var7 = getEmbeddedHexString(var4);
            var2.append(var7);
            var4.clear();
         }

         do {
            do {
               var3 = var1.read();
            } while(var3 == 10);
         } while(var3 == 32);

         if (var3 != -1) {
            throw new IOException("AVA had characters other than whitespace after terminating quote");
         }

         if (!this.oid.equals((Object) PKCS9Attribute.EMAIL_ADDRESS_OID) && (!this.oid.equals((Object) X500Name.DOMAIN_COMPONENT_OID) || PRESERVE_OLD_DC_ENCODING)) {
            if (var5) {
               return new DerValue(var2.toString().trim());
            }

            return new DerValue((byte)12, var2.toString().trim());
         }

         return new DerValue((byte)22, var2.toString().trim());
      }
   }

   private DerValue parseString(Reader var1, int var2, int var3, StringBuilder var4) throws IOException {
      ArrayList var5 = new ArrayList();
      boolean var6 = true;
      boolean var7 = false;
      boolean var8 = true;
      int var9 = 0;

      String var12;
      do {
         var7 = false;
         if (var2 == 92) {
            var7 = true;
            var2 = readChar(var1, "Invalid trailing backslash");
            Byte var10 = null;
            if ((var10 = getEmbeddedHexPair(var2, var1)) != null) {
               var6 = false;
               var5.add(var10);
               var2 = var1.read();
               var8 = false;
               continue;
            }

            if (var3 == 1 && ",=\n+<>#;\\\" ".indexOf((char)var2) == -1) {
               throw new IOException("Invalid escaped character in AVA: '" + (char)var2 + "'");
            }

            if (var3 == 3) {
               if (var2 == 32) {
                  if (!var8 && !trailingSpace(var1)) {
                     throw new IOException("Invalid escaped space character in AVA.  Only a leading or trailing space character can be escaped.");
                  }
               } else if (var2 == 35) {
                  if (!var8) {
                     throw new IOException("Invalid escaped '#' character in AVA.  Only a leading '#' can be escaped.");
                  }
               } else if (",=+<>#;\\\"".indexOf((char)var2) == -1) {
                  throw new IOException("Invalid escaped character in AVA: '" + (char)var2 + "'");
               }
            }
         } else if (var3 == 3) {
            if (",=+<>#;\\\"".indexOf((char)var2) != -1) {
               throw new IOException("Character '" + (char)var2 + "' in AVA appears without escape");
            }
         } else if (",+<>;\"".indexOf((char)var2) != -1) {
            throw new IOException("Character '" + (char)var2 + "' in AVA appears without escape");
         }

         int var11;
         if (var5.size() > 0) {
            for(var11 = 0; var11 < var9; ++var11) {
               var4.append(" ");
            }

            var9 = 0;
            var12 = getEmbeddedHexString(var5);
            var4.append(var12);
            var5.clear();
         }

         var6 &= DerValue.isPrintableStringChar((char)var2);
         if (var2 == 32 && !var7) {
            ++var9;
         } else {
            for(var11 = 0; var11 < var9; ++var11) {
               var4.append(" ");
            }

            var9 = 0;
            var4.append((char)var2);
         }

         var2 = var1.read();
         var8 = false;
      } while(!isTerminator(var2, var3));

      if (var3 == 3 && var9 > 0) {
         throw new IOException("Incorrect AVA RFC2253 format - trailing space must be escaped");
      } else {
         if (var5.size() > 0) {
            var12 = getEmbeddedHexString(var5);
            var4.append(var12);
            var5.clear();
         }

         if (this.oid.equals((Object) PKCS9Attribute.EMAIL_ADDRESS_OID) || this.oid.equals((Object) X500Name.DOMAIN_COMPONENT_OID) && !PRESERVE_OLD_DC_ENCODING) {
            return new DerValue((byte)22, var4.toString());
         } else if (var6) {
            return new DerValue(var4.toString());
         } else {
            return new DerValue((byte)12, var4.toString());
         }
      }
   }

   private static Byte getEmbeddedHexPair(int var0, Reader var1) throws IOException {
      if ("0123456789ABCDEF".indexOf(Character.toUpperCase((char)var0)) >= 0) {
         int var2 = readChar(var1, "unexpected EOF - escaped hex value must include two valid digits");
         if ("0123456789ABCDEF".indexOf(Character.toUpperCase((char)var2)) >= 0) {
            int var3 = Character.digit((char)((char)var0), 16);
            int var4 = Character.digit((char)((char)var2), 16);
            return new Byte((byte)((var3 << 4) + var4));
         } else {
            throw new IOException("escaped hex value must include two valid digits");
         }
      } else {
         return null;
      }
   }

   private static String getEmbeddedHexString(List<Byte> var0) throws IOException {
      int var1 = var0.size();
      byte[] var2 = new byte[var1];

      for(int var3 = 0; var3 < var1; ++var3) {
         var2[var3] = (Byte)var0.get(var3);
      }

      return new String(var2, "UTF8");
   }

   private static boolean isTerminator(int var0, int var1) {
      switch(var0) {
      case -1:
      case 43:
      case 44:
         return true;
      case 59:
         return var1 != 3;
      default:
         return false;
      }
   }

   private static int readChar(Reader var0, String var1) throws IOException {
      int var2 = var0.read();
      if (var2 == -1) {
         throw new IOException(var1);
      } else {
         return var2;
      }
   }

   private static boolean trailingSpace(Reader var0) throws IOException {
      boolean var1 = false;
      if (!var0.markSupported()) {
         return true;
      } else {
         var0.mark(9999);

         while(true) {
            int var2 = var0.read();
            if (var2 == -1) {
               var1 = true;
               break;
            }

            if (var2 != 32) {
               if (var2 == 92) {
                  int var3 = var0.read();
                  if (var3 == 32) {
                     continue;
                  }

                  var1 = false;
                  break;
               }

               var1 = false;
               break;
            }
         }

         var0.reset();
         return var1;
      }
   }

   AVA(DerValue var1) throws IOException {
      if (var1.tag != 48) {
         throw new IOException("AVA not a sequence");
      } else {
         this.oid = var1.data.getOID();
         this.value = var1.data.getDerValue();
         if (var1.data.available() != 0) {
            throw new IOException("AVA, extra bytes = " + var1.data.available());
         }
      }
   }

   AVA(DerInputStream var1) throws IOException {
      this(var1.getDerValue());
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof AVA)) {
         return false;
      } else {
         AVA var2 = (AVA)var1;
         return this.toRFC2253CanonicalString().equals(var2.toRFC2253CanonicalString());
      }
   }

   public int hashCode() {
      return this.toRFC2253CanonicalString().hashCode();
   }

   public void encode(DerOutputStream var1) throws IOException {
      this.derEncode(var1);
   }

   public void derEncode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      DerOutputStream var3 = new DerOutputStream();
      var2.putOID(this.oid);
      this.value.encode(var2);
      var3.write((byte)48, (DerOutputStream)var2);
      var1.write(var3.toByteArray());
   }

   private String toKeyword(int var1, Map<String, String> var2) {
      return AVAKeyword.getKeyword(this.oid, var1, var2);
   }

   public String toString() {
      return this.toKeywordValueString(this.toKeyword(1, Collections.emptyMap()));
   }

   public String toRFC1779String() {
      return this.toRFC1779String(Collections.emptyMap());
   }

   public String toRFC1779String(Map<String, String> var1) {
      return this.toKeywordValueString(this.toKeyword(2, var1));
   }

   public String toRFC2253String() {
      return this.toRFC2253String(Collections.emptyMap());
   }

   public String toRFC2253String(Map<String, String> var1) {
      StringBuilder var2 = new StringBuilder(100);
      var2.append(this.toKeyword(3, var1));
      var2.append('=');
      String var3;
      if ((var2.charAt(0) < '0' || var2.charAt(0) > '9') && isDerString(this.value, false)) {
         var3 = null;

         try {
            var3 = new String(this.value.getDataBytes(), "UTF8");
         } catch (IOException var12) {
            throw new IllegalArgumentException("DER Value conversion");
         }

         StringBuilder var15 = new StringBuilder();

         int var9;
         char var10;
         for(int var6 = 0; var6 < var3.length(); ++var6) {
            char var7 = var3.charAt(var6);
            if (!DerValue.isPrintableStringChar(var7) && ",=+<>#;\"\\".indexOf(var7) < 0) {
               if (var7 == 0) {
                  var15.append("\\00");
               } else if (debug != null && Debug.isOn("ava")) {
                  Object var8 = null;

                  byte[] var18;
                  try {
                     var18 = Character.toString(var7).getBytes("UTF8");
                  } catch (IOException var11) {
                     throw new IllegalArgumentException("DER Value conversion");
                  }

                  for(var9 = 0; var9 < var18.length; ++var9) {
                     var15.append('\\');
                     var10 = Character.forDigit(15 & var18[var9] >>> 4, 16);
                     var15.append(Character.toUpperCase(var10));
                     var10 = Character.forDigit(15 & var18[var9], 16);
                     var15.append(Character.toUpperCase(var10));
                  }
               } else {
                  var15.append(var7);
               }
            } else {
               if (",=+<>#;\"\\".indexOf(var7) >= 0) {
                  var15.append('\\');
               }

               var15.append(var7);
            }
         }

         char[] var16 = var15.toString().toCharArray();
         var15 = new StringBuilder();

         int var17;
         for(var17 = 0; var17 < var16.length && (var16[var17] == ' ' || var16[var17] == '\r'); ++var17) {
         }

         int var19;
         for(var19 = var16.length - 1; var19 >= 0 && (var16[var19] == ' ' || var16[var19] == '\r'); --var19) {
         }

         for(var9 = 0; var9 < var16.length; ++var9) {
            var10 = var16[var9];
            if (var9 < var17 || var9 > var19) {
               var15.append('\\');
            }

            var15.append(var10);
         }

         var2.append(var15.toString());
      } else {
         var3 = null;

         byte[] var14;
         try {
            var14 = this.value.toByteArray();
         } catch (IOException var13) {
            throw new IllegalArgumentException("DER Value conversion");
         }

         var2.append('#');

         for(int var4 = 0; var4 < var14.length; ++var4) {
            byte var5 = var14[var4];
            var2.append(Character.forDigit(15 & var5 >>> 4, 16));
            var2.append(Character.forDigit(15 & var5, 16));
         }
      }

      return var2.toString();
   }

   public String toRFC2253CanonicalString() {
      StringBuilder var1 = new StringBuilder(40);
      var1.append(this.toKeyword(3, Collections.emptyMap()));
      var1.append('=');
      String var2;
      if ((var1.charAt(0) < '0' || var1.charAt(0) > '9') && isDerString(this.value, true)) {
         var2 = null;

         try {
            var2 = new String(this.value.getDataBytes(), "UTF8");
         } catch (IOException var11) {
            throw new IllegalArgumentException("DER Value conversion");
         }

         StringBuilder var14 = new StringBuilder();
         boolean var5 = false;

         for(int var6 = 0; var6 < var2.length(); ++var6) {
            char var7 = var2.charAt(var6);
            if (DerValue.isPrintableStringChar(var7) || ",+<>;\"\\".indexOf(var7) >= 0 || var6 == 0 && var7 == '#') {
               if (var6 == 0 && var7 == '#' || ",+<>;\"\\".indexOf(var7) >= 0) {
                  var14.append('\\');
               }

               if (!Character.isWhitespace(var7)) {
                  var5 = false;
                  var14.append(var7);
               } else if (!var5) {
                  var5 = true;
                  var14.append(var7);
               }
            } else if (debug != null && Debug.isOn("ava")) {
               var5 = false;
               Object var8 = null;

               byte[] var15;
               try {
                  var15 = Character.toString(var7).getBytes("UTF8");
               } catch (IOException var10) {
                  throw new IllegalArgumentException("DER Value conversion");
               }

               for(int var9 = 0; var9 < var15.length; ++var9) {
                  var14.append('\\');
                  var14.append(Character.forDigit(15 & var15[var9] >>> 4, 16));
                  var14.append(Character.forDigit(15 & var15[var9], 16));
               }
            } else {
               var5 = false;
               var14.append(var7);
            }
         }

         var1.append(var14.toString().trim());
      } else {
         var2 = null;

         byte[] var13;
         try {
            var13 = this.value.toByteArray();
         } catch (IOException var12) {
            throw new IllegalArgumentException("DER Value conversion");
         }

         var1.append('#');

         for(int var3 = 0; var3 < var13.length; ++var3) {
            byte var4 = var13[var3];
            var1.append(Character.forDigit(15 & var4 >>> 4, 16));
            var1.append(Character.forDigit(15 & var4, 16));
         }
      }

      var2 = var1.toString();
      var2 = var2.toUpperCase(Locale.US).toLowerCase(Locale.US);
      return Normalizer.normalize(var2, Normalizer.Form.NFKD);
   }

   private static boolean isDerString(DerValue var0, boolean var1) {
      if (var1) {
         switch(var0.tag) {
         case 12:
         case 19:
            return true;
         default:
            return false;
         }
      } else {
         switch(var0.tag) {
         case 12:
         case 19:
         case 20:
         case 22:
         case 27:
         case 30:
            return true;
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 21:
         case 23:
         case 24:
         case 25:
         case 26:
         case 28:
         case 29:
         default:
            return false;
         }
      }
   }

   boolean hasRFC2253Keyword() {
      return AVAKeyword.hasKeyword(this.oid, 3);
   }

   private String toKeywordValueString(String var1) {
      StringBuilder var2 = new StringBuilder(40);
      var2.append(var1);
      var2.append("=");

      try {
         String var3 = this.value.getAsString();
         if (var3 == null) {
            byte[] var4 = this.value.toByteArray();
            var2.append('#');

            for(int var5 = 0; var5 < var4.length; ++var5) {
               var2.append("0123456789ABCDEF".charAt(var4[var5] >> 4 & 15));
               var2.append("0123456789ABCDEF".charAt(var4[var5] & 15));
            }
         } else {
            boolean var16 = false;
            StringBuilder var17 = new StringBuilder();
            boolean var6 = false;
            int var8 = var3.length();
            boolean var9 = var8 > 1 && var3.charAt(0) == '"' && var3.charAt(var8 - 1) == '"';

            for(int var10 = 0; var10 < var8; ++var10) {
               char var11 = var3.charAt(var10);
               if (!var9 || var10 != 0 && var10 != var8 - 1) {
                  if (!DerValue.isPrintableStringChar(var11) && ",+=\n<>#;\\\"".indexOf(var11) < 0) {
                     if (debug != null && Debug.isOn("ava")) {
                        var6 = false;
                        byte[] var12 = Character.toString(var11).getBytes("UTF8");

                        for(int var13 = 0; var13 < var12.length; ++var13) {
                           var17.append('\\');
                           char var14 = Character.forDigit(15 & var12[var13] >>> 4, 16);
                           var17.append(Character.toUpperCase(var14));
                           var14 = Character.forDigit(15 & var12[var13], 16);
                           var17.append(Character.toUpperCase(var14));
                        }
                     } else {
                        var6 = false;
                        var17.append(var11);
                     }
                  } else {
                     if (!var16 && (var10 == 0 && (var11 == ' ' || var11 == '\n') || ",+=\n<>#;\\\"".indexOf(var11) >= 0)) {
                        var16 = true;
                     }

                     if (var11 != ' ' && var11 != '\n') {
                        if (var11 == '"' || var11 == '\\') {
                           var17.append('\\');
                        }

                        var6 = false;
                     } else {
                        if (!var16 && var6) {
                           var16 = true;
                        }

                        var6 = true;
                     }

                     var17.append(var11);
                  }
               } else {
                  var17.append(var11);
               }
            }

            if (var17.length() > 0) {
               char var18 = var17.charAt(var17.length() - 1);
               if (var18 == ' ' || var18 == '\n') {
                  var16 = true;
               }
            }

            if (!var9 && var16) {
               var2.append("\"" + var17.toString() + "\"");
            } else {
               var2.append(var17.toString());
            }
         }
      } catch (IOException var15) {
         throw new IllegalArgumentException("DER Value conversion");
      }

      return var2.toString();
   }
}
