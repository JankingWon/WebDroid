package sun.security.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.Vector;

public class DerInputStream {
   DerInputBuffer buffer;
   public byte tag;

   public DerInputStream(byte[] var1) throws IOException {
      this.init(var1, 0, var1.length, true);
   }

   public DerInputStream(byte[] var1, int var2, int var3, boolean var4) throws IOException {
      this.init(var1, var2, var3, var4);
   }

   public DerInputStream(byte[] var1, int var2, int var3) throws IOException {
      this.init(var1, var2, var3, true);
   }

   private void init(byte[] var1, int var2, int var3, boolean var4) throws IOException {
      if (var2 + 2 <= var1.length && var2 + var3 <= var1.length) {
         if (DerIndefLenConverter.isIndefinite(var1[var2 + 1])) {
            if (!var4) {
               throw new IOException("Indefinite length BER encoding found");
            }

            byte[] var5 = new byte[var3];
            System.arraycopy(var1, var2, var5, 0, var3);
            DerIndefLenConverter var6 = new DerIndefLenConverter();
            this.buffer = new DerInputBuffer(var6.convert(var5), var4);
         } else {
            this.buffer = new DerInputBuffer(var1, var2, var3, var4);
         }

         this.buffer.mark(Integer.MAX_VALUE);
      } else {
         throw new IOException("Encoding bytes too short");
      }
   }

   DerInputStream(DerInputBuffer var1) {
      this.buffer = var1;
      this.buffer.mark(Integer.MAX_VALUE);
   }

   public DerInputStream subStream(int var1, boolean var2) throws IOException {
      DerInputBuffer var3 = this.buffer.dup();
      var3.truncate(var1);
      if (var2) {
         this.buffer.skip((long)var1);
      }

      return new DerInputStream(var3);
   }

   public byte[] toByteArray() {
      return this.buffer.toByteArray();
   }

   public int getInteger() throws IOException {
      if (this.buffer.read() != 2) {
         throw new IOException("DER input, Integer tag error");
      } else {
         return this.buffer.getInteger(getLength(this.buffer));
      }
   }

   public BigInteger getBigInteger() throws IOException {
      if (this.buffer.read() != 2) {
         throw new IOException("DER input, Integer tag error");
      } else {
         return this.buffer.getBigInteger(getLength(this.buffer), false);
      }
   }

   public BigInteger getPositiveBigInteger() throws IOException {
      if (this.buffer.read() != 2) {
         throw new IOException("DER input, Integer tag error");
      } else {
         return this.buffer.getBigInteger(getLength(this.buffer), true);
      }
   }

   public int getEnumerated() throws IOException {
      if (this.buffer.read() != 10) {
         throw new IOException("DER input, Enumerated tag error");
      } else {
         return this.buffer.getInteger(getLength(this.buffer));
      }
   }

   public byte[] getBitString() throws IOException {
      if (this.buffer.read() != 3) {
         throw new IOException("DER input not an bit string");
      } else {
         return this.buffer.getBitString(getLength(this.buffer));
      }
   }

   public BitArray getUnalignedBitString() throws IOException {
      if (this.buffer.read() != 3) {
         throw new IOException("DER input not a bit string");
      } else {
         int var1 = getLength(this.buffer) - 1;
         int var2 = this.buffer.read();
         if (var2 < 0) {
            throw new IOException("Unused bits of bit string invalid");
         } else {
            int var3 = var1 * 8 - var2;
            if (var3 < 0) {
               throw new IOException("Valid bits of bit string invalid");
            } else {
               byte[] var4 = new byte[var1];
               if (var1 != 0 && this.buffer.read(var4) != var1) {
                  throw new IOException("Short read of DER bit string");
               } else {
                  return new BitArray(var3, var4);
               }
            }
         }
      }
   }

   public byte[] getOctetString() throws IOException {
      if (this.buffer.read() != 4) {
         throw new IOException("DER input not an octet string");
      } else {
         int var1 = getLength(this.buffer);
         byte[] var2 = new byte[var1];
         if (var1 != 0 && this.buffer.read(var2) != var1) {
            throw new IOException("Short read of DER octet string");
         } else {
            return var2;
         }
      }
   }

   public void getBytes(byte[] var1) throws IOException {
      if (var1.length != 0 && this.buffer.read(var1) != var1.length) {
         throw new IOException("Short read of DER octet string");
      }
   }

   public void getNull() throws IOException {
      if (this.buffer.read() != 5 || this.buffer.read() != 0) {
         throw new IOException("getNull, bad data");
      }
   }

   public ObjectIdentifier getOID() throws IOException {
      return new ObjectIdentifier(this);
   }

   public DerValue[] getSequence(int var1) throws IOException {
      this.tag = (byte)this.buffer.read();
      if (this.tag != 48) {
         throw new IOException("Sequence tag error");
      } else {
         return this.readVector(var1);
      }
   }

   public DerValue[] getSet(int var1) throws IOException {
      this.tag = (byte)this.buffer.read();
      if (this.tag != 49) {
         throw new IOException("Set tag error");
      } else {
         return this.readVector(var1);
      }
   }

   public DerValue[] getSet(int var1, boolean var2) throws IOException {
      this.tag = (byte)this.buffer.read();
      if (!var2 && this.tag != 49) {
         throw new IOException("Set tag error");
      } else {
         return this.readVector(var1);
      }
   }

   protected DerValue[] readVector(int var1) throws IOException {
      byte var3 = (byte)this.buffer.read();
      int var4 = getLength(var3, this.buffer);
      if (var4 == -1) {
         int var5 = this.buffer.available();
         byte var6 = 2;
         byte[] var7 = new byte[var5 + var6];
         var7[0] = this.tag;
         var7[1] = var3;
         DataInputStream var8 = new DataInputStream(this.buffer);
         var8.readFully(var7, var6, var5);
         var8.close();
         DerIndefLenConverter var9 = new DerIndefLenConverter();
         this.buffer = new DerInputBuffer(var9.convert(var7), this.buffer.allowBER);
         if (this.tag != this.buffer.read()) {
            throw new IOException("Indefinite length encoding not supported");
         }

         var4 = getLength(this.buffer);
      }

      if (var4 == 0) {
         return new DerValue[0];
      } else {
         DerInputStream var2;
         if (this.buffer.available() == var4) {
            var2 = this;
         } else {
            var2 = this.subStream(var4, true);
         }

         Vector var10 = new Vector(var1);

         do {
            DerValue var11 = new DerValue(var2.buffer, this.buffer.allowBER);
            var10.addElement(var11);
         } while(var2.available() > 0);

         if (var2.available() != 0) {
            throw new IOException("Extra data at end of vector");
         } else {
            int var13 = var10.size();
            DerValue[] var14 = new DerValue[var13];

            for(int var12 = 0; var12 < var13; ++var12) {
               var14[var12] = (DerValue)var10.elementAt(var12);
            }

            return var14;
         }
      }
   }

   public DerValue getDerValue() throws IOException {
      return new DerValue(this.buffer);
   }

   public String getUTF8String() throws IOException {
      return this.readString((byte)12, "UTF-8", "UTF8");
   }

   public String getPrintableString() throws IOException {
      return this.readString((byte)19, "Printable", "ASCII");
   }

   public String getT61String() throws IOException {
      return this.readString((byte)20, "T61", "ISO-8859-1");
   }

   public String getIA5String() throws IOException {
      return this.readString((byte)22, "IA5", "ASCII");
   }

   public String getBMPString() throws IOException {
      return this.readString((byte)30, "BMP", "UnicodeBigUnmarked");
   }

   public String getGeneralString() throws IOException {
      return this.readString((byte)27, "General", "ASCII");
   }

   private String readString(byte var1, String var2, String var3) throws IOException {
      if (this.buffer.read() != var1) {
         throw new IOException("DER input not a " + var2 + " string");
      } else {
         int var4 = getLength(this.buffer);
         byte[] var5 = new byte[var4];
         if (var4 != 0 && this.buffer.read(var5) != var4) {
            throw new IOException("Short read of DER " + var2 + " string");
         } else {
            return new String(var5, var3);
         }
      }
   }

   public Date getUTCTime() throws IOException {
      if (this.buffer.read() != 23) {
         throw new IOException("DER input, UTCtime tag invalid ");
      } else {
         return this.buffer.getUTCTime(getLength(this.buffer));
      }
   }

   public Date getGeneralizedTime() throws IOException {
      if (this.buffer.read() != 24) {
         throw new IOException("DER input, GeneralizedTime tag invalid ");
      } else {
         return this.buffer.getGeneralizedTime(getLength(this.buffer));
      }
   }

   int getByte() throws IOException {
      return 255 & this.buffer.read();
   }

   public int peekByte() throws IOException {
      return this.buffer.peek();
   }

   int getLength() throws IOException {
      return getLength(this.buffer);
   }

   static int getLength(InputStream var0) throws IOException {
      return getLength(var0.read(), var0);
   }

   static int getLength(int var0, InputStream var1) throws IOException {
      if (var0 == -1) {
         throw new IOException("Short read of DER length");
      } else {
         String var4 = "DerInputStream.getLength(): ";
         int var2;
         if ((var0 & 128) == 0) {
            var2 = var0;
         } else {
            int var3 = var0 & 127;
            if (var3 == 0) {
               return -1;
            }

            if (var3 < 0 || var3 > 4) {
               throw new IOException(var4 + "lengthTag=" + var3 + ", " + (var3 < 0 ? "incorrect DER encoding." : "too big."));
            }

            var2 = 255 & var1.read();
            --var3;
            if (var2 == 0) {
               throw new IOException(var4 + "Redundant length bytes found");
            }

            while(var3-- > 0) {
               var2 <<= 8;
               var2 += 255 & var1.read();
            }

            if (var2 < 0) {
               throw new IOException(var4 + "Invalid length bytes");
            }

            if (var2 <= 127) {
               throw new IOException(var4 + "Should use short form for length");
            }
         }

         return var2;
      }
   }

   public void mark(int var1) {
      this.buffer.mark(var1);
   }

   public void reset() {
      this.buffer.reset();
   }

   public int available() {
      return this.buffer.available();
   }
}
