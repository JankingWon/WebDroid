package sun.security.x509;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import sun.misc.HexDumpEncoder;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class IPAddressName implements GeneralNameInterface {
   private byte[] address;
   private boolean isIPv4;
   private String name;
   private static final int MASKSIZE = 16;

   public IPAddressName(DerValue var1) throws IOException {
      this(var1.getOctetString());
   }

   public IPAddressName(byte[] var1) throws IOException {
      if (var1.length != 4 && var1.length != 8) {
         if (var1.length != 16 && var1.length != 32) {
            throw new IOException("Invalid IPAddressName");
         }

         this.isIPv4 = false;
      } else {
         this.isIPv4 = true;
      }

      this.address = var1;
   }

   public IPAddressName(String var1) throws IOException {
      if (var1 != null && var1.length() != 0) {
         if (var1.charAt(var1.length() - 1) == '/') {
            throw new IOException("Invalid IPAddress: " + var1);
         } else {
            if (var1.indexOf(58) >= 0) {
               this.parseIPv6(var1);
               this.isIPv4 = false;
            } else {
               if (var1.indexOf(46) < 0) {
                  throw new IOException("Invalid IPAddress: " + var1);
               }

               this.parseIPv4(var1);
               this.isIPv4 = true;
            }

         }
      } else {
         throw new IOException("IPAddress cannot be null or empty");
      }
   }

   private void parseIPv4(String var1) throws IOException {
      int var2 = var1.indexOf(47);
      if (var2 == -1) {
         this.address = InetAddress.getByName(var1).getAddress();
      } else {
         this.address = new byte[8];
         byte[] var3 = InetAddress.getByName(var1.substring(var2 + 1)).getAddress();
         byte[] var4 = InetAddress.getByName(var1.substring(0, var2)).getAddress();
         System.arraycopy(var4, 0, this.address, 0, 4);
         System.arraycopy(var3, 0, this.address, 4, 4);
      }

   }

   private void parseIPv6(String var1) throws IOException {
      int var2 = var1.indexOf(47);
      if (var2 == -1) {
         this.address = InetAddress.getByName(var1).getAddress();
      } else {
         this.address = new byte[32];
         byte[] var3 = InetAddress.getByName(var1.substring(0, var2)).getAddress();
         System.arraycopy(var3, 0, this.address, 0, 16);
         int var4 = Integer.parseInt(var1.substring(var2 + 1));
         if (var4 < 0 || var4 > 128) {
            throw new IOException("IPv6Address prefix length (" + var4 + ") in out of valid range [0,128]");
         }

         BitArray var5 = new BitArray(128);

         for(int var6 = 0; var6 < var4; ++var6) {
            var5.set(var6, true);
         }

         byte[] var8 = var5.toByteArray();

         for(int var7 = 0; var7 < 16; ++var7) {
            this.address[16 + var7] = var8[var7];
         }
      }

   }

   public int getType() {
      return 7;
   }

   public void encode(DerOutputStream var1) throws IOException {
      var1.putOctetString(this.address);
   }

   public String toString() {
      try {
         return "IPAddress: " + this.getName();
      } catch (IOException var3) {
         HexDumpEncoder var2 = new HexDumpEncoder();
         return "IPAddress: " + var2.encodeBuffer(this.address);
      }
   }

   public String getName() throws IOException {
      if (this.name != null) {
         return this.name;
      } else {
         byte[] var1;
         byte[] var2;
         if (this.isIPv4) {
            var1 = new byte[4];
            System.arraycopy(this.address, 0, var1, 0, 4);
            this.name = InetAddress.getByAddress(var1).getHostAddress();
            if (this.address.length == 8) {
               var2 = new byte[4];
               System.arraycopy(this.address, 4, var2, 0, 4);
               this.name = this.name + "/" + InetAddress.getByAddress(var2).getHostAddress();
            }
         } else {
            var1 = new byte[16];
            System.arraycopy(this.address, 0, var1, 0, 16);
            this.name = InetAddress.getByAddress(var1).getHostAddress();
            if (this.address.length == 32) {
               var2 = new byte[16];

               for(int var3 = 16; var3 < 32; ++var3) {
                  var2[var3 - 16] = this.address[var3];
               }

               BitArray var5 = new BitArray(128, var2);

               int var4;
               for(var4 = 0; var4 < 128 && var5.get(var4); ++var4) {
               }

               for(this.name = this.name + "/" + var4; var4 < 128; ++var4) {
                  if (var5.get(var4)) {
                     throw new IOException("Invalid IPv6 subdomain - set bit " + var4 + " not contiguous");
                  }
               }
            }
         }

         return this.name;
      }
   }

   public byte[] getBytes() {
      return (byte[])this.address.clone();
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof IPAddressName)) {
         return false;
      } else {
         IPAddressName var2 = (IPAddressName)var1;
         byte[] var3 = var2.address;
         if (var3.length != this.address.length) {
            return false;
         } else if (this.address.length != 8 && this.address.length != 32) {
            return Arrays.equals(var3, this.address);
         } else {
            int var4 = this.address.length / 2;

            int var5;
            for(var5 = 0; var5 < var4; ++var5) {
               byte var6 = (byte)(this.address[var5] & this.address[var5 + var4]);
               byte var7 = (byte)(var3[var5] & var3[var5 + var4]);
               if (var6 != var7) {
                  return false;
               }
            }

            for(var5 = var4; var5 < this.address.length; ++var5) {
               if (this.address[var5] != var3[var5]) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public int hashCode() {
      int var1 = 0;

      for(int var2 = 0; var2 < this.address.length; ++var2) {
         var1 += this.address[var2] * var2;
      }

      return var1;
   }

   public int constrains(GeneralNameInterface var1) throws UnsupportedOperationException {
      byte var2;
      if (var1 == null) {
         var2 = -1;
      } else if (var1.getType() != 7) {
         var2 = -1;
      } else if (((IPAddressName)var1).equals(this)) {
         var2 = 0;
      } else {
         IPAddressName var3 = (IPAddressName)var1;
         byte[] var4 = var3.address;
         if (var4.length == 4 && this.address.length == 4) {
            var2 = 3;
         } else if (var4.length == 8 && this.address.length == 8 || var4.length == 32 && this.address.length == 32) {
            boolean var11 = true;
            boolean var12 = true;
            boolean var7 = false;
            boolean var8 = false;
            int var9 = this.address.length / 2;

            for(int var10 = 0; var10 < var9; ++var10) {
               if ((byte)(this.address[var10] & this.address[var10 + var9]) != this.address[var10]) {
                  var7 = true;
               }

               if ((byte)(var4[var10] & var4[var10 + var9]) != var4[var10]) {
                  var8 = true;
               }

               if ((byte)(this.address[var10 + var9] & var4[var10 + var9]) != this.address[var10 + var9] || (byte)(this.address[var10] & this.address[var10 + var9]) != (byte)(var4[var10] & this.address[var10 + var9])) {
                  var11 = false;
               }

               if ((byte)(var4[var10 + var9] & this.address[var10 + var9]) != var4[var10 + var9] || (byte)(var4[var10] & var4[var10 + var9]) != (byte)(this.address[var10] & var4[var10 + var9])) {
                  var12 = false;
               }
            }

            if (!var7 && !var8) {
               if (var11) {
                  var2 = 1;
               } else if (var12) {
                  var2 = 2;
               } else {
                  var2 = 3;
               }
            } else if (var7 && var8) {
               var2 = 0;
            } else if (var7) {
               var2 = 2;
            } else {
               var2 = 1;
            }
         } else {
            int var5;
            int var6;
            if (var4.length != 8 && var4.length != 32) {
               if (this.address.length != 8 && this.address.length != 32) {
                  var2 = 3;
               } else {
                  var5 = 0;

                  for(var6 = this.address.length / 2; var5 < var6 && (var4[var5] & this.address[var5 + var6]) == this.address[var5]; ++var5) {
                  }

                  if (var5 == var6) {
                     var2 = 1;
                  } else {
                     var2 = 3;
                  }
               }
            } else {
               var5 = 0;

               for(var6 = var4.length / 2; var5 < var6 && (this.address[var5] & var4[var5 + var6]) == var4[var5]; ++var5) {
               }

               if (var5 == var6) {
                  var2 = 2;
               } else {
                  var2 = 3;
               }
            }
         }
      }

      return var2;
   }

   public int subtreeDepth() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("subtreeDepth() not defined for IPAddressName");
   }
}
