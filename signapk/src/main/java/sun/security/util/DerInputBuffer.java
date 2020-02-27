package sun.security.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.TimeZone;

import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.Gregorian;

class DerInputBuffer extends ByteArrayInputStream implements Cloneable {
   boolean allowBER;

   DerInputBuffer(byte[] var1) {
      this(var1, true);
   }

   DerInputBuffer(byte[] var1, boolean var2) {
      super(var1);
      this.allowBER = true;
      this.allowBER = var2;
   }

   DerInputBuffer(byte[] var1, int var2, int var3, boolean var4) {
      super(var1, var2, var3);
      this.allowBER = true;
      this.allowBER = var4;
   }

   DerInputBuffer dup() {
      try {
         DerInputBuffer var1 = (DerInputBuffer)this.clone();
         var1.mark(Integer.MAX_VALUE);
         return var1;
      } catch (CloneNotSupportedException var2) {
         throw new IllegalArgumentException(var2.toString());
      }
   }

   byte[] toByteArray() {
      int var1 = this.available();
      if (var1 <= 0) {
         return null;
      } else {
         byte[] var2 = new byte[var1];
         System.arraycopy(this.buf, this.pos, var2, 0, var1);
         return var2;
      }
   }

   int peek() throws IOException {
      if (this.pos >= this.count) {
         throw new IOException("out of data");
      } else {
         return this.buf[this.pos];
      }
   }

   public boolean equals(Object var1) {
      return var1 instanceof DerInputBuffer ? this.equals((DerInputBuffer)var1) : false;
   }

   boolean equals(DerInputBuffer var1) {
      if (this == var1) {
         return true;
      } else {
         int var2 = this.available();
         if (var1.available() != var2) {
            return false;
         } else {
            for(int var3 = 0; var3 < var2; ++var3) {
               if (this.buf[this.pos + var3] != var1.buf[var1.pos + var3]) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public int hashCode() {
      int var1 = 0;
      int var2 = this.available();
      int var3 = this.pos;

      for(int var4 = 0; var4 < var2; ++var4) {
         var1 += this.buf[var3 + var4] * var4;
      }

      return var1;
   }

   void truncate(int var1) throws IOException {
      if (var1 > this.available()) {
         throw new IOException("insufficient data");
      } else {
         this.count = this.pos + var1;
      }
   }

   BigInteger getBigInteger(int var1, boolean var2) throws IOException {
      if (var1 > this.available()) {
         throw new IOException("short read of integer");
      } else if (var1 == 0) {
         throw new IOException("Invalid encoding: zero length Int value");
      } else {
         byte[] var3 = new byte[var1];
         System.arraycopy(this.buf, this.pos, var3, 0, var1);
         this.skip((long)var1);
         if (!this.allowBER && var1 >= 2 && var3[0] == 0 && var3[1] >= 0) {
            throw new IOException("Invalid encoding: redundant leading 0s");
         } else {
            return var2 ? new BigInteger(1, var3) : new BigInteger(var3);
         }
      }
   }

   public int getInteger(int var1) throws IOException {
      BigInteger var2 = this.getBigInteger(var1, false);
      if (var2.compareTo(BigInteger.valueOf(-2147483648L)) < 0) {
         throw new IOException("Integer below minimum valid value");
      } else if (var2.compareTo(BigInteger.valueOf(2147483647L)) > 0) {
         throw new IOException("Integer exceeds maximum valid value");
      } else {
         return var2.intValue();
      }
   }

   public byte[] getBitString(int var1) throws IOException {
      if (var1 > this.available()) {
         throw new IOException("short read of bit string");
      } else if (var1 == 0) {
         throw new IOException("Invalid encoding: zero length bit string");
      } else {
         byte var2 = this.buf[this.pos];
         if (var2 >= 0 && var2 <= 7) {
            byte[] var3 = new byte[var1 - 1];
            System.arraycopy(this.buf, this.pos + 1, var3, 0, var1 - 1);
            if (var2 != 0) {
               var3[var1 - 2] = (byte)(var3[var1 - 2] & 255 << var2);
            }

            this.skip((long)var1);
            return var3;
         } else {
            throw new IOException("Invalid number of padding bits");
         }
      }
   }

   byte[] getBitString() throws IOException {
      return this.getBitString(this.available());
   }

   BitArray getUnalignedBitString() throws IOException {
      if (this.pos >= this.count) {
         return null;
      } else {
         int var1 = this.available();
         int var2 = this.buf[this.pos] & 255;
         if (var2 > 7) {
            throw new IOException("Invalid value for unused bits: " + var2);
         } else {
            byte[] var3 = new byte[var1 - 1];
            int var4 = var3.length == 0 ? 0 : var3.length * 8 - var2;
            System.arraycopy(this.buf, this.pos + 1, var3, 0, var1 - 1);
            BitArray var5 = new BitArray(var4, var3);
            this.pos = this.count;
            return var5;
         }
      }
   }

   public Date getUTCTime(int var1) throws IOException {
      if (var1 > this.available()) {
         throw new IOException("short read of DER UTC Time");
      } else if (var1 >= 11 && var1 <= 17) {
         return this.getTime(var1, false);
      } else {
         throw new IOException("DER UTC Time length error");
      }
   }

   public Date getGeneralizedTime(int var1) throws IOException {
      if (var1 > this.available()) {
         throw new IOException("short read of DER Generalized Time");
      } else if (var1 >= 13 && var1 <= 23) {
         return this.getTime(var1, true);
      } else {
         throw new IOException("DER Generalized Time length error");
      }
   }

   private Date getTime(int var1, boolean var2) throws IOException {
      String var10 = null;
      int var3;
      if (var2) {
         var10 = "Generalized";
         var3 = 1000 * Character.digit((char)((char)this.buf[this.pos++]), 10);
         var3 += 100 * Character.digit((char)((char)this.buf[this.pos++]), 10);
         var3 += 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
         var3 += Character.digit((char)((char)this.buf[this.pos++]), 10);
         var1 -= 2;
      } else {
         var10 = "UTC";
         var3 = 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
         var3 += Character.digit((char)((char)this.buf[this.pos++]), 10);
         if (var3 < 50) {
            var3 += 2000;
         } else {
            var3 += 1900;
         }
      }

      int var4 = 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
      var4 += Character.digit((char)((char)this.buf[this.pos++]), 10);
      int var5 = 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
      var5 += Character.digit((char)((char)this.buf[this.pos++]), 10);
      int var6 = 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
      var6 += Character.digit((char)((char)this.buf[this.pos++]), 10);
      int var7 = 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
      var7 += Character.digit((char)((char)this.buf[this.pos++]), 10);
      var1 -= 10;
      int var9 = 0;
      int var8;
      if (var1 > 2 && var1 < 12) {
         var8 = 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
         var8 += Character.digit((char)((char)this.buf[this.pos++]), 10);
         var1 -= 2;
         if (this.buf[this.pos] == 46 || this.buf[this.pos] == 44) {
            --var1;
            ++this.pos;
            int var11 = 0;

            for(int var12 = this.pos; this.buf[var12] != 90 && this.buf[var12] != 43 && this.buf[var12] != 45; ++var11) {
               ++var12;
            }

            switch(var11) {
            case 1:
               var9 += 100 * Character.digit((char)((char)this.buf[this.pos++]), 10);
               break;
            case 2:
               var9 += 100 * Character.digit((char)((char)this.buf[this.pos++]), 10);
               var9 += 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
               break;
            case 3:
               var9 += 100 * Character.digit((char)((char)this.buf[this.pos++]), 10);
               var9 += 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
               var9 += Character.digit((char)((char)this.buf[this.pos++]), 10);
               break;
            default:
               throw new IOException("Parse " + var10 + " time, unsupported precision for seconds value");
            }

            var1 -= var11;
         }
      } else {
         var8 = 0;
      }

      if (var4 != 0 && var5 != 0 && var4 <= 12 && var5 <= 31 && var6 < 24 && var7 < 60 && var8 < 60) {
         Gregorian var17 = CalendarSystem.getGregorianCalendar();
         CalendarDate var18 = var17.newCalendarDate((TimeZone)null);
         var18.setDate(var3, var4, var5);
         var18.setTimeOfDay(var6, var7, var8, var9);
         long var13 = var17.getTime(var18);
         if (var1 != 1 && var1 != 5) {
            throw new IOException("Parse " + var10 + " time, invalid offset");
         } else {
            int var15;
            int var16;
            switch(this.buf[this.pos++]) {
            case 43:
               var15 = 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
               var15 += Character.digit((char)((char)this.buf[this.pos++]), 10);
               var16 = 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
               var16 += Character.digit((char)((char)this.buf[this.pos++]), 10);
               if (var15 >= 24 || var16 >= 60) {
                  throw new IOException("Parse " + var10 + " time, +hhmm");
               }

               var13 -= (long)((var15 * 60 + var16) * 60 * 1000);
               break;
            case 45:
               var15 = 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
               var15 += Character.digit((char)((char)this.buf[this.pos++]), 10);
               var16 = 10 * Character.digit((char)((char)this.buf[this.pos++]), 10);
               var16 += Character.digit((char)((char)this.buf[this.pos++]), 10);
               if (var15 >= 24 || var16 >= 60) {
                  throw new IOException("Parse " + var10 + " time, -hhmm");
               }

               var13 += (long)((var15 * 60 + var16) * 60 * 1000);
            case 90:
               break;
            default:
               throw new IOException("Parse " + var10 + " time, garbage offset");
            }

            return new Date(var13);
         }
      } else {
         throw new IOException("Parse " + var10 + " time, invalid format");
      }
   }
}
