package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class AlgorithmId implements Serializable, DerEncoder {
   private static final long serialVersionUID = 7205873507486557157L;
   private ObjectIdentifier algid;
   private AlgorithmParameters algParams;
   private boolean constructedFromDer = true;
   protected DerValue params;
   private static boolean initOidTable = false;
   private static Map<String, ObjectIdentifier> oidTable;
   private static final Map<ObjectIdentifier, String> nameTable;
   public static final ObjectIdentifier MD2_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 2, 2});
   public static final ObjectIdentifier MD5_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 2, 5});
   public static final ObjectIdentifier SHA_oid = ObjectIdentifier.newInternal(new int[]{1, 3, 14, 3, 2, 26});
   public static final ObjectIdentifier SHA224_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 4});
   public static final ObjectIdentifier SHA256_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 1});
   public static final ObjectIdentifier SHA384_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 2});
   public static final ObjectIdentifier SHA512_oid = ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 101, 3, 4, 2, 3});
   private static final int[] DH_data = new int[]{1, 2, 840, 113549, 1, 3, 1};
   private static final int[] DH_PKIX_data = new int[]{1, 2, 840, 10046, 2, 1};
   private static final int[] DSA_OIW_data = new int[]{1, 3, 14, 3, 2, 12};
   private static final int[] DSA_PKIX_data = new int[]{1, 2, 840, 10040, 4, 1};
   private static final int[] RSA_data = new int[]{2, 5, 8, 1, 1};
   private static final int[] RSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 1};
   public static final ObjectIdentifier DH_oid;
   public static final ObjectIdentifier DH_PKIX_oid;
   public static final ObjectIdentifier DSA_oid;
   public static final ObjectIdentifier DSA_OIW_oid;
   public static final ObjectIdentifier EC_oid = oid(1, 2, 840, 10045, 2, 1);
   public static final ObjectIdentifier ECDH_oid = oid(1, 3, 132, 1, 12);
   public static final ObjectIdentifier RSA_oid;
   public static final ObjectIdentifier RSAEncryption_oid;
   public static final ObjectIdentifier AES_oid = oid(2, 16, 840, 1, 101, 3, 4, 1);
   private static final int[] md2WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 2};
   private static final int[] md5WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 4};
   private static final int[] sha1WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 5};
   private static final int[] sha1WithRSAEncryption_OIW_data = new int[]{1, 3, 14, 3, 2, 29};
   private static final int[] sha224WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 14};
   private static final int[] sha256WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 11};
   private static final int[] sha384WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 12};
   private static final int[] sha512WithRSAEncryption_data = new int[]{1, 2, 840, 113549, 1, 1, 13};
   private static final int[] shaWithDSA_OIW_data = new int[]{1, 3, 14, 3, 2, 13};
   private static final int[] sha1WithDSA_OIW_data = new int[]{1, 3, 14, 3, 2, 27};
   private static final int[] dsaWithSHA1_PKIX_data = new int[]{1, 2, 840, 10040, 4, 3};
   public static final ObjectIdentifier md2WithRSAEncryption_oid;
   public static final ObjectIdentifier md5WithRSAEncryption_oid;
   public static final ObjectIdentifier sha1WithRSAEncryption_oid;
   public static final ObjectIdentifier sha1WithRSAEncryption_OIW_oid;
   public static final ObjectIdentifier sha224WithRSAEncryption_oid;
   public static final ObjectIdentifier sha256WithRSAEncryption_oid;
   public static final ObjectIdentifier sha384WithRSAEncryption_oid;
   public static final ObjectIdentifier sha512WithRSAEncryption_oid;
   public static final ObjectIdentifier shaWithDSA_OIW_oid;
   public static final ObjectIdentifier sha1WithDSA_OIW_oid;
   public static final ObjectIdentifier sha1WithDSA_oid;
   public static final ObjectIdentifier sha224WithDSA_oid = oid(2, 16, 840, 1, 101, 3, 4, 3, 1);
   public static final ObjectIdentifier sha256WithDSA_oid = oid(2, 16, 840, 1, 101, 3, 4, 3, 2);
   public static final ObjectIdentifier sha1WithECDSA_oid = oid(1, 2, 840, 10045, 4, 1);
   public static final ObjectIdentifier sha224WithECDSA_oid = oid(1, 2, 840, 10045, 4, 3, 1);
   public static final ObjectIdentifier sha256WithECDSA_oid = oid(1, 2, 840, 10045, 4, 3, 2);
   public static final ObjectIdentifier sha384WithECDSA_oid = oid(1, 2, 840, 10045, 4, 3, 3);
   public static final ObjectIdentifier sha512WithECDSA_oid = oid(1, 2, 840, 10045, 4, 3, 4);
   public static final ObjectIdentifier specifiedWithECDSA_oid = oid(1, 2, 840, 10045, 4, 3);
   public static final ObjectIdentifier pbeWithMD5AndDES_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 3});
   public static final ObjectIdentifier pbeWithMD5AndRC2_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 6});
   public static final ObjectIdentifier pbeWithSHA1AndDES_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 10});
   public static final ObjectIdentifier pbeWithSHA1AndRC2_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 5, 11});
   public static ObjectIdentifier pbeWithSHA1AndDESede_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 12, 1, 3});
   public static ObjectIdentifier pbeWithSHA1AndRC2_40_oid = ObjectIdentifier.newInternal(new int[]{1, 2, 840, 113549, 1, 12, 1, 6});

   /** @deprecated */
   @Deprecated
   public AlgorithmId() {
   }

   public AlgorithmId(ObjectIdentifier var1) {
      this.algid = var1;
   }

   public AlgorithmId(ObjectIdentifier var1, AlgorithmParameters var2) {
      this.algid = var1;
      this.algParams = var2;
      this.constructedFromDer = false;
   }

   private AlgorithmId(ObjectIdentifier var1, DerValue var2) throws IOException {
      this.algid = var1;
      this.params = var2;
      if (this.params != null) {
         this.decodeParams();
      }

   }

   protected void decodeParams() throws IOException {
      String var1 = this.algid.toString();

      try {
         this.algParams = AlgorithmParameters.getInstance(var1);
      } catch (NoSuchAlgorithmException var3) {
         this.algParams = null;
         return;
      }

      this.algParams.init(this.params.toByteArray());
   }

   public final void encode(DerOutputStream var1) throws IOException {
      this.derEncode(var1);
   }

   public void derEncode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      DerOutputStream var3 = new DerOutputStream();
      var2.putOID(this.algid);
      if (!this.constructedFromDer) {
         if (this.algParams != null) {
            this.params = new DerValue(this.algParams.getEncoded());
         } else {
            this.params = null;
         }
      }

      if (this.params == null) {
         var2.putNull();
      } else {
         var2.putDerValue(this.params);
      }

      var3.write((byte)48, (DerOutputStream)var2);
      var1.write(var3.toByteArray());
   }

   public final byte[] encode() throws IOException {
      DerOutputStream var1 = new DerOutputStream();
      this.derEncode(var1);
      return var1.toByteArray();
   }

   public final ObjectIdentifier getOID() {
      return this.algid;
   }

   public String getName() {
      String var1 = (String)nameTable.get(this.algid);
      if (var1 != null) {
         return var1;
      } else {
         if (this.params != null && this.algid.equals((Object)specifiedWithECDSA_oid)) {
            try {
               AlgorithmId var2 = parse(new DerValue(this.getEncodedParams()));
               String var3 = var2.getName();
               var1 = makeSigAlg(var3, "EC");
            } catch (IOException var4) {
            }
         }

         return var1 == null ? this.algid.toString() : var1;
      }
   }

   public AlgorithmParameters getParameters() {
      return this.algParams;
   }

   public byte[] getEncodedParams() throws IOException {
      return this.params == null ? null : this.params.toByteArray();
   }

   public boolean equals(AlgorithmId var1) {
      boolean var2 = this.params == null ? var1.params == null : this.params.equals(var1.params);
      return this.algid.equals((Object)var1.algid) && var2;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 instanceof AlgorithmId) {
         return this.equals((AlgorithmId)var1);
      } else {
         return var1 instanceof ObjectIdentifier ? this.equals((ObjectIdentifier)var1) : false;
      }
   }

   public final boolean equals(ObjectIdentifier var1) {
      return this.algid.equals((Object)var1);
   }

   public int hashCode() {
      StringBuilder var1 = new StringBuilder();
      var1.append(this.algid.toString());
      var1.append(this.paramsToString());
      return var1.toString().hashCode();
   }

   protected String paramsToString() {
      if (this.params == null) {
         return "";
      } else {
         return this.algParams != null ? this.algParams.toString() : ", params unparsed";
      }
   }

   public String toString() {
      return this.getName() + this.paramsToString();
   }

   public static AlgorithmId parse(DerValue var0) throws IOException {
      if (var0.tag != 48) {
         throw new IOException("algid parse error, not a sequence");
      } else {
         DerInputStream var3 = var0.toDerInputStream();
         ObjectIdentifier var1 = var3.getOID();
         DerValue var2;
         if (var3.available() == 0) {
            var2 = null;
         } else {
            var2 = var3.getDerValue();
            if (var2.tag == 5) {
               if (var2.length() != 0) {
                  throw new IOException("invalid NULL");
               }

               var2 = null;
            }

            if (var3.available() != 0) {
               throw new IOException("Invalid AlgorithmIdentifier: extra data");
            }
         }

         return new AlgorithmId(var1, var2);
      }
   }

   /** @deprecated */
   @Deprecated
   public static AlgorithmId getAlgorithmId(String var0) throws NoSuchAlgorithmException {
      return get(var0);
   }

   public static AlgorithmId get(String var0) throws NoSuchAlgorithmException {
      ObjectIdentifier var1;
      try {
         var1 = algOID(var0);
      } catch (IOException var3) {
         throw new NoSuchAlgorithmException("Invalid ObjectIdentifier " + var0);
      }

      if (var1 == null) {
         throw new NoSuchAlgorithmException("unrecognized algorithm name: " + var0);
      } else {
         return new AlgorithmId(var1);
      }
   }

   public static AlgorithmId get(AlgorithmParameters var0) throws NoSuchAlgorithmException {
      String var2 = var0.getAlgorithm();

      ObjectIdentifier var1;
      try {
         var1 = algOID(var2);
      } catch (IOException var4) {
         throw new NoSuchAlgorithmException("Invalid ObjectIdentifier " + var2);
      }

      if (var1 == null) {
         throw new NoSuchAlgorithmException("unrecognized algorithm name: " + var2);
      } else {
         return new AlgorithmId(var1, var0);
      }
   }

   private static ObjectIdentifier algOID(String var0) throws IOException {
      if (var0.indexOf(46) != -1) {
         return var0.startsWith("OID.") ? new ObjectIdentifier(var0.substring("OID.".length())) : new ObjectIdentifier(var0);
      } else if (var0.equalsIgnoreCase("MD5")) {
         return MD5_oid;
      } else if (var0.equalsIgnoreCase("MD2")) {
         return MD2_oid;
      } else if (!var0.equalsIgnoreCase("SHA") && !var0.equalsIgnoreCase("SHA1") && !var0.equalsIgnoreCase("SHA-1")) {
         if (!var0.equalsIgnoreCase("SHA-256") && !var0.equalsIgnoreCase("SHA256")) {
            if (!var0.equalsIgnoreCase("SHA-384") && !var0.equalsIgnoreCase("SHA384")) {
               if (!var0.equalsIgnoreCase("SHA-512") && !var0.equalsIgnoreCase("SHA512")) {
                  if (!var0.equalsIgnoreCase("SHA-224") && !var0.equalsIgnoreCase("SHA224")) {
                     if (var0.equalsIgnoreCase("RSA")) {
                        return RSAEncryption_oid;
                     } else if (!var0.equalsIgnoreCase("Diffie-Hellman") && !var0.equalsIgnoreCase("DH")) {
                        if (var0.equalsIgnoreCase("DSA")) {
                           return DSA_oid;
                        } else if (var0.equalsIgnoreCase("EC")) {
                           return EC_oid;
                        } else if (var0.equalsIgnoreCase("ECDH")) {
                           return ECDH_oid;
                        } else if (var0.equalsIgnoreCase("AES")) {
                           return AES_oid;
                        } else if (!var0.equalsIgnoreCase("MD5withRSA") && !var0.equalsIgnoreCase("MD5/RSA")) {
                           if (!var0.equalsIgnoreCase("MD2withRSA") && !var0.equalsIgnoreCase("MD2/RSA")) {
                              if (!var0.equalsIgnoreCase("SHAwithDSA") && !var0.equalsIgnoreCase("SHA1withDSA") && !var0.equalsIgnoreCase("SHA/DSA") && !var0.equalsIgnoreCase("SHA1/DSA") && !var0.equalsIgnoreCase("DSAWithSHA1") && !var0.equalsIgnoreCase("DSS") && !var0.equalsIgnoreCase("SHA-1/DSA")) {
                                 if (var0.equalsIgnoreCase("SHA224WithDSA")) {
                                    return sha224WithDSA_oid;
                                 } else if (var0.equalsIgnoreCase("SHA256WithDSA")) {
                                    return sha256WithDSA_oid;
                                 } else if (!var0.equalsIgnoreCase("SHA1WithRSA") && !var0.equalsIgnoreCase("SHA1/RSA")) {
                                    if (!var0.equalsIgnoreCase("SHA1withECDSA") && !var0.equalsIgnoreCase("ECDSA")) {
                                       if (var0.equalsIgnoreCase("SHA224withECDSA")) {
                                          return sha224WithECDSA_oid;
                                       } else if (var0.equalsIgnoreCase("SHA256withECDSA")) {
                                          return sha256WithECDSA_oid;
                                       } else if (var0.equalsIgnoreCase("SHA384withECDSA")) {
                                          return sha384WithECDSA_oid;
                                       } else if (var0.equalsIgnoreCase("SHA512withECDSA")) {
                                          return sha512WithECDSA_oid;
                                       } else {
                                          if (!initOidTable) {
                                             Provider[] var2 = Security.getProviders();

                                             for(int var3 = 0; var3 < var2.length; ++var3) {
                                                Enumeration var4 = var2[var3].keys();

                                                while(var4.hasMoreElements()) {
                                                   String var5 = (String)var4.nextElement();
                                                   String var6 = var5.toUpperCase(Locale.ENGLISH);
                                                   int var7;
                                                   if (var6.startsWith("ALG.ALIAS") && (var7 = var6.indexOf("OID.", 0)) != -1) {
                                                      var7 += "OID.".length();
                                                      if (var7 == var5.length()) {
                                                         break;
                                                      }

                                                      if (oidTable == null) {
                                                         oidTable = new HashMap();
                                                      }

                                                      String var1 = var5.substring(var7);
                                                      String var8 = var2[var3].getProperty(var5);
                                                      if (var8 != null) {
                                                         var8 = var8.toUpperCase(Locale.ENGLISH);
                                                      }

                                                      if (var8 != null && oidTable.get(var8) == null) {
                                                         oidTable.put(var8, new ObjectIdentifier(var1));
                                                      }
                                                   }
                                                }
                                             }

                                             if (oidTable == null) {
                                                oidTable = Collections.emptyMap();
                                             }

                                             initOidTable = true;
                                          }

                                          return (ObjectIdentifier)oidTable.get(var0.toUpperCase(Locale.ENGLISH));
                                       }
                                    } else {
                                       return sha1WithECDSA_oid;
                                    }
                                 } else {
                                    return sha1WithRSAEncryption_oid;
                                 }
                              } else {
                                 return sha1WithDSA_oid;
                              }
                           } else {
                              return md2WithRSAEncryption_oid;
                           }
                        } else {
                           return md5WithRSAEncryption_oid;
                        }
                     } else {
                        return DH_oid;
                     }
                  } else {
                     return SHA224_oid;
                  }
               } else {
                  return SHA512_oid;
               }
            } else {
               return SHA384_oid;
            }
         } else {
            return SHA256_oid;
         }
      } else {
         return SHA_oid;
      }
   }

   private static ObjectIdentifier oid(int... var0) {
      return ObjectIdentifier.newInternal(var0);
   }

   public static String makeSigAlg(String var0, String var1) {
      var0 = var0.replace("-", "");
      if (var1.equalsIgnoreCase("EC")) {
         var1 = "ECDSA";
      }

      return var0 + "with" + var1;
   }

   public static String getEncAlgFromSigAlg(String var0) {
      var0 = var0.toUpperCase(Locale.ENGLISH);
      int var1 = var0.indexOf("WITH");
      String var2 = null;
      if (var1 > 0) {
         int var3 = var0.indexOf("AND", var1 + 4);
         if (var3 > 0) {
            var2 = var0.substring(var1 + 4, var3);
         } else {
            var2 = var0.substring(var1 + 4);
         }

         if (var2.equalsIgnoreCase("ECDSA")) {
            var2 = "EC";
         }
      }

      return var2;
   }

   public static String getDigAlgFromSigAlg(String var0) {
      var0 = var0.toUpperCase(Locale.ENGLISH);
      int var1 = var0.indexOf("WITH");
      return var1 > 0 ? var0.substring(0, var1) : null;
   }

   static {
      DH_oid = ObjectIdentifier.newInternal(DH_data);
      DH_PKIX_oid = ObjectIdentifier.newInternal(DH_PKIX_data);
      DSA_OIW_oid = ObjectIdentifier.newInternal(DSA_OIW_data);
      DSA_oid = ObjectIdentifier.newInternal(DSA_PKIX_data);
      RSA_oid = ObjectIdentifier.newInternal(RSA_data);
      RSAEncryption_oid = ObjectIdentifier.newInternal(RSAEncryption_data);
      md2WithRSAEncryption_oid = ObjectIdentifier.newInternal(md2WithRSAEncryption_data);
      md5WithRSAEncryption_oid = ObjectIdentifier.newInternal(md5WithRSAEncryption_data);
      sha1WithRSAEncryption_oid = ObjectIdentifier.newInternal(sha1WithRSAEncryption_data);
      sha1WithRSAEncryption_OIW_oid = ObjectIdentifier.newInternal(sha1WithRSAEncryption_OIW_data);
      sha224WithRSAEncryption_oid = ObjectIdentifier.newInternal(sha224WithRSAEncryption_data);
      sha256WithRSAEncryption_oid = ObjectIdentifier.newInternal(sha256WithRSAEncryption_data);
      sha384WithRSAEncryption_oid = ObjectIdentifier.newInternal(sha384WithRSAEncryption_data);
      sha512WithRSAEncryption_oid = ObjectIdentifier.newInternal(sha512WithRSAEncryption_data);
      shaWithDSA_OIW_oid = ObjectIdentifier.newInternal(shaWithDSA_OIW_data);
      sha1WithDSA_OIW_oid = ObjectIdentifier.newInternal(sha1WithDSA_OIW_data);
      sha1WithDSA_oid = ObjectIdentifier.newInternal(dsaWithSHA1_PKIX_data);
      nameTable = new HashMap();
      nameTable.put(MD5_oid, "MD5");
      nameTable.put(MD2_oid, "MD2");
      nameTable.put(SHA_oid, "SHA-1");
      nameTable.put(SHA224_oid, "SHA-224");
      nameTable.put(SHA256_oid, "SHA-256");
      nameTable.put(SHA384_oid, "SHA-384");
      nameTable.put(SHA512_oid, "SHA-512");
      nameTable.put(RSAEncryption_oid, "RSA");
      nameTable.put(RSA_oid, "RSA");
      nameTable.put(DH_oid, "Diffie-Hellman");
      nameTable.put(DH_PKIX_oid, "Diffie-Hellman");
      nameTable.put(DSA_oid, "DSA");
      nameTable.put(DSA_OIW_oid, "DSA");
      nameTable.put(EC_oid, "EC");
      nameTable.put(ECDH_oid, "ECDH");
      nameTable.put(AES_oid, "AES");
      nameTable.put(sha1WithECDSA_oid, "SHA1withECDSA");
      nameTable.put(sha224WithECDSA_oid, "SHA224withECDSA");
      nameTable.put(sha256WithECDSA_oid, "SHA256withECDSA");
      nameTable.put(sha384WithECDSA_oid, "SHA384withECDSA");
      nameTable.put(sha512WithECDSA_oid, "SHA512withECDSA");
      nameTable.put(md5WithRSAEncryption_oid, "MD5withRSA");
      nameTable.put(md2WithRSAEncryption_oid, "MD2withRSA");
      nameTable.put(sha1WithDSA_oid, "SHA1withDSA");
      nameTable.put(sha1WithDSA_OIW_oid, "SHA1withDSA");
      nameTable.put(shaWithDSA_OIW_oid, "SHA1withDSA");
      nameTable.put(sha224WithDSA_oid, "SHA224withDSA");
      nameTable.put(sha256WithDSA_oid, "SHA256withDSA");
      nameTable.put(sha1WithRSAEncryption_oid, "SHA1withRSA");
      nameTable.put(sha1WithRSAEncryption_OIW_oid, "SHA1withRSA");
      nameTable.put(sha224WithRSAEncryption_oid, "SHA224withRSA");
      nameTable.put(sha256WithRSAEncryption_oid, "SHA256withRSA");
      nameTable.put(sha384WithRSAEncryption_oid, "SHA384withRSA");
      nameTable.put(sha512WithRSAEncryption_oid, "SHA512withRSA");
      nameTable.put(pbeWithMD5AndDES_oid, "PBEWithMD5AndDES");
      nameTable.put(pbeWithMD5AndRC2_oid, "PBEWithMD5AndRC2");
      nameTable.put(pbeWithSHA1AndDES_oid, "PBEWithSHA1AndDES");
      nameTable.put(pbeWithSHA1AndRC2_oid, "PBEWithSHA1AndRC2");
      nameTable.put(pbeWithSHA1AndDESede_oid, "PBEWithSHA1AndDESede");
      nameTable.put(pbeWithSHA1AndRC2_40_oid, "PBEWithSHA1AndRC2_40");
   }
}
