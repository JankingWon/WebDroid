package sun.security.provider.certpath;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;

import javax.security.auth.x500.X500Principal;

import sun.security.provider.X509Factory;
import sun.security.util.Cache;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.X509CertImpl;

public class X509CertificatePair {
   private static final byte TAG_FORWARD = 0;
   private static final byte TAG_REVERSE = 1;
   private X509Certificate forward;
   private X509Certificate reverse;
   private byte[] encoded;
   private static final Cache<Object, X509CertificatePair> cache = Cache.newSoftMemoryCache(750);

   public X509CertificatePair() {
   }

   public X509CertificatePair(X509Certificate var1, X509Certificate var2) throws CertificateException {
      if (var1 == null && var2 == null) {
         throw new CertificateException("at least one of certificate pair must be non-null");
      } else {
         this.forward = var1;
         this.reverse = var2;
         this.checkPair();
      }
   }

   private X509CertificatePair(byte[] var1) throws CertificateException {
      try {
         this.parse(new DerValue(var1));
         this.encoded = var1;
      } catch (IOException var3) {
         throw new CertificateException(var3.toString());
      }

      this.checkPair();
   }

   public static synchronized void clearCache() {
      cache.clear();
   }

   public static synchronized X509CertificatePair generateCertificatePair(byte[] var0) throws CertificateException {
      Cache.EqualByteArray var1 = new Cache.EqualByteArray(var0);
      X509CertificatePair var2 = (X509CertificatePair)cache.get(var1);
      if (var2 != null) {
         return var2;
      } else {
         var2 = new X509CertificatePair(var0);
         var1 = new Cache.EqualByteArray(var2.encoded);
         cache.put(var1, var2);
         return var2;
      }
   }

   public void setForward(X509Certificate var1) throws CertificateException {
      this.checkPair();
      this.forward = var1;
   }

   public void setReverse(X509Certificate var1) throws CertificateException {
      this.checkPair();
      this.reverse = var1;
   }

   public X509Certificate getForward() {
      return this.forward;
   }

   public X509Certificate getReverse() {
      return this.reverse;
   }

   public byte[] getEncoded() throws CertificateEncodingException {
      try {
         if (this.encoded == null) {
            DerOutputStream var1 = new DerOutputStream();
            this.emit(var1);
            this.encoded = var1.toByteArray();
         }
      } catch (IOException var2) {
         throw new CertificateEncodingException(var2.toString());
      }

      return this.encoded;
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder();
      var1.append("X.509 Certificate Pair: [\n");
      if (this.forward != null) {
         var1.append("  Forward: ").append((Object)this.forward).append("\n");
      }

      if (this.reverse != null) {
         var1.append("  Reverse: ").append((Object)this.reverse).append("\n");
      }

      var1.append("]");
      return var1.toString();
   }

   private void parse(DerValue var1) throws IOException, CertificateException {
      if (var1.tag != 48) {
         throw new IOException("Sequence tag missing for X509CertificatePair");
      } else {
         while(var1.data != null && var1.data.available() != 0) {
            DerValue var2 = var1.data.getDerValue();
            short var3 = (short)((byte)(var2.tag & 31));
            switch(var3) {
            case 0:
               if (var2.isContextSpecific() && var2.isConstructed()) {
                  if (this.forward != null) {
                     throw new IOException("Duplicate forward certificate in X509CertificatePair");
                  }

                  var2 = var2.data.getDerValue();
                  this.forward = X509Factory.intern((X509Certificate)(new X509CertImpl(var2.toByteArray())));
               }
               break;
            case 1:
               if (var2.isContextSpecific() && var2.isConstructed()) {
                  if (this.reverse != null) {
                     throw new IOException("Duplicate reverse certificate in X509CertificatePair");
                  }

                  var2 = var2.data.getDerValue();
                  this.reverse = X509Factory.intern((X509Certificate)(new X509CertImpl(var2.toByteArray())));
               }
               break;
            default:
               throw new IOException("Invalid encoding of X509CertificatePair");
            }
         }

         if (this.forward == null && this.reverse == null) {
            throw new CertificateException("at least one of certificate pair must be non-null");
         }
      }
   }

   private void emit(DerOutputStream var1) throws IOException, CertificateEncodingException {
      DerOutputStream var2 = new DerOutputStream();
      DerOutputStream var3;
      if (this.forward != null) {
         var3 = new DerOutputStream();
         var3.putDerValue(new DerValue(this.forward.getEncoded()));
         var2.write(DerValue.createTag((byte)-128, true, (byte)0), var3);
      }

      if (this.reverse != null) {
         var3 = new DerOutputStream();
         var3.putDerValue(new DerValue(this.reverse.getEncoded()));
         var2.write(DerValue.createTag((byte)-128, true, (byte)1), var3);
      }

      var1.write((byte)48, (DerOutputStream)var2);
   }

   private void checkPair() throws CertificateException {
      if (this.forward != null && this.reverse != null) {
         X500Principal var1 = this.forward.getSubjectX500Principal();
         X500Principal var2 = this.forward.getIssuerX500Principal();
         X500Principal var3 = this.reverse.getSubjectX500Principal();
         X500Principal var4 = this.reverse.getIssuerX500Principal();
         if (var2.equals(var3) && var4.equals(var1)) {
            try {
               PublicKey var5 = this.reverse.getPublicKey();
               if (!(var5 instanceof DSAPublicKey) || ((DSAPublicKey)var5).getParams() != null) {
                  this.forward.verify(var5);
               }

               var5 = this.forward.getPublicKey();
               if (!(var5 instanceof DSAPublicKey) || ((DSAPublicKey)var5).getParams() != null) {
                  this.reverse.verify(var5);
               }

            } catch (GeneralSecurityException var6) {
               throw new CertificateException("invalid signature: " + var6.getMessage());
            }
         } else {
            throw new CertificateException("subject and issuer names in forward and reverse certificates do not match");
         }
      }
   }
}
