package sun.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class HexDumpEncoder extends CharacterEncoder {
   private int offset;
   private int thisLineLength;
   private int currentByte;
   private byte[] thisLine = new byte[16];

   static void hexDigit(PrintStream var0, byte var1) {
      char var2 = (char)(var1 >> 4 & 15);
      if (var2 > '\t') {
         var2 = (char)(var2 - 10 + 65);
      } else {
         var2 = (char)(var2 + 48);
      }

      var0.write(var2);
      var2 = (char)(var1 & 15);
      if (var2 > '\t') {
         var2 = (char)(var2 - 10 + 65);
      } else {
         var2 = (char)(var2 + 48);
      }

      var0.write(var2);
   }

   protected int bytesPerAtom() {
      return 1;
   }

   protected int bytesPerLine() {
      return 16;
   }

   protected void encodeBufferPrefix(OutputStream var1) throws IOException {
      this.offset = 0;
      super.encodeBufferPrefix(var1);
   }

   protected void encodeLinePrefix(OutputStream var1, int var2) throws IOException {
      hexDigit(this.pStream, (byte)(this.offset >>> 8 & 255));
      hexDigit(this.pStream, (byte)(this.offset & 255));
      this.pStream.print(": ");
      this.currentByte = 0;
      this.thisLineLength = var2;
   }

   protected void encodeAtom(OutputStream var1, byte[] var2, int var3, int var4) throws IOException {
      this.thisLine[this.currentByte] = var2[var3];
      hexDigit(this.pStream, var2[var3]);
      this.pStream.print(" ");
      ++this.currentByte;
      if (this.currentByte == 8) {
         this.pStream.print("  ");
      }

   }

   protected void encodeLineSuffix(OutputStream var1) throws IOException {
      int var2;
      if (this.thisLineLength < 16) {
         for(var2 = this.thisLineLength; var2 < 16; ++var2) {
            this.pStream.print("   ");
            if (var2 == 7) {
               this.pStream.print("  ");
            }
         }
      }

      this.pStream.print(" ");

      for(var2 = 0; var2 < this.thisLineLength; ++var2) {
         if (this.thisLine[var2] >= 32 && this.thisLine[var2] <= 122) {
            this.pStream.write(this.thisLine[var2]);
         } else {
            this.pStream.print(".");
         }
      }

      this.pStream.println();
      this.offset += this.thisLineLength;
   }
}
