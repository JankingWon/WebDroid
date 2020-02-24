package sun.security.x509;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import sun.misc.HexDumpEncoder;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class X509Key implements PublicKey {
   private static final long serialVersionUID = -5359250853002055002L;
   protected AlgorithmId algid;
   /** @deprecated */
   @Deprecated
   protected byte[] key = null;
   /** @deprecated */
   @Deprecated
   private int unusedBits = 0;
   private BitArray bitStringKey = null;
   protected byte[] encodedKey;

   public X509Key() {
   }

   private X509Key(AlgorithmId var1, BitArray var2) throws InvalidKeyException {
      this.algid = var1;
      this.setKey(var2);
      this.encode();
   }

   protected void setKey(BitArray var1) {
      this.bitStringKey = (BitArray)var1.clone();
      this.key = var1.toByteArray();
      int var2 = var1.length() % 8;
      this.unusedBits = var2 == 0 ? 0 : 8 - var2;
   }

   protected BitArray getKey() {
      this.bitStringKey = new BitArray(this.key.length * 8 - this.unusedBits, this.key);
      return (BitArray)this.bitStringKey.clone();
   }

   public static PublicKey parse(DerValue var0) throws IOException {
      if (var0.tag != 48) {
         throw new IOException("corrupt subject key");
      } else {
         AlgorithmId var1 = AlgorithmId.parse(var0.data.getDerValue());

         PublicKey var2;
         try {
            var2 = buildX509Key(var1, var0.data.getUnalignedBitString());
         } catch (InvalidKeyException var4) {
            throw new IOException("subject key, " + var4.getMessage(), var4);
         }

         if (var0.data.available() != 0) {
            throw new IOException("excess subject key");
         } else {
            return var2;
         }
      }
   }

   protected void parseKeyBits() throws IOException, InvalidKeyException {
      this.encode();
   }

   static PublicKey buildX509Key(AlgorithmId var0, BitArray var1) throws IOException, InvalidKeyException {
      DerOutputStream var2 = new DerOutputStream();
      encode(var2, var0, var1);
      X509EncodedKeySpec var3 = new X509EncodedKeySpec(var2.toByteArray());

      try {
         KeyFactory var18 = KeyFactory.getInstance(var0.getName());
         return var18.generatePublic(var3);
      } catch (NoSuchAlgorithmException var15) {
         String var4 = "";

         try {
            Provider var7 = Security.getProvider("SUN");
            if (var7 == null) {
               throw new InstantiationException();
            }

            var4 = var7.getProperty("PublicKey.X.509." + var0.getName());
            if (var4 == null) {
               throw new InstantiationException();
            }

            Class var8 = null;

            try {
               var8 = Class.forName(var4);
            } catch (ClassNotFoundException var11) {
               ClassLoader var10 = ClassLoader.getSystemClassLoader();
               if (var10 != null) {
                  var8 = var10.loadClass(var4);
               }
            }

            Object var9 = null;
            if (var8 != null) {
               var9 = var8.newInstance();
            }

            if (var9 instanceof X509Key) {
               X509Key var17 = (X509Key)var9;
               var17.algid = var0;
               var17.setKey(var1);
               var17.parseKeyBits();
               return var17;
            }
         } catch (ClassNotFoundException var12) {
         } catch (InstantiationException var13) {
         } catch (IllegalAccessException var14) {
            throw new IOException(var4 + " [internal error]");
         }

         X509Key var5 = new X509Key(var0, var1);
         return var5;
      } catch (InvalidKeySpecException var16) {
         throw new InvalidKeyException(var16.getMessage(), var16);
      }
   }

   public String getAlgorithm() {
      return this.algid.getName();
   }

   public AlgorithmId getAlgorithmId() {
      return this.algid;
   }

   public final void encode(DerOutputStream var1) throws IOException {
      encode(var1, this.algid, this.getKey());
   }

   public byte[] getEncoded() {
      try {
         return (byte[])this.getEncodedInternal().clone();
      } catch (InvalidKeyException var2) {
         return null;
      }
   }

   public byte[] getEncodedInternal() throws InvalidKeyException {
      byte[] var1 = this.encodedKey;
      if (var1 == null) {
         try {
            DerOutputStream var2 = new DerOutputStream();
            this.encode(var2);
            var1 = var2.toByteArray();
         } catch (IOException var3) {
            throw new InvalidKeyException("IOException : " + var3.getMessage());
         }

         this.encodedKey = var1;
      }

      return var1;
   }

   public String getFormat() {
      return "X.509";
   }

   public byte[] encode() throws InvalidKeyException {
      return (byte[])this.getEncodedInternal().clone();
   }

   public String toString() {
      HexDumpEncoder var1 = new HexDumpEncoder();
      return "algorithm = " + this.algid.toString() + ", unparsed keybits = \n" + var1.encodeBuffer(this.key);
   }

   public void decode(InputStream var1) throws InvalidKeyException {
      try {
         DerValue var2 = new DerValue(var1);
         if (var2.tag != 48) {
            throw new InvalidKeyException("invalid key format");
         } else {
            this.algid = AlgorithmId.parse(var2.data.getDerValue());
            this.setKey(var2.data.getUnalignedBitString());
            this.parseKeyBits();
            if (var2.data.available() != 0) {
               throw new InvalidKeyException("excess key data");
            }
         }
      } catch (IOException var4) {
         throw new InvalidKeyException("IOException: " + var4.getMessage());
      }
   }

   public void decode(byte[] var1) throws InvalidKeyException {
      this.decode((InputStream)(new ByteArrayInputStream(var1)));
   }

   private void writeObject(ObjectOutputStream var1) throws IOException {
      var1.write(this.getEncoded());
   }

   private void readObject(ObjectInputStream var1) throws IOException {
      try {
         this.decode((InputStream)var1);
      } catch (InvalidKeyException var3) {
         var3.printStackTrace();
         throw new IOException("deserialized key is invalid: " + var3.getMessage());
      }
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof Key)) {
         return false;
      } else {
         try {
            byte[] var2 = this.getEncodedInternal();
            byte[] var3;
            if (var1 instanceof X509Key) {
               var3 = ((X509Key)var1).getEncodedInternal();
            } else {
               var3 = ((Key)var1).getEncoded();
            }

            return Arrays.equals(var2, var3);
         } catch (InvalidKeyException var4) {
            return false;
         }
      }
   }

   public int hashCode() {
      try {
         byte[] var1 = this.getEncodedInternal();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var1.length; ++var3) {
            var2 += (var1[var3] & 255) * 37;
         }

         return var2;
      } catch (InvalidKeyException var4) {
         return 0;
      }
   }

   static void encode(DerOutputStream var0, AlgorithmId var1, BitArray var2) throws IOException {
      DerOutputStream var3 = new DerOutputStream();
      var1.encode(var3);
      var3.putUnalignedBitString(var2);
      var0.write((byte)48, (DerOutputStream)var3);
   }
}
