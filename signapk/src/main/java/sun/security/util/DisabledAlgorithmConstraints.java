package sun.security.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.Key;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisabledAlgorithmConstraints extends AbstractAlgorithmConstraints {
   private static final Debug debug = Debug.getInstance("certpath");
   public static final String PROPERTY_CERTPATH_DISABLED_ALGS = "jdk.certpath.disabledAlgorithms";
   public static final String PROPERTY_TLS_DISABLED_ALGS = "jdk.tls.disabledAlgorithms";
   public static final String PROPERTY_JAR_DISABLED_ALGS = "jdk.jar.disabledAlgorithms";
   private final String[] disabledAlgorithms;
   private final Constraints algorithmConstraints;

   public DisabledAlgorithmConstraints(String var1) {
      this(var1, new AlgorithmDecomposer());
   }

   public DisabledAlgorithmConstraints(String var1, AlgorithmDecomposer var2) {
      super(var2);
      this.disabledAlgorithms = getAlgorithms(var1);
      this.algorithmConstraints = new Constraints(this.disabledAlgorithms);
   }

   public final boolean permits(Set<CryptoPrimitive> var1, String var2, AlgorithmParameters var3) {
      if (!checkAlgorithm(this.disabledAlgorithms, var2, this.decomposer)) {
         return false;
      } else {
         return var3 != null ? this.algorithmConstraints.permits(var2, var3) : true;
      }
   }

   public final boolean permits(Set<CryptoPrimitive> var1, Key var2) {
      return this.checkConstraints(var1, "", var2, (AlgorithmParameters)null);
   }

   public final boolean permits(Set<CryptoPrimitive> var1, String var2, Key var3, AlgorithmParameters var4) {
      if (var2 != null && var2.length() != 0) {
         return this.checkConstraints(var1, var2, var3, var4);
      } else {
         throw new IllegalArgumentException("No algorithm name specified");
      }
   }

   public final void permits(ConstraintsParameters var1) throws CertPathValidatorException {
      this.permits(var1.getAlgorithm(), var1);
   }

   public final void permits(String var1, Key var2, AlgorithmParameters var3, String var4) throws CertPathValidatorException {
      this.permits(var1, new ConstraintsParameters(var1, var3, var2, var4 == null ? "generic" : var4));
   }

   public final void permits(String var1, ConstraintsParameters var2) throws CertPathValidatorException {
      this.algorithmConstraints.permits(var1, var2);
   }

   public boolean checkProperty(String var1) {
      var1 = var1.toLowerCase(Locale.ENGLISH);
      String[] var2 = this.disabledAlgorithms;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String var5 = var2[var4];
         if (var5.toLowerCase(Locale.ENGLISH).indexOf(var1) >= 0) {
            return true;
         }
      }

      return false;
   }

   private boolean checkConstraints(Set<CryptoPrimitive> var1, String var2, Key var3, AlgorithmParameters var4) {
      if (var3 == null) {
         throw new IllegalArgumentException("The key cannot be null");
      } else if (var2 != null && var2.length() != 0 && !this.permits(var1, var2, var4)) {
         return false;
      } else {
         return !this.permits(var1, var3.getAlgorithm(), (AlgorithmParameters)null) ? false : this.algorithmConstraints.permits(var3);
      }
   }

   private abstract static class Constraint {
      String algorithm;
      Constraint nextConstraint;

      private Constraint() {
         this.nextConstraint = null;
      }

      public boolean permits(Key var1) {
         return true;
      }

      public boolean permits(AlgorithmParameters var1) {
         return true;
      }

      public abstract void permits(ConstraintsParameters var1) throws CertPathValidatorException;

      boolean next(ConstraintsParameters var1) throws CertPathValidatorException {
         if (this.nextConstraint != null) {
            this.nextConstraint.permits(var1);
            return true;
         } else {
            return false;
         }
      }

      boolean next(Key var1) {
         return this.nextConstraint != null && this.nextConstraint.permits(var1);
      }

      String extendedMsg(ConstraintsParameters var1) {
         return var1.getCertificate() == null ? "." : " used with certificate: " + var1.getCertificate().getSubjectX500Principal() + (var1.getVariant() != "generic" ? ".  Usage was " + var1.getVariant() : ".");
      }

      // $FF: synthetic method
      Constraint(Object var1) {
         this();
      }

      static enum Operator {
         EQ,
         NE,
         LT,
         LE,
         GT,
         GE;

         static Operator of(String var0) {
            byte var2 = -1;
            switch(var0.hashCode()) {
            case 60:
               if (var0.equals("<")) {
                  var2 = 2;
               }
               break;
            case 62:
               if (var0.equals(">")) {
                  var2 = 4;
               }
               break;
            case 1084:
               if (var0.equals("!=")) {
                  var2 = 1;
               }
               break;
            case 1921:
               if (var0.equals("<=")) {
                  var2 = 3;
               }
               break;
            case 1952:
               if (var0.equals("==")) {
                  var2 = 0;
               }
               break;
            case 1983:
               if (var0.equals(">=")) {
                  var2 = 5;
               }
            }

            switch(var2) {
            case 0:
               return EQ;
            case 1:
               return NE;
            case 2:
               return LT;
            case 3:
               return LE;
            case 4:
               return GT;
            case 5:
               return GE;
            default:
               throw new IllegalArgumentException("Error in security property. " + var0 + " is not a legal Operator");
            }
         }
      }
   }

   private static class Constraints {
      private Map<String, List<Constraint>> constraintsMap = new HashMap();

      public Constraints(String[] var1) {
         String[] var2 = var1;
         int var3 = var1.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String var5 = var2[var4];
            if (var5 != null && !var5.isEmpty()) {
               var5 = var5.trim();
               if (DisabledAlgorithmConstraints.debug != null) {
                  DisabledAlgorithmConstraints.debug.println("Constraints: " + var5);
               }

               int var6 = var5.indexOf(32);
               String var7 = AlgorithmDecomposer.hashName((var6 > 0 ? var5.substring(0, var6) : var5).toUpperCase(Locale.ENGLISH));
               List var8 = (List)this.constraintsMap.getOrDefault(var7, new ArrayList(1));
               Iterator var9 = AlgorithmDecomposer.getAliases(var7).iterator();

               while(var9.hasNext()) {
                  String var10 = (String)var9.next();
                  this.constraintsMap.putIfAbsent(var10, var8);
               }

               if (var6 <= 0) {
                  var8.add(new DisabledConstraint(var7));
               } else {
                  String var22 = var5.substring(var6 + 1);
                  Object var11 = null;
                  boolean var12 = false;
                  boolean var13 = false;
                  String[] var14 = var22.split("&");
                  int var15 = var14.length;

                  for(int var16 = 0; var16 < var15; ++var16) {
                     String var17 = var14[var16];
                     var17 = var17.trim();
                     Object var23;
                     if (var17.startsWith("keySize")) {
                        if (DisabledAlgorithmConstraints.debug != null) {
                           DisabledAlgorithmConstraints.debug.println("Constraints set to keySize: " + var17);
                        }

                        StringTokenizer var25 = new StringTokenizer(var17);
                        if (!"keySize".equals(var25.nextToken())) {
                           throw new IllegalArgumentException("Error in security property. Constraint unknown: " + var17);
                        }

                        var23 = new KeySizeConstraint(var7, Constraint.Operator.of(var25.nextToken()), Integer.parseInt(var25.nextToken()));
                     } else if (var17.equalsIgnoreCase("jdkCA")) {
                        if (DisabledAlgorithmConstraints.debug != null) {
                           DisabledAlgorithmConstraints.debug.println("Constraints set to jdkCA.");
                        }

                        if (var12) {
                           throw new IllegalArgumentException("Only one jdkCA entry allowed in property. Constraint: " + var5);
                        }

                        var23 = new jdkCAConstraint(var7);
                        var12 = true;
                     } else {
                        Matcher var18;
                        if (var17.startsWith("denyAfter") && (var18 = Holder.DENY_AFTER_PATTERN.matcher(var17)).matches()) {
                           if (DisabledAlgorithmConstraints.debug != null) {
                              DisabledAlgorithmConstraints.debug.println("Constraints set to denyAfter");
                           }

                           if (var13) {
                              throw new IllegalArgumentException("Only one denyAfter entry allowed in property. Constraint: " + var5);
                           }

                           int var24 = Integer.parseInt(var18.group(1));
                           int var20 = Integer.parseInt(var18.group(2));
                           int var21 = Integer.parseInt(var18.group(3));
                           var23 = new DenyAfterConstraint(var7, var24, var20, var21);
                           var13 = true;
                        } else {
                           if (!var17.startsWith("usage")) {
                              throw new IllegalArgumentException("Error in security property. Constraint unknown: " + var17);
                           }

                           String[] var19 = var17.substring(5).trim().split(" ");
                           var23 = new UsageConstraint(var7, var19);
                           if (DisabledAlgorithmConstraints.debug != null) {
                              DisabledAlgorithmConstraints.debug.println("Constraints usage length is " + var19.length);
                           }
                        }
                     }

                     if (var11 == null) {
                        var8.add(var23);
                     } else {
                        ((Constraint)var11).nextConstraint = (Constraint)var23;
                     }

                     var11 = var23;
                  }
               }
            }
         }

      }

      private List<Constraint> getConstraints(String var1) {
         return (List)this.constraintsMap.get(var1);
      }

      public boolean permits(Key var1) {
         List var2 = this.getConstraints(var1.getAlgorithm());
         if (var2 == null) {
            return true;
         } else {
            Iterator var3 = var2.iterator();

            Constraint var4;
            do {
               if (!var3.hasNext()) {
                  return true;
               }

               var4 = (Constraint)var3.next();
            } while(var4.permits(var1));

            if (DisabledAlgorithmConstraints.debug != null) {
               DisabledAlgorithmConstraints.debug.println("keySizeConstraint: failed key constraint check " + KeyUtil.getKeySize(var1));
            }

            return false;
         }
      }

      public boolean permits(String var1, AlgorithmParameters var2) {
         List var3 = this.getConstraints(var1);
         if (var3 == null) {
            return true;
         } else {
            Iterator var4 = var3.iterator();

            Constraint var5;
            do {
               if (!var4.hasNext()) {
                  return true;
               }

               var5 = (Constraint)var4.next();
            } while(var5.permits(var2));

            if (DisabledAlgorithmConstraints.debug != null) {
               DisabledAlgorithmConstraints.debug.println("keySizeConstraint: failed algorithm parameters constraint check " + var2);
            }

            return false;
         }
      }

      public void permits(String var1, ConstraintsParameters var2) throws CertPathValidatorException {
         X509Certificate var3 = var2.getCertificate();
         if (DisabledAlgorithmConstraints.debug != null) {
            DisabledAlgorithmConstraints.debug.println("Constraints.permits(): " + var1 + " Variant: " + var2.getVariant());
         }

         HashSet var4 = new HashSet();
         if (var1 != null) {
            var4.addAll(AlgorithmDecomposer.decomposeOneHash(var1));
         }

         if (var3 != null) {
            var4.add(var3.getPublicKey().getAlgorithm());
         }

         if (var2.getPublicKey() != null) {
            var4.add(var2.getPublicKey().getAlgorithm());
         }

         Iterator var5 = var4.iterator();

         while(true) {
            List var7;
            do {
               if (!var5.hasNext()) {
                  return;
               }

               String var6 = (String)var5.next();
               var7 = this.getConstraints(var6);
            } while(var7 == null);

            Iterator var8 = var7.iterator();

            while(var8.hasNext()) {
               Constraint var9 = (Constraint)var8.next();
               var9.permits(var2);
            }
         }
      }

      private static class Holder {
         private static final Pattern DENY_AFTER_PATTERN = Pattern.compile("denyAfter\\s+(\\d{4})-(\\d{2})-(\\d{2})");
      }
   }

   private static class DenyAfterConstraint extends Constraint {
      private Date denyAfterDate;
      private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d HH:mm:ss z yyyy");

      DenyAfterConstraint(String var1, int var2, int var3, int var4) {
         super(null);
         this.algorithm = var1;
         if (DisabledAlgorithmConstraints.debug != null) {
            DisabledAlgorithmConstraints.debug.println("DenyAfterConstraint read in as:  year " + var2 + ", month = " + var3 + ", day = " + var4);
         }

         Calendar var5 = (new Calendar.Builder()).setTimeZone(TimeZone.getTimeZone("GMT")).setDate(var2, var3 - 1, var4).build();
         if (var2 <= var5.getActualMaximum(1) && var2 >= var5.getActualMinimum(1)) {
            if (var3 - 1 <= var5.getActualMaximum(2) && var3 - 1 >= var5.getActualMinimum(2)) {
               if (var4 <= var5.getActualMaximum(5) && var4 >= var5.getActualMinimum(5)) {
                  this.denyAfterDate = var5.getTime();
                  if (DisabledAlgorithmConstraints.debug != null) {
                     DisabledAlgorithmConstraints.debug.println("DenyAfterConstraint date set to: " + dateFormat.format(this.denyAfterDate));
                  }

               } else {
                  throw new IllegalArgumentException("Invalid Day of Month given in constraint: " + var4);
               }
            } else {
               throw new IllegalArgumentException("Invalid month given in constraint: " + var3);
            }
         } else {
            throw new IllegalArgumentException("Invalid year given in constraint: " + var2);
         }
      }

      public void permits(ConstraintsParameters var1) throws CertPathValidatorException {
         Date var2;
         String var3;
         if (var1.getJARTimestamp() != null) {
            var2 = var1.getJARTimestamp().getTimestamp();
            var3 = "JAR Timestamp date: ";
         } else if (var1.getPKIXParamDate() != null) {
            var2 = var1.getPKIXParamDate();
            var3 = "PKIXParameter date: ";
         } else {
            var2 = new Date();
            var3 = "Current date: ";
         }

         if (!this.denyAfterDate.after(var2)) {
            if (!this.next(var1)) {
               throw new CertPathValidatorException("denyAfter constraint check failed: " + this.algorithm + " used with Constraint date: " + dateFormat.format(this.denyAfterDate) + "; " + var3 + dateFormat.format(var2) + this.extendedMsg(var1), (Throwable)null, (CertPath)null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            }
         }
      }

      public boolean permits(Key var1) {
         if (this.next(var1)) {
            return true;
         } else {
            if (DisabledAlgorithmConstraints.debug != null) {
               DisabledAlgorithmConstraints.debug.println("DenyAfterConstraints.permits(): " + this.algorithm);
            }

            return this.denyAfterDate.after(new Date());
         }
      }
   }

   private static class DisabledConstraint extends Constraint {
      DisabledConstraint(String var1) {
         super(null);
         this.algorithm = var1;
      }

      public void permits(ConstraintsParameters var1) throws CertPathValidatorException {
         throw new CertPathValidatorException("Algorithm constraints check failed on disabled algorithm: " + this.algorithm + this.extendedMsg(var1), (Throwable)null, (CertPath)null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
      }

      public boolean permits(Key var1) {
         return false;
      }
   }

   private static class KeySizeConstraint extends Constraint {
      private int minSize;
      private int maxSize;
      private int prohibitedSize = -1;
      private int size;

      public KeySizeConstraint(String var1, Operator var2, int var3) {
         super(null);
         this.algorithm = var1;
         switch(var2) {
         case EQ:
            this.minSize = 0;
            this.maxSize = Integer.MAX_VALUE;
            this.prohibitedSize = var3;
            break;
         case NE:
            this.minSize = var3;
            this.maxSize = var3;
            break;
         case LT:
            this.minSize = var3;
            this.maxSize = Integer.MAX_VALUE;
            break;
         case LE:
            this.minSize = var3 + 1;
            this.maxSize = Integer.MAX_VALUE;
            break;
         case GT:
            this.minSize = 0;
            this.maxSize = var3;
            break;
         case GE:
            this.minSize = 0;
            this.maxSize = var3 > 1 ? var3 - 1 : 0;
            break;
         default:
            this.minSize = Integer.MAX_VALUE;
            this.maxSize = -1;
         }

      }

      public void permits(ConstraintsParameters var1) throws CertPathValidatorException {
         Object var2 = null;
         if (var1.getPublicKey() != null) {
            var2 = var1.getPublicKey();
         } else if (var1.getCertificate() != null) {
            var2 = var1.getCertificate().getPublicKey();
         }

         if (var2 != null && !this.permitsImpl((Key)var2)) {
            if (this.nextConstraint != null) {
               this.nextConstraint.permits(var1);
            } else {
               throw new CertPathValidatorException("Algorithm constraints check failed on keysize limits. " + this.algorithm + " " + KeyUtil.getKeySize((Key)var2) + "bit key" + this.extendedMsg(var1), (Throwable)null, (CertPath)null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            }
         }
      }

      public boolean permits(Key var1) {
         if (this.nextConstraint != null && this.nextConstraint.permits(var1)) {
            return true;
         } else {
            if (DisabledAlgorithmConstraints.debug != null) {
               DisabledAlgorithmConstraints.debug.println("KeySizeConstraints.permits(): " + this.algorithm);
            }

            return this.permitsImpl(var1);
         }
      }

      public boolean permits(AlgorithmParameters var1) {
         String var2 = var1.getAlgorithm();
         if (!this.algorithm.equalsIgnoreCase(var1.getAlgorithm())) {
            Collection var3 = AlgorithmDecomposer.getAliases(this.algorithm);
            if (!var3.contains(var2)) {
               return true;
            }
         }

         int var4 = KeyUtil.getKeySize(var1);
         if (var4 == 0) {
            return false;
         } else if (var4 <= 0) {
            return true;
         } else {
            return var4 >= this.minSize && var4 <= this.maxSize && this.prohibitedSize != var4;
         }
      }

      private boolean permitsImpl(Key var1) {
         if (this.algorithm.compareToIgnoreCase(var1.getAlgorithm()) != 0) {
            return true;
         } else {
            this.size = KeyUtil.getKeySize(var1);
            if (this.size == 0) {
               return false;
            } else if (this.size <= 0) {
               return true;
            } else {
               return this.size >= this.minSize && this.size <= this.maxSize && this.prohibitedSize != this.size;
            }
         }
      }
   }

   private static class UsageConstraint extends Constraint {
      String[] usages;

      UsageConstraint(String var1, String[] var2) {
         super(null);
         this.algorithm = var1;
         this.usages = var2;
      }

      public void permits(ConstraintsParameters var1) throws CertPathValidatorException {
         String[] var2 = this.usages;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String var5 = var2[var4];
            String var6 = null;
            if (var5.compareToIgnoreCase("TLSServer") == 0) {
               var6 = "tls server";
            } else if (var5.compareToIgnoreCase("TLSClient") == 0) {
               var6 = "tls client";
            } else if (var5.compareToIgnoreCase("SignedJAR") == 0) {
               var6 = "plugin code signing";
            }

            if (DisabledAlgorithmConstraints.debug != null) {
               DisabledAlgorithmConstraints.debug.println("Checking if usage constraint \"" + var6 + "\" matches \"" + var1.getVariant() + "\"");
               ByteArrayOutputStream var7 = new ByteArrayOutputStream();
               PrintStream var8 = new PrintStream(var7);
               (new Exception()).printStackTrace(var8);
               DisabledAlgorithmConstraints.debug.println(var7.toString());
            }

            if (var1.getVariant().compareTo(var6) == 0) {
               if (this.next(var1)) {
                  return;
               }

               throw new CertPathValidatorException("Usage constraint " + var5 + " check failed: " + this.algorithm + this.extendedMsg(var1), (Throwable)null, (CertPath)null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            }
         }

      }
   }

   private static class jdkCAConstraint extends Constraint {
      jdkCAConstraint(String var1) {
         super(null);
         this.algorithm = var1;
      }

      public void permits(ConstraintsParameters var1) throws CertPathValidatorException {
         if (DisabledAlgorithmConstraints.debug != null) {
            DisabledAlgorithmConstraints.debug.println("jdkCAConstraints.permits(): " + this.algorithm);
         }

         if (var1.isTrustedMatch()) {
            if (!this.next(var1)) {
               throw new CertPathValidatorException("Algorithm constraints check failed on certificate anchor limits. " + this.algorithm + this.extendedMsg(var1), (Throwable)null, (CertPath)null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            }
         }
      }
   }
}
