package sun.security.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;

import sun.misc.IOUtils;

public class DerValue {
   public static final byte TAG_UNIVERSAL = 0;
   public static final byte TAG_APPLICATION = 64;
   public static final byte TAG_CONTEXT = -128;
   public static final byte TAG_PRIVATE = -64;
   public byte tag;
   protected DerInputBuffer buffer;
   public final DerInputStream data;
   private int length;
   public static final byte tag_Boolean = 1;
   public static final byte tag_Integer = 2;
   public static final byte tag_BitString = 3;
   public static final byte tag_OctetString = 4;
   public static final byte tag_Null = 5;
   public static final byte tag_ObjectId = 6;
   public static final byte tag_Enumerated = 10;
   public static final byte tag_UTF8String = 12;
   public static final byte tag_PrintableString = 19;
   public static final byte tag_T61String = 20;
   public static final byte tag_IA5String = 22;
   public static final byte tag_UtcTime = 23;
   public static final byte tag_GeneralizedTime = 24;
   public static final byte tag_GeneralString = 27;
   public static final byte tag_UniversalString = 28;
   public static final byte tag_BMPString = 30;
   public static final byte tag_Sequence = 48;
   public static final byte tag_SequenceOf = 48;
   public static final byte tag_Set = 49;
   public static final byte tag_SetOf = 49;

   public boolean isUniversal() {
      return (this.tag & 192) == 0;
   }

   public boolean isApplication() {
      return (this.tag & 192) == 64;
   }

   public boolean isContextSpecific() {
      return (this.tag & 192) == 128;
   }

   public boolean isContextSpecific(byte var1) {
      if (!this.isContextSpecific()) {
         return false;
      } else {
         return (this.tag & 31) == var1;
      }
   }

   boolean isPrivate() {
      return (this.tag & 192) == 192;
   }

   public boolean isConstructed() {
      return (this.tag & 32) == 32;
   }

   public boolean isConstructed(byte var1) {
      if (!this.isConstructed()) {
         return false;
      } else {
         return (this.tag & 31) == var1;
      }
   }

   public DerValue(String var1) throws IOException {
      boolean var2 = true;

      for(int var3 = 0; var3 < var1.length(); ++var3) {
         if (!isPrintableStringChar(var1.charAt(var3))) {
            var2 = false;
            break;
         }
      }

      this.data = this.init((byte)(var2 ? 19 : 12), var1);
   }

   public DerValue(byte var1, String var2) throws IOException {
      this.data = this.init(var1, var2);
   }

   DerValue(byte var1, byte[] var2, boolean var3) {
      this.tag = var1;
      this.buffer = new DerInputBuffer((byte[])var2.clone(), var3);
      this.length = var2.length;
      this.data = new DerInputStream(this.buffer);
      this.data.mark(Integer.MAX_VALUE);
   }

   public DerValue(byte var1, byte[] var2) {
      this(var1, var2, true);
   }

   DerValue(DerInputBuffer var1) throws IOException {
      this.tag = (byte)var1.read();
      byte var2 = (byte)var1.read();
      this.length = DerInputStream.getLength(var2, var1);
      if (this.length == -1) {
         DerInputBuffer var3 = var1.dup();
         int var4 = var3.available();
         byte var5 = 2;
         byte[] var6 = new byte[var4 + var5];
         var6[0] = this.tag;
         var6[1] = var2;
         DataInputStream var7 = new DataInputStream(var3);
         var7.readFully(var6, var5, var4);
         var7.close();
         DerIndefLenConverter var8 = new DerIndefLenConverter();
         var3 = new DerInputBuffer(var8.convert(var6), var1.allowBER);
         if (this.tag != var3.read()) {
            throw new IOException("Indefinite length encoding not supported");
         }

         this.length = DerInputStream.getLength(var3);
         this.buffer = var3.dup();
         this.buffer.truncate(this.length);
         this.data = new DerInputStream(this.buffer);
         var1.skip((long)(this.length + var5));
      } else {
         this.buffer = var1.dup();
         this.buffer.truncate(this.length);
         this.data = new DerInputStream(this.buffer);
         var1.skip((long)this.length);
      }

   }

   DerValue(byte[] var1, boolean var2) throws IOException {
      this.data = this.init(true, new ByteArrayInputStream(var1), var2);
   }

   public DerValue(byte[] var1) throws IOException {
      this(var1, true);
   }

   DerValue(byte[] var1, int var2, int var3, boolean var4) throws IOException {
      this.data = this.init(true, new ByteArrayInputStream(var1, var2, var3), var4);
   }

   public DerValue(byte[] var1, int var2, int var3) throws IOException {
      this(var1, var2, var3, true);
   }

   DerValue(InputStream var1, boolean var2) throws IOException {
      this.data = this.init(false, var1, var2);
   }

   public DerValue(InputStream var1) throws IOException {
      this(var1, true);
   }

   private DerInputStream init(byte var1, String var2) throws IOException {
      String var3 = null;
      this.tag = var1;
      switch(var1) {
      case 12:
         var3 = "UTF8";
         break;
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
         throw new IllegalArgumentException("Unsupported DER string type");
      case 19:
      case 22:
      case 27:
         var3 = "ASCII";
         break;
      case 20:
         var3 = "ISO-8859-1";
         break;
      case 30:
         var3 = "UnicodeBigUnmarked";
      }

      byte[] var4 = var2.getBytes(var3);
      this.length = var4.length;
      this.buffer = new DerInputBuffer(var4, true);
      DerInputStream var5 = new DerInputStream(this.buffer);
      var5.mark(Integer.MAX_VALUE);
      return var5;
   }

   private DerInputStream init(boolean var1, InputStream var2, boolean var3) throws IOException {
      this.tag = (byte)((InputStream)var2).read();
      byte var4 = (byte)((InputStream)var2).read();
      this.length = DerInputStream.getLength(var4, (InputStream)var2);
      if (this.length == -1) {
         int var5 = ((InputStream)var2).available();
         byte var6 = 2;
         byte[] var7 = new byte[var5 + var6];
         var7[0] = this.tag;
         var7[1] = var4;
         DataInputStream var8 = new DataInputStream((InputStream)var2);
         var8.readFully(var7, var6, var5);
         var8.close();
         DerIndefLenConverter var9 = new DerIndefLenConverter();
         var2 = new ByteArrayInputStream(var9.convert(var7));
         if (this.tag != ((InputStream)var2).read()) {
            throw new IOException("Indefinite length encoding not supported");
         }

         this.length = DerInputStream.getLength((InputStream)var2);
      }

      if (var1 && ((InputStream)var2).available() != this.length) {
         throw new IOException("extra data given to DerValue constructor");
      } else {
         byte[] var10 = IOUtils.readExactlyNBytes((InputStream)var2, this.length);
         this.buffer = new DerInputBuffer(var10, var3);
         return new DerInputStream(this.buffer);
      }
   }

   public void encode(DerOutputStream var1) throws IOException {
      var1.write(this.tag);
      var1.putLength(this.length);
      if (this.length > 0) {
         byte[] var2 = new byte[this.length];
         synchronized(this.data) {
            this.buffer.reset();
            if (this.buffer.read(var2) != this.length) {
               throw new IOException("short DER value read (encode)");
            }

            var1.write(var2);
         }
      }

   }

   public final DerInputStream getData() {
      return this.data;
   }

   public final byte getTag() {
      return this.tag;
   }

   public boolean getBoolean() throws IOException {
      if (this.tag != 1) {
         throw new IOException("DerValue.getBoolean, not a BOOLEAN " + this.tag);
      } else if (this.length != 1) {
         throw new IOException("DerValue.getBoolean, invalid length " + this.length);
      } else {
         return this.buffer.read() != 0;
      }
   }

   public ObjectIdentifier getOID() throws IOException {
      if (this.tag != 6) {
         throw new IOException("DerValue.getOID, not an OID " + this.tag);
      } else {
         return new ObjectIdentifier(this.buffer);
      }
   }

   private byte[] append(byte[] var1, byte[] var2) {
      if (var1 == null) {
         return var2;
      } else {
         byte[] var3 = new byte[var1.length + var2.length];
         System.arraycopy(var1, 0, var3, 0, var1.length);
         System.arraycopy(var2, 0, var3, var1.length, var2.length);
         return var3;
      }
   }

   public byte[] getOctetString() throws IOException {
      if (this.tag != 4 && !this.isConstructed((byte)4)) {
         throw new IOException("DerValue.getOctetString, not an Octet String: " + this.tag);
      } else if (this.length == 0) {
         return new byte[0];
      } else {
         DerInputBuffer var1 = this.buffer;
         if (var1.available() < this.length) {
            throw new IOException("short read on DerValue buffer");
         } else {
            byte[] var2 = new byte[this.length];
            var1.read(var2);
            if (this.isConstructed()) {
               DerInputStream var3 = new DerInputStream(var2, 0, var2.length, this.buffer.allowBER);

               for(var2 = null; var3.available() != 0; var2 = this.append(var2, var3.getOctetString())) {
               }
            }

            return var2;
         }
      }
   }

   public int getInteger() throws IOException {
      if (this.tag != 2) {
         throw new IOException("DerValue.getInteger, not an int " + this.tag);
      } else {
         return this.buffer.getInteger(this.data.available());
      }
   }

   public BigInteger getBigInteger() throws IOException {
      if (this.tag != 2) {
         throw new IOException("DerValue.getBigInteger, not an int " + this.tag);
      } else {
         return this.buffer.getBigInteger(this.data.available(), false);
      }
   }

   public BigInteger getPositiveBigInteger() throws IOException {
      if (this.tag != 2) {
         throw new IOException("DerValue.getBigInteger, not an int " + this.tag);
      } else {
         return this.buffer.getBigInteger(this.data.available(), true);
      }
   }

   public int getEnumerated() throws IOException {
      if (this.tag != 10) {
         throw new IOException("DerValue.getEnumerated, incorrect tag: " + this.tag);
      } else {
         return this.buffer.getInteger(this.data.available());
      }
   }

   public byte[] getBitString() throws IOException {
      if (this.tag != 3) {
         throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
      } else {
         return this.buffer.getBitString();
      }
   }

   public BitArray getUnalignedBitString() throws IOException {
      if (this.tag != 3) {
         throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
      } else {
         return this.buffer.getUnalignedBitString();
      }
   }

   public String getAsString() throws IOException {
      if (this.tag == 12) {
         return this.getUTF8String();
      } else if (this.tag == 19) {
         return this.getPrintableString();
      } else if (this.tag == 20) {
         return this.getT61String();
      } else if (this.tag == 22) {
         return this.getIA5String();
      } else if (this.tag == 30) {
         return this.getBMPString();
      } else {
         return this.tag == 27 ? this.getGeneralString() : null;
      }
   }

   public byte[] getBitString(boolean var1) throws IOException {
      if (!var1 && this.tag != 3) {
         throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
      } else {
         return this.buffer.getBitString();
      }
   }

   public BitArray getUnalignedBitString(boolean var1) throws IOException {
      if (!var1 && this.tag != 3) {
         throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
      } else {
         return this.buffer.getUnalignedBitString();
      }
   }

   public byte[] getDataBytes() throws IOException {
      byte[] var1 = new byte[this.length];
      synchronized(this.data) {
         this.data.reset();
         this.data.getBytes(var1);
         return var1;
      }
   }

   public String getPrintableString() throws IOException {
      if (this.tag != 19) {
         throw new IOException("DerValue.getPrintableString, not a string " + this.tag);
      } else {
         return new String(this.getDataBytes(), "ASCII");
      }
   }

   public String getT61String() throws IOException {
      if (this.tag != 20) {
         throw new IOException("DerValue.getT61String, not T61 " + this.tag);
      } else {
         return new String(this.getDataBytes(), "ISO-8859-1");
      }
   }

   public String getIA5String() throws IOException {
      if (this.tag != 22) {
         throw new IOException("DerValue.getIA5String, not IA5 " + this.tag);
      } else {
         return new String(this.getDataBytes(), "ASCII");
      }
   }

   public String getBMPString() throws IOException {
      if (this.tag != 30) {
         throw new IOException("DerValue.getBMPString, not BMP " + this.tag);
      } else {
         return new String(this.getDataBytes(), "UnicodeBigUnmarked");
      }
   }

   public String getUTF8String() throws IOException {
      if (this.tag != 12) {
         throw new IOException("DerValue.getUTF8String, not UTF-8 " + this.tag);
      } else {
         return new String(this.getDataBytes(), "UTF8");
      }
   }

   public String getGeneralString() throws IOException {
      if (this.tag != 27) {
         throw new IOException("DerValue.getGeneralString, not GeneralString " + this.tag);
      } else {
         return new String(this.getDataBytes(), "ASCII");
      }
   }

   public Date getUTCTime() throws IOException {
      if (this.tag != 23) {
         throw new IOException("DerValue.getUTCTime, not a UtcTime: " + this.tag);
      } else {
         return this.buffer.getUTCTime(this.data.available());
      }
   }

   public Date getGeneralizedTime() throws IOException {
      if (this.tag != 24) {
         throw new IOException("DerValue.getGeneralizedTime, not a GeneralizedTime: " + this.tag);
      } else {
         return this.buffer.getGeneralizedTime(this.data.available());
      }
   }

   public boolean equals(Object var1) {
      return var1 instanceof DerValue ? this.equals((DerValue)var1) : false;
   }

   public boolean equals(DerValue var1) {
      if (this == var1) {
         return true;
      } else if (this.tag != var1.tag) {
         return false;
      } else if (this.data == var1.data) {
         return true;
      } else {
         return System.identityHashCode(this.data) > System.identityHashCode(var1.data) ? doEquals(this, var1) : doEquals(var1, this);
      }
   }

   private static boolean doEquals(DerValue var0, DerValue var1) {
      synchronized(var0.data) {
         boolean var10000;
         synchronized(var1.data) {
            var0.data.reset();
            var1.data.reset();
            var10000 = var0.buffer.equals(var1.buffer);
         }

         return var10000;
      }
   }

   public String toString() {
      try {
         String var1 = this.getAsString();
         if (var1 != null) {
            return "\"" + var1 + "\"";
         } else if (this.tag == 5) {
            return "[DerValue, null]";
         } else {
            return this.tag == 6 ? "OID." + this.getOID() : "[DerValue, tag = " + this.tag + ", length = " + this.length + "]";
         }
      } catch (IOException var2) {
         throw new IllegalArgumentException("misformatted DER value");
      }
   }

   public byte[] toByteArray() throws IOException {
      DerOutputStream var1 = new DerOutputStream();
      this.encode(var1);
      this.data.reset();
      return var1.toByteArray();
   }

   public DerInputStream toDerInputStream() throws IOException {
      if (this.tag != 48 && this.tag != 49) {
         throw new IOException("toDerInputStream rejects tag type " + this.tag);
      } else {
         return new DerInputStream(this.buffer);
      }
   }

   public int length() {
      return this.length;
   }

   public static boolean isPrintableStringChar(char var0) {
      if ((var0 < 'a' || var0 > 'z') && (var0 < 'A' || var0 > 'Z') && (var0 < '0' || var0 > '9')) {
         switch(var0) {
         case ' ':
         case '\'':
         case '(':
         case ')':
         case '+':
         case ',':
         case '-':
         case '.':
         case '/':
         case ':':
         case '=':
         case '?':
            return true;
         case '!':
         case '"':
         case '#':
         case '$':
         case '%':
         case '&':
         case '*':
         case '0':
         case '1':
         case '2':
         case '3':
         case '4':
         case '5':
         case '6':
         case '7':
         case '8':
         case '9':
         case ';':
         case '<':
         case '>':
         default:
            return false;
         }
      } else {
         return true;
      }
   }

   public static byte createTag(byte var0, boolean var1, byte var2) {
      byte var3 = (byte)(var0 | var2);
      if (var1) {
         var3 = (byte)(var3 | 32);
      }

      return var3;
   }

   public void resetTag(byte var1) {
      this.tag = var1;
   }

   public int hashCode() {
      return this.toString().hashCode();
   }
}
