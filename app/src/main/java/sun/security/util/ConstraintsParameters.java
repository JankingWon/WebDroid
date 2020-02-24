package sun.security.util;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.Timestamp;
import java.security.cert.X509Certificate;
import java.util.Date;

public class ConstraintsParameters {
   private final String algorithm;
   private final AlgorithmParameters algParams;
   private final Key publicKey;
   private final X509Certificate cert;
   private final boolean trustedMatch;
   private final Date pkixDate;
   private final Timestamp jarTimestamp;
   private final String variant;

   public ConstraintsParameters(X509Certificate var1, boolean var2, Date var3, Timestamp var4, String var5) {
      this.cert = var1;
      this.trustedMatch = var2;
      this.pkixDate = var3;
      this.jarTimestamp = var4;
      this.variant = var5 == null ? "generic" : var5;
      this.algorithm = null;
      this.algParams = null;
      this.publicKey = null;
   }

   public ConstraintsParameters(String var1, AlgorithmParameters var2, Key var3, String var4) {
      this.algorithm = var1;
      this.algParams = var2;
      this.publicKey = var3;
      this.cert = null;
      this.trustedMatch = false;
      this.pkixDate = null;
      this.jarTimestamp = null;
      this.variant = var4 == null ? "generic" : var4;
   }

   public ConstraintsParameters(X509Certificate var1) {
      this(var1, false, (Date)null, (Timestamp)null, "generic");
   }

   public ConstraintsParameters(Timestamp var1) {
      this((X509Certificate)null, false, (Date)null, var1, "generic");
   }

   public String getAlgorithm() {
      return this.algorithm;
   }

   public AlgorithmParameters getAlgParams() {
      return this.algParams;
   }

   public Key getPublicKey() {
      return this.publicKey;
   }

   public boolean isTrustedMatch() {
      return this.trustedMatch;
   }

   public X509Certificate getCertificate() {
      return this.cert;
   }

   public Date getPKIXParamDate() {
      return this.pkixDate;
   }

   public Timestamp getJARTimestamp() {
      return this.jarTimestamp;
   }

   public String getVariant() {
      return this.variant;
   }
}
