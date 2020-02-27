package sun.security.util;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

import sun.security.jca.JCAUtil;

public final class KeyUtil {
   public static final int getKeySize(Key var0) {
      int var1 = -1;
      if (var0 instanceof Length) {
         try {
            Length var2 = (Length)var0;
            var1 = var2.length();
         } catch (UnsupportedOperationException var4) {
         }

         if (var1 >= 0) {
            return var1;
         }
      }

      if (var0 instanceof SecretKey) {
         SecretKey var5 = (SecretKey)var0;
         String var3 = var5.getFormat();
         if ("RAW".equals(var3) && var5.getEncoded() != null) {
            var1 = var5.getEncoded().length * 8;
         }
      } else if (var0 instanceof RSAKey) {
         RSAKey var6 = (RSAKey)var0;
         var1 = var6.getModulus().bitLength();
      } else if (var0 instanceof ECKey) {
         ECKey var7 = (ECKey)var0;
         var1 = var7.getParams().getOrder().bitLength();
      } else if (var0 instanceof DSAKey) {
         DSAKey var8 = (DSAKey)var0;
         DSAParams var9 = var8.getParams();
         var1 = var9 != null ? var9.getP().bitLength() : -1;
      } else if (var0 instanceof DHKey) {
         DHKey var10 = (DHKey)var0;
         var1 = var10.getParams().getP().bitLength();
      }

      return var1;
   }

   public static final int getKeySize(AlgorithmParameters var0) {
      String var1 = var0.getAlgorithm();
      byte var3 = -1;
      switch(var1.hashCode()) {
      case -1976312388:
         if (var1.equals("DiffieHellman")) {
            var3 = 1;
         }
         break;
      case 2206:
         if (var1.equals("EC")) {
            var3 = 0;
         }
      }

      switch(var3) {
      case 0:
         try {
            ECKeySizeParameterSpec var8 = (ECKeySizeParameterSpec)var0.getParameterSpec(ECKeySizeParameterSpec.class);
            if (var8 != null) {
               return var8.getKeySize();
            }
         } catch (InvalidParameterSpecException var7) {
         }

         try {
            ECParameterSpec var9 = (ECParameterSpec)var0.getParameterSpec(ECParameterSpec.class);
            if (var9 != null) {
               return var9.getOrder().bitLength();
            }
         } catch (InvalidParameterSpecException var6) {
         }
         break;
      case 1:
         try {
            DHParameterSpec var4 = (DHParameterSpec)var0.getParameterSpec(DHParameterSpec.class);
            if (var4 != null) {
               return var4.getP().bitLength();
            }
         } catch (InvalidParameterSpecException var5) {
         }
      }

      return -1;
   }

   public static final void validate(Key var0) throws InvalidKeyException {
      if (var0 == null) {
         throw new NullPointerException("The key to be validated cannot be null");
      } else {
         if (var0 instanceof DHPublicKey) {
            validateDHPublicKey((DHPublicKey)var0);
         }

      }
   }

   public static final void validate(KeySpec var0) throws InvalidKeyException {
      if (var0 == null) {
         throw new NullPointerException("The key spec to be validated cannot be null");
      } else {
         if (var0 instanceof DHPublicKeySpec) {
            validateDHPublicKey((DHPublicKeySpec)var0);
         }

      }
   }

   public static final boolean isOracleJCEProvider(String var0) {
      return var0 != null && (var0.equals("SunJCE") || var0.equals("SunMSCAPI") || var0.equals("OracleUcrypto") || var0.startsWith("SunPKCS11"));
   }

   public static byte[] checkTlsPreMasterSecretKey(int var0, int var1, SecureRandom var2, byte[] var3, boolean var4) {
      if (var2 == null) {
         var2 = JCAUtil.getSecureRandom();
      }

      byte[] var5 = new byte[48];
      var2.nextBytes(var5);
      if (!var4 && var3 != null) {
         if (var3.length != 48) {
            return var5;
         } else {
            int var6 = (var3[0] & 255) << 8 | var3[1] & 255;
            if (var0 != var6 && (var0 > 769 || var1 != var6)) {
               var3 = var5;
            }

            return var3;
         }
      } else {
         return var5;
      }
   }

   private static void validateDHPublicKey(DHPublicKey var0) throws InvalidKeyException {
      DHParameterSpec var1 = var0.getParams();
      BigInteger var2 = var1.getP();
      BigInteger var3 = var1.getG();
      BigInteger var4 = var0.getY();
      validateDHPublicKey(var2, var3, var4);
   }

   private static void validateDHPublicKey(DHPublicKeySpec var0) throws InvalidKeyException {
      validateDHPublicKey(var0.getP(), var0.getG(), var0.getY());
   }

   private static void validateDHPublicKey(BigInteger var0, BigInteger var1, BigInteger var2) throws InvalidKeyException {
      BigInteger var3 = BigInteger.ONE;
      BigInteger var4 = var0.subtract(BigInteger.ONE);
      if (var2.compareTo(var3) <= 0) {
         throw new InvalidKeyException("Diffie-Hellman public key is too small");
      } else if (var2.compareTo(var4) >= 0) {
         throw new InvalidKeyException("Diffie-Hellman public key is too large");
      } else {
         BigInteger var5 = var0.remainder(var2);
         if (var5.equals(BigInteger.ZERO)) {
            throw new InvalidKeyException("Invalid Diffie-Hellman parameters");
         }
      }
   }

   public static byte[] trimZeroes(byte[] var0) {
      int var1;
      for(var1 = 0; var1 < var0.length - 1 && var0[var1] == 0; ++var1) {
      }

      if (var1 == 0) {
         return var0;
      } else {
         byte[] var2 = new byte[var0.length - var1];
         System.arraycopy(var0, var1, var2, 0, var2.length);
         return var2;
      }
   }
}
