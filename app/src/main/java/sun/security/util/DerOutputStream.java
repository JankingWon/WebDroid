package sun.security.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DerOutputStream extends ByteArrayOutputStream implements DerEncoder {
   private static ByteArrayLexOrder lexOrder = new ByteArrayLexOrder();
   private static ByteArrayTagOrder tagOrder = new ByteArrayTagOrder();

   public DerOutputStream(int var1) {
      super(var1);
   }

   public DerOutputStream() {
   }

   public void write(byte var1, byte[] var2) throws IOException {
      this.write(var1);
      this.putLength(var2.length);
      this.write(var2, 0, var2.length);
   }

   public void write(byte var1, DerOutputStream var2) throws IOException {
      this.write(var1);
      this.putLength(var2.count);
      this.write(var2.buf, 0, var2.count);
   }

   public void writeImplicit(byte var1, DerOutputStream var2) throws IOException {
      this.write(var1);
      this.write(var2.buf, 1, var2.count - 1);
   }

   public void putDerValue(DerValue var1) throws IOException {
      var1.encode(this);
   }

   public void putBoolean(boolean var1) throws IOException {
      this.write(1);
      this.putLength(1);
      if (var1) {
         this.write(255);
      } else {
         this.write(0);
      }

   }

   public void putEnumerated(int var1) throws IOException {
      this.write(10);
      this.putIntegerContents(var1);
   }

   public void putInteger(BigInteger var1) throws IOException {
      this.write(2);
      byte[] var2 = var1.toByteArray();
      this.putLength(var2.length);
      this.write(var2, 0, var2.length);
   }

   public void putInteger(Integer var1) throws IOException {
      this.putInteger(var1);
   }

   public void putInteger(int var1) throws IOException {
      this.write(2);
      this.putIntegerContents(var1);
   }

   private void putIntegerContents(int var1) throws IOException {
      byte[] var2 = new byte[4];
      int var3 = 0;
      var2[3] = (byte)(var1 & 255);
      var2[2] = (byte)((var1 & '\uff00') >>> 8);
      var2[1] = (byte)((var1 & 16711680) >>> 16);
      var2[0] = (byte)((var1 & -16777216) >>> 24);
      int var4;
      if (var2[0] == -1) {
         for(var4 = 0; var4 < 3 && var2[var4] == -1 && (var2[var4 + 1] & 128) == 128; ++var4) {
            ++var3;
         }
      } else if (var2[0] == 0) {
         for(var4 = 0; var4 < 3 && var2[var4] == 0 && (var2[var4 + 1] & 128) == 0; ++var4) {
            ++var3;
         }
      }

      this.putLength(4 - var3);

      for(var4 = var3; var4 < 4; ++var4) {
         this.write(var2[var4]);
      }

   }

   public void putBitString(byte[] var1) throws IOException {
      this.write(3);
      this.putLength(var1.length + 1);
      this.write(0);
      this.write(var1);
   }

   public void putUnalignedBitString(BitArray var1) throws IOException {
      byte[] var2 = var1.toByteArray();
      this.write(3);
      this.putLength(var2.length + 1);
      this.write(var2.length * 8 - var1.length());
      this.write(var2);
   }

   public void putTruncatedUnalignedBitString(BitArray var1) throws IOException {
      this.putUnalignedBitString(var1.truncate());
   }

   public void putOctetString(byte[] var1) throws IOException {
      this.write((byte)4, (byte[])var1);
   }

   public void putNull() throws IOException {
      this.write(5);
      this.putLength(0);
   }

   public void putOID(ObjectIdentifier var1) throws IOException {
      var1.encode(this);
   }

   public void putSequence(DerValue[] var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();

      for(int var3 = 0; var3 < var1.length; ++var3) {
         var1[var3].encode(var2);
      }

      this.write((byte)48, (DerOutputStream)var2);
   }

   public void putSet(DerValue[] var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();

      for(int var3 = 0; var3 < var1.length; ++var3) {
         var1[var3].encode(var2);
      }

      this.write((byte)49, (DerOutputStream)var2);
   }

   public void putOrderedSetOf(byte var1, DerEncoder[] var2) throws IOException {
      this.putOrderedSet(var1, var2, lexOrder);
   }

   public void putOrderedSet(byte var1, DerEncoder[] var2) throws IOException {
      this.putOrderedSet(var1, var2, tagOrder);
   }

   private void putOrderedSet(byte var1, DerEncoder[] var2, Comparator<byte[]> var3) throws IOException {
      DerOutputStream[] var4 = new DerOutputStream[var2.length];

      for(int var5 = 0; var5 < var2.length; ++var5) {
         var4[var5] = new DerOutputStream();
         var2[var5].derEncode(var4[var5]);
      }

      byte[][] var8 = new byte[var4.length][];

      for(int var6 = 0; var6 < var4.length; ++var6) {
         var8[var6] = var4[var6].toByteArray();
      }

      Arrays.sort(var8, var3);
      DerOutputStream var9 = new DerOutputStream();

      for(int var7 = 0; var7 < var4.length; ++var7) {
         var9.write(var8[var7]);
      }

      this.write(var1, var9);
   }

   public void putUTF8String(String var1) throws IOException {
      this.writeString(var1, (byte)12, "UTF8");
   }

   public void putPrintableString(String var1) throws IOException {
      this.writeString(var1, (byte)19, "ASCII");
   }

   public void putT61String(String var1) throws IOException {
      this.writeString(var1, (byte)20, "ISO-8859-1");
   }

   public void putIA5String(String var1) throws IOException {
      this.writeString(var1, (byte)22, "ASCII");
   }

   public void putBMPString(String var1) throws IOException {
      this.writeString(var1, (byte)30, "UnicodeBigUnmarked");
   }

   public void putGeneralString(String var1) throws IOException {
      this.writeString(var1, (byte)27, "ASCII");
   }

   private void writeString(String var1, byte var2, String var3) throws IOException {
      byte[] var4 = var1.getBytes(var3);
      this.write(var2);
      this.putLength(var4.length);
      this.write(var4);
   }

   public void putUTCTime(Date var1) throws IOException {
      this.putTime(var1, (byte)23);
   }

   public void putGeneralizedTime(Date var1) throws IOException {
      this.putTime(var1, (byte)24);
   }

   private void putTime(Date var1, byte var2) throws IOException {
      TimeZone var3 = TimeZone.getTimeZone("GMT");
      String var4 = null;
      if (var2 == 23) {
         var4 = "yyMMddHHmmss'Z'";
      } else {
         var2 = 24;
         var4 = "yyyyMMddHHmmss'Z'";
      }

      SimpleDateFormat var5 = new SimpleDateFormat(var4, Locale.US);
      var5.setTimeZone(var3);
      byte[] var6 = var5.format(var1).getBytes("ISO-8859-1");
      this.write(var2);
      this.putLength(var6.length);
      this.write(var6);
   }

   public void putLength(int var1) throws IOException {
      if (var1 < 128) {
         this.write((byte)var1);
      } else if (var1 < 256) {
         this.write(-127);
         this.write((byte)var1);
      } else if (var1 < 65536) {
         this.write(-126);
         this.write((byte)(var1 >> 8));
         this.write((byte)var1);
      } else if (var1 < 16777216) {
         this.write(-125);
         this.write((byte)(var1 >> 16));
         this.write((byte)(var1 >> 8));
         this.write((byte)var1);
      } else {
         this.write(-124);
         this.write((byte)(var1 >> 24));
         this.write((byte)(var1 >> 16));
         this.write((byte)(var1 >> 8));
         this.write((byte)var1);
      }

   }

   public void putTag(byte var1, boolean var2, byte var3) {
      byte var4 = (byte)(var1 | var3);
      if (var2) {
         var4 = (byte)(var4 | 32);
      }

      this.write(var4);
   }

   public void derEncode(OutputStream var1) throws IOException {
      var1.write(this.toByteArray());
   }
}
