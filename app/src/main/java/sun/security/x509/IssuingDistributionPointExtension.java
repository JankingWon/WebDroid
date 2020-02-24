package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class IssuingDistributionPointExtension extends Extension implements CertAttrSet<String> {
   public static final String IDENT = "x509.info.extensions.IssuingDistributionPoint";
   public static final String NAME = "IssuingDistributionPoint";
   public static final String POINT = "point";
   public static final String REASONS = "reasons";
   public static final String ONLY_USER_CERTS = "only_user_certs";
   public static final String ONLY_CA_CERTS = "only_ca_certs";
   public static final String ONLY_ATTRIBUTE_CERTS = "only_attribute_certs";
   public static final String INDIRECT_CRL = "indirect_crl";
   private DistributionPointName distributionPoint = null;
   private ReasonFlags revocationReasons = null;
   private boolean hasOnlyUserCerts = false;
   private boolean hasOnlyCACerts = false;
   private boolean hasOnlyAttributeCerts = false;
   private boolean isIndirectCRL = false;
   private static final byte TAG_DISTRIBUTION_POINT = 0;
   private static final byte TAG_ONLY_USER_CERTS = 1;
   private static final byte TAG_ONLY_CA_CERTS = 2;
   private static final byte TAG_ONLY_SOME_REASONS = 3;
   private static final byte TAG_INDIRECT_CRL = 4;
   private static final byte TAG_ONLY_ATTRIBUTE_CERTS = 5;

   public IssuingDistributionPointExtension(DistributionPointName var1, ReasonFlags var2, boolean var3, boolean var4, boolean var5, boolean var6) throws IOException {
      if ((!var3 || !var4 && !var5) && (!var4 || !var3 && !var5) && (!var5 || !var3 && !var4)) {
         this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
         this.critical = true;
         this.distributionPoint = var1;
         this.revocationReasons = var2;
         this.hasOnlyUserCerts = var3;
         this.hasOnlyCACerts = var4;
         this.hasOnlyAttributeCerts = var5;
         this.isIndirectCRL = var6;
         this.encodeThis();
      } else {
         throw new IllegalArgumentException("Only one of hasOnlyUserCerts, hasOnlyCACerts, hasOnlyAttributeCerts may be set to true");
      }
   }

   public IssuingDistributionPointExtension(Boolean var1, Object var2) throws IOException {
      this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
      this.critical = var1;
      if (!(var2 instanceof byte[])) {
         throw new IOException("Illegal argument type");
      } else {
         this.extensionValue = (byte[])((byte[])var2);
         DerValue var3 = new DerValue(this.extensionValue);
         if (var3.tag != 48) {
            throw new IOException("Invalid encoding for IssuingDistributionPointExtension.");
         } else if (var3.data != null && var3.data.available() != 0) {
            DerInputStream var4 = var3.data;

            while(true) {
               if (var4 != null && var4.available() != 0) {
                  DerValue var5 = var4.getDerValue();
                  if (var5.isContextSpecific((byte)0) && var5.isConstructed()) {
                     this.distributionPoint = new DistributionPointName(var5.data.getDerValue());
                     continue;
                  }

                  if (var5.isContextSpecific((byte)1) && !var5.isConstructed()) {
                     var5.resetTag((byte)1);
                     this.hasOnlyUserCerts = var5.getBoolean();
                     continue;
                  }

                  if (var5.isContextSpecific((byte)2) && !var5.isConstructed()) {
                     var5.resetTag((byte)1);
                     this.hasOnlyCACerts = var5.getBoolean();
                     continue;
                  }

                  if (var5.isContextSpecific((byte)3) && !var5.isConstructed()) {
                     this.revocationReasons = new ReasonFlags(var5);
                     continue;
                  }

                  if (var5.isContextSpecific((byte)4) && !var5.isConstructed()) {
                     var5.resetTag((byte)1);
                     this.isIndirectCRL = var5.getBoolean();
                     continue;
                  }

                  if (var5.isContextSpecific((byte)5) && !var5.isConstructed()) {
                     var5.resetTag((byte)1);
                     this.hasOnlyAttributeCerts = var5.getBoolean();
                     continue;
                  }

                  throw new IOException("Invalid encoding of IssuingDistributionPoint");
               }

               return;
            }
         }
      }
   }

   public String getName() {
      return "IssuingDistributionPoint";
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
         this.critical = false;
         this.encodeThis();
      }

      super.encode(var2);
      var1.write(var2.toByteArray());
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("point")) {
         if (!(var2 instanceof DistributionPointName)) {
            throw new IOException("Attribute value should be of type DistributionPointName.");
         }

         this.distributionPoint = (DistributionPointName)var2;
      } else if (var1.equalsIgnoreCase("reasons")) {
         if (!(var2 instanceof ReasonFlags)) {
            throw new IOException("Attribute value should be of type ReasonFlags.");
         }

         this.revocationReasons = (ReasonFlags)var2;
      } else if (var1.equalsIgnoreCase("indirect_crl")) {
         if (!(var2 instanceof Boolean)) {
            throw new IOException("Attribute value should be of type Boolean.");
         }

         this.isIndirectCRL = (Boolean)var2;
      } else if (var1.equalsIgnoreCase("only_user_certs")) {
         if (!(var2 instanceof Boolean)) {
            throw new IOException("Attribute value should be of type Boolean.");
         }

         this.hasOnlyUserCerts = (Boolean)var2;
      } else if (var1.equalsIgnoreCase("only_ca_certs")) {
         if (!(var2 instanceof Boolean)) {
            throw new IOException("Attribute value should be of type Boolean.");
         }

         this.hasOnlyCACerts = (Boolean)var2;
      } else {
         if (!var1.equalsIgnoreCase("only_attribute_certs")) {
            throw new IOException("Attribute name [" + var1 + "] not recognized by CertAttrSet:IssuingDistributionPointExtension.");
         }

         if (!(var2 instanceof Boolean)) {
            throw new IOException("Attribute value should be of type Boolean.");
         }

         this.hasOnlyAttributeCerts = (Boolean)var2;
      }

      this.encodeThis();
   }

   public Object get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("point")) {
         return this.distributionPoint;
      } else if (var1.equalsIgnoreCase("indirect_crl")) {
         return this.isIndirectCRL;
      } else if (var1.equalsIgnoreCase("reasons")) {
         return this.revocationReasons;
      } else if (var1.equalsIgnoreCase("only_user_certs")) {
         return this.hasOnlyUserCerts;
      } else if (var1.equalsIgnoreCase("only_ca_certs")) {
         return this.hasOnlyCACerts;
      } else if (var1.equalsIgnoreCase("only_attribute_certs")) {
         return this.hasOnlyAttributeCerts;
      } else {
         throw new IOException("Attribute name [" + var1 + "] not recognized by CertAttrSet:IssuingDistributionPointExtension.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("point")) {
         this.distributionPoint = null;
      } else if (var1.equalsIgnoreCase("indirect_crl")) {
         this.isIndirectCRL = false;
      } else if (var1.equalsIgnoreCase("reasons")) {
         this.revocationReasons = null;
      } else if (var1.equalsIgnoreCase("only_user_certs")) {
         this.hasOnlyUserCerts = false;
      } else if (var1.equalsIgnoreCase("only_ca_certs")) {
         this.hasOnlyCACerts = false;
      } else {
         if (!var1.equalsIgnoreCase("only_attribute_certs")) {
            throw new IOException("Attribute name [" + var1 + "] not recognized by CertAttrSet:IssuingDistributionPointExtension.");
         }

         this.hasOnlyAttributeCerts = false;
      }

      this.encodeThis();
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("point");
      var1.addElement("reasons");
      var1.addElement("only_user_certs");
      var1.addElement("only_ca_certs");
      var1.addElement("only_attribute_certs");
      var1.addElement("indirect_crl");
      return var1.elements();
   }

   private void encodeThis() throws IOException {
      if (this.distributionPoint == null && this.revocationReasons == null && !this.hasOnlyUserCerts && !this.hasOnlyCACerts && !this.hasOnlyAttributeCerts && !this.isIndirectCRL) {
         this.extensionValue = null;
      } else {
         DerOutputStream var1 = new DerOutputStream();
         DerOutputStream var2;
         if (this.distributionPoint != null) {
            var2 = new DerOutputStream();
            this.distributionPoint.encode(var2);
            var1.writeImplicit(DerValue.createTag((byte)-128, true, (byte)0), var2);
         }

         if (this.hasOnlyUserCerts) {
            var2 = new DerOutputStream();
            var2.putBoolean(this.hasOnlyUserCerts);
            var1.writeImplicit(DerValue.createTag((byte)-128, false, (byte)1), var2);
         }

         if (this.hasOnlyCACerts) {
            var2 = new DerOutputStream();
            var2.putBoolean(this.hasOnlyCACerts);
            var1.writeImplicit(DerValue.createTag((byte)-128, false, (byte)2), var2);
         }

         if (this.revocationReasons != null) {
            var2 = new DerOutputStream();
            this.revocationReasons.encode(var2);
            var1.writeImplicit(DerValue.createTag((byte)-128, false, (byte)3), var2);
         }

         if (this.isIndirectCRL) {
            var2 = new DerOutputStream();
            var2.putBoolean(this.isIndirectCRL);
            var1.writeImplicit(DerValue.createTag((byte)-128, false, (byte)4), var2);
         }

         if (this.hasOnlyAttributeCerts) {
            var2 = new DerOutputStream();
            var2.putBoolean(this.hasOnlyAttributeCerts);
            var1.writeImplicit(DerValue.createTag((byte)-128, false, (byte)5), var2);
         }

         var2 = new DerOutputStream();
         var2.write((byte)48, (DerOutputStream)var1);
         this.extensionValue = var2.toByteArray();
      }
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder(super.toString());
      var1.append("IssuingDistributionPoint [\n  ");
      if (this.distributionPoint != null) {
         var1.append((Object)this.distributionPoint);
      }

      if (this.revocationReasons != null) {
         var1.append((Object)this.revocationReasons);
      }

      var1.append(this.hasOnlyUserCerts ? "  Only contains user certs: true" : "  Only contains user certs: false").append("\n");
      var1.append(this.hasOnlyCACerts ? "  Only contains CA certs: true" : "  Only contains CA certs: false").append("\n");
      var1.append(this.hasOnlyAttributeCerts ? "  Only contains attribute certs: true" : "  Only contains attribute certs: false").append("\n");
      var1.append(this.isIndirectCRL ? "  Indirect CRL: true" : "  Indirect CRL: false").append("\n");
      var1.append("]\n");
      return var1.toString();
   }
}
