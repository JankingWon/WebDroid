package sun.security.util;

import java.io.IOException;
import java.util.ArrayList;

class DerIndefLenConverter {
   private static final int TAG_MASK = 31;
   private static final int FORM_MASK = 32;
   private static final int CLASS_MASK = 192;
   private static final int LEN_LONG = 128;
   private static final int LEN_MASK = 127;
   private static final int SKIP_EOC_BYTES = 2;
   private byte[] data;
   private byte[] newData;
   private int newDataPos;
   private int dataPos;
   private int dataSize;
   private int index;
   private int unresolved = 0;
   private ArrayList<Object> ndefsList = new ArrayList();
   private int numOfTotalLenBytes = 0;

   private boolean isEOC(int var1) {
      return (var1 & 31) == 0 && (var1 & 32) == 0 && (var1 & 192) == 0;
   }

   static boolean isLongForm(int var0) {
      return (var0 & 128) == 128;
   }

   static boolean isIndefinite(int var0) {
      return isLongForm(var0) && (var0 & 127) == 0;
   }

   private void parseTag() throws IOException {
      if (this.dataPos != this.dataSize) {
         try {
            if (this.isEOC(this.data[this.dataPos]) && this.data[this.dataPos + 1] == 0) {
               int var1 = 0;
               Object var2 = null;

               int var3;
               for(var3 = this.ndefsList.size() - 1; var3 >= 0; --var3) {
                  var2 = this.ndefsList.get(var3);
                  if (var2 instanceof Integer) {
                     break;
                  }

                  var1 += ((byte[])((byte[])var2)).length - 3;
               }

               if (var3 < 0) {
                  throw new IOException("EOC does not have matching indefinite-length tag");
               }

               int var4 = this.dataPos - (Integer)var2 + var1;
               byte[] var5 = this.getLengthBytes(var4);
               this.ndefsList.set(var3, var5);
               --this.unresolved;
               this.numOfTotalLenBytes += var5.length - 3;
            }

            ++this.dataPos;
         } catch (IndexOutOfBoundsException var6) {
            throw new IOException(var6);
         }
      }
   }

   private void writeTag() {
      if (this.dataPos != this.dataSize) {
         byte var1 = this.data[this.dataPos++];
         if (this.isEOC(var1) && this.data[this.dataPos] == 0) {
            ++this.dataPos;
            this.writeTag();
         } else {
            this.newData[this.newDataPos++] = (byte)var1;
         }

      }
   }

   private int parseLength() throws IOException {
      int var1 = 0;
      if (this.dataPos == this.dataSize) {
         return var1;
      } else {
         int var2 = this.data[this.dataPos++] & 255;
         if (isIndefinite(var2)) {
            this.ndefsList.add(new Integer(this.dataPos));
            ++this.unresolved;
            return var1;
         } else {
            if (isLongForm(var2)) {
               var2 &= 127;
               if (var2 > 4) {
                  throw new IOException("Too much data");
               }

               if (this.dataSize - this.dataPos < var2 + 1) {
                  throw new IOException("Too little data");
               }

               for(int var3 = 0; var3 < var2; ++var3) {
                  var1 = (var1 << 8) + (this.data[this.dataPos++] & 255);
               }

               if (var1 < 0) {
                  throw new IOException("Invalid length bytes");
               }
            } else {
               var1 = var2 & 127;
            }

            return var1;
         }
      }
   }

   private void writeLengthAndValue() throws IOException {
      if (this.dataPos != this.dataSize) {
         int var1 = 0;
         int var2 = this.data[this.dataPos++] & 255;
         if (isIndefinite(var2)) {
            byte[] var4 = (byte[])((byte[])this.ndefsList.get(this.index++));
            System.arraycopy(var4, 0, this.newData, this.newDataPos, var4.length);
            this.newDataPos += var4.length;
         } else {
            if (isLongForm(var2)) {
               var2 &= 127;

               for(int var3 = 0; var3 < var2; ++var3) {
                  var1 = (var1 << 8) + (this.data[this.dataPos++] & 255);
               }

               if (var1 < 0) {
                  throw new IOException("Invalid length bytes");
               }
            } else {
               var1 = var2 & 127;
            }

            this.writeLength(var1);
            this.writeValue(var1);
         }
      }
   }

   private void writeLength(int var1) {
      if (var1 < 128) {
         this.newData[this.newDataPos++] = (byte)var1;
      } else if (var1 < 256) {
         this.newData[this.newDataPos++] = -127;
         this.newData[this.newDataPos++] = (byte)var1;
      } else if (var1 < 65536) {
         this.newData[this.newDataPos++] = -126;
         this.newData[this.newDataPos++] = (byte)(var1 >> 8);
         this.newData[this.newDataPos++] = (byte)var1;
      } else if (var1 < 16777216) {
         this.newData[this.newDataPos++] = -125;
         this.newData[this.newDataPos++] = (byte)(var1 >> 16);
         this.newData[this.newDataPos++] = (byte)(var1 >> 8);
         this.newData[this.newDataPos++] = (byte)var1;
      } else {
         this.newData[this.newDataPos++] = -124;
         this.newData[this.newDataPos++] = (byte)(var1 >> 24);
         this.newData[this.newDataPos++] = (byte)(var1 >> 16);
         this.newData[this.newDataPos++] = (byte)(var1 >> 8);
         this.newData[this.newDataPos++] = (byte)var1;
      }

   }

   private byte[] getLengthBytes(int var1) {
      byte var3 = 0;
      byte[] var2;
      int var4;
      if (var1 < 128) {
         var2 = new byte[1];
         var4 = var3 + 1;
         var2[var3] = (byte)var1;
      } else if (var1 < 256) {
         var2 = new byte[2];
         var4 = var3 + 1;
         var2[var3] = -127;
         var2[var4++] = (byte)var1;
      } else if (var1 < 65536) {
         var2 = new byte[3];
         var4 = var3 + 1;
         var2[var3] = -126;
         var2[var4++] = (byte)(var1 >> 8);
         var2[var4++] = (byte)var1;
      } else if (var1 < 16777216) {
         var2 = new byte[4];
         var4 = var3 + 1;
         var2[var3] = -125;
         var2[var4++] = (byte)(var1 >> 16);
         var2[var4++] = (byte)(var1 >> 8);
         var2[var4++] = (byte)var1;
      } else {
         var2 = new byte[5];
         var4 = var3 + 1;
         var2[var3] = -124;
         var2[var4++] = (byte)(var1 >> 24);
         var2[var4++] = (byte)(var1 >> 16);
         var2[var4++] = (byte)(var1 >> 8);
         var2[var4++] = (byte)var1;
      }

      return var2;
   }

   private int getNumOfLenBytes(int var1) {
      boolean var2 = false;
      byte var3;
      if (var1 < 128) {
         var3 = 1;
      } else if (var1 < 256) {
         var3 = 2;
      } else if (var1 < 65536) {
         var3 = 3;
      } else if (var1 < 16777216) {
         var3 = 4;
      } else {
         var3 = 5;
      }

      return var3;
   }

   private void parseValue(int var1) {
      this.dataPos += var1;
   }

   private void writeValue(int var1) {
      for(int var2 = 0; var2 < var1; ++var2) {
         this.newData[this.newDataPos++] = this.data[this.dataPos++];
      }

   }

   byte[] convert(byte[] var1) throws IOException {
      this.data = var1;
      this.dataPos = 0;
      this.index = 0;
      this.dataSize = this.data.length;
      boolean var2 = false;
      int var3 = 0;

      while(this.dataPos < this.dataSize) {
         this.parseTag();
         int var4 = this.parseLength();
         this.parseValue(var4);
         if (this.unresolved == 0) {
            var3 = this.dataSize - this.dataPos;
            this.dataSize = this.dataPos;
            break;
         }
      }

      if (this.unresolved != 0) {
         throw new IOException("not all indef len BER resolved");
      } else {
         this.newData = new byte[this.dataSize + this.numOfTotalLenBytes + var3];
         this.dataPos = 0;
         this.newDataPos = 0;
         this.index = 0;

         while(this.dataPos < this.dataSize) {
            this.writeTag();
            this.writeLengthAndValue();
         }

         System.arraycopy(var1, this.dataSize, this.newData, this.dataSize + this.numOfTotalLenBytes, var3);
         return this.newData;
      }
   }
}
