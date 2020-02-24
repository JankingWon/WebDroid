package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;

import sun.net.util.IPAddressUtil;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class NameConstraintsExtension extends Extension implements CertAttrSet<String>, Cloneable {
   public static final String IDENT = "x509.info.extensions.NameConstraints";
   public static final String NAME = "NameConstraints";
   public static final String PERMITTED_SUBTREES = "permitted_subtrees";
   public static final String EXCLUDED_SUBTREES = "excluded_subtrees";
   private static final byte TAG_PERMITTED = 0;
   private static final byte TAG_EXCLUDED = 1;
   private GeneralSubtrees permitted = null;
   private GeneralSubtrees excluded = null;
   private boolean hasMin;
   private boolean hasMax;
   private boolean minMaxValid = false;

   private void calcMinMax() throws IOException {
      this.hasMin = false;
      this.hasMax = false;
      int var1;
      GeneralSubtree var2;
      if (this.excluded != null) {
         for(var1 = 0; var1 < this.excluded.size(); ++var1) {
            var2 = this.excluded.get(var1);
            if (var2.getMinimum() != 0) {
               this.hasMin = true;
            }

            if (var2.getMaximum() != -1) {
               this.hasMax = true;
            }
         }
      }

      if (this.permitted != null) {
         for(var1 = 0; var1 < this.permitted.size(); ++var1) {
            var2 = this.permitted.get(var1);
            if (var2.getMinimum() != 0) {
               this.hasMin = true;
            }

            if (var2.getMaximum() != -1) {
               this.hasMax = true;
            }
         }
      }

      this.minMaxValid = true;
   }

   private void encodeThis() throws IOException {
      this.minMaxValid = false;
      if (this.permitted == null && this.excluded == null) {
         this.extensionValue = null;
      } else {
         DerOutputStream var1 = new DerOutputStream();
         DerOutputStream var2 = new DerOutputStream();
         DerOutputStream var3;
         if (this.permitted != null) {
            var3 = new DerOutputStream();
            this.permitted.encode(var3);
            var2.writeImplicit(DerValue.createTag((byte)-128, true, (byte)0), var3);
         }

         if (this.excluded != null) {
            var3 = new DerOutputStream();
            this.excluded.encode(var3);
            var2.writeImplicit(DerValue.createTag((byte)-128, true, (byte)1), var3);
         }

         var1.write((byte)48, (DerOutputStream)var2);
         this.extensionValue = var1.toByteArray();
      }
   }

   public NameConstraintsExtension(GeneralSubtrees var1, GeneralSubtrees var2) throws IOException {
      this.permitted = var1;
      this.excluded = var2;
      this.extensionId = PKIXExtensions.NameConstraints_Id;
      this.critical = true;
      this.encodeThis();
   }

   public NameConstraintsExtension(Boolean var1, Object var2) throws IOException {
      this.extensionId = PKIXExtensions.NameConstraints_Id;
      this.critical = var1;
      this.extensionValue = (byte[])((byte[])var2);
      DerValue var3 = new DerValue(this.extensionValue);
      if (var3.tag != 48) {
         throw new IOException("Invalid encoding for NameConstraintsExtension.");
      } else if (var3.data != null) {
         while(true) {
            while(var3.data.available() != 0) {
               DerValue var4 = var3.data.getDerValue();
               if (!var4.isContextSpecific((byte)0) || !var4.isConstructed()) {
                  if (!var4.isContextSpecific((byte)1) || !var4.isConstructed()) {
                     throw new IOException("Invalid encoding of NameConstraintsExtension.");
                  }

                  if (this.excluded != null) {
                     throw new IOException("Duplicate excluded GeneralSubtrees in NameConstraintsExtension.");
                  }

                  var4.resetTag((byte)48);
                  this.excluded = new GeneralSubtrees(var4);
               } else {
                  if (this.permitted != null) {
                     throw new IOException("Duplicate permitted GeneralSubtrees in NameConstraintsExtension.");
                  }

                  var4.resetTag((byte)48);
                  this.permitted = new GeneralSubtrees(var4);
               }
            }

            this.minMaxValid = false;
            return;
         }
      }
   }

   public String toString() {
      return super.toString() + "NameConstraints: [" + (this.permitted == null ? "" : "\n    Permitted:" + this.permitted.toString()) + (this.excluded == null ? "" : "\n    Excluded:" + this.excluded.toString()) + "   ]\n";
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = PKIXExtensions.NameConstraints_Id;
         this.critical = true;
         this.encodeThis();
      }

      super.encode(var2);
      var1.write(var2.toByteArray());
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("permitted_subtrees")) {
         if (!(var2 instanceof GeneralSubtrees)) {
            throw new IOException("Attribute value should be of type GeneralSubtrees.");
         }

         this.permitted = (GeneralSubtrees)var2;
      } else {
         if (!var1.equalsIgnoreCase("excluded_subtrees")) {
            throw new IOException("Attribute name not recognized by CertAttrSet:NameConstraintsExtension.");
         }

         if (!(var2 instanceof GeneralSubtrees)) {
            throw new IOException("Attribute value should be of type GeneralSubtrees.");
         }

         this.excluded = (GeneralSubtrees)var2;
      }

      this.encodeThis();
   }

   public GeneralSubtrees get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("permitted_subtrees")) {
         return this.permitted;
      } else if (var1.equalsIgnoreCase("excluded_subtrees")) {
         return this.excluded;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:NameConstraintsExtension.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("permitted_subtrees")) {
         this.permitted = null;
      } else {
         if (!var1.equalsIgnoreCase("excluded_subtrees")) {
            throw new IOException("Attribute name not recognized by CertAttrSet:NameConstraintsExtension.");
         }

         this.excluded = null;
      }

      this.encodeThis();
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("permitted_subtrees");
      var1.addElement("excluded_subtrees");
      return var1.elements();
   }

   public String getName() {
      return "NameConstraints";
   }

   public void merge(NameConstraintsExtension var1) throws IOException {
      if (var1 != null) {
         GeneralSubtrees var2 = var1.get("excluded_subtrees");
         if (this.excluded == null) {
            this.excluded = var2 != null ? (GeneralSubtrees)var2.clone() : null;
         } else if (var2 != null) {
            this.excluded.union(var2);
         }

         GeneralSubtrees var3 = var1.get("permitted_subtrees");
         if (this.permitted == null) {
            this.permitted = var3 != null ? (GeneralSubtrees)var3.clone() : null;
         } else if (var3 != null) {
            var2 = this.permitted.intersect(var3);
            if (var2 != null) {
               if (this.excluded != null) {
                  this.excluded.union(var2);
               } else {
                  this.excluded = (GeneralSubtrees)var2.clone();
               }
            }
         }

         if (this.permitted != null) {
            this.permitted.reduce(this.excluded);
         }

         this.encodeThis();
      }
   }

   public boolean verify(X509Certificate var1) throws IOException {
      if (var1 == null) {
         throw new IOException("Certificate is null");
      } else {
         if (!this.minMaxValid) {
            this.calcMinMax();
         }

         if (this.hasMin) {
            throw new IOException("Non-zero minimum BaseDistance in name constraints not supported");
         } else if (this.hasMax) {
            throw new IOException("Maximum BaseDistance in name constraints not supported");
         } else {
            X500Principal var2 = var1.getSubjectX500Principal();
            X500Name var3 = X500Name.asX500Name(var2);
            if (!var3.isEmpty() && !this.verify((GeneralNameInterface)var3)) {
               return false;
            } else {
               GeneralNames var4 = null;

               try {
                  X509CertImpl var5 = X509CertImpl.toImpl(var1);
                  SubjectAlternativeNameExtension var6 = var5.getSubjectAlternativeNameExtension();
                  if (var6 != null) {
                     var4 = var6.get("subject_name");
                  }
               } catch (CertificateException var11) {
                  throw new IOException("Unable to extract extensions from certificate: " + var11.getMessage());
               }

               if (var4 == null) {
                  var4 = new GeneralNames();
                  Iterator var13 = var3.allAvas().iterator();

                  while(var13.hasNext()) {
                     AVA var15 = (AVA)var13.next();
                     ObjectIdentifier var7 = var15.getObjectIdentifier();
                     if (var7.equals(PKCS9Attribute.EMAIL_ADDRESS_OID)) {
                        String var8 = var15.getValueString();
                        if (var8 != null) {
                           try {
                              var4.add(new GeneralName(new RFC822Name(var8)));
                           } catch (IOException var10) {
                           }
                        }
                     }
                  }
               }

               DerValue var14 = var3.findMostSpecificAttribute(X500Name.commonName_oid);
               String var16 = var14 == null ? null : var14.getAsString();
               if (var16 != null) {
                  try {
                     if (!IPAddressUtil.isIPv4LiteralAddress(var16) && !IPAddressUtil.isIPv6LiteralAddress(var16)) {
                        if (!hasNameType(var4, 2)) {
                           var4.add(new GeneralName(new DNSName(var16)));
                        }
                     } else if (!hasNameType(var4, 7)) {
                        var4.add(new GeneralName(new IPAddressName(var16)));
                     }
                  } catch (IOException var12) {
                  }
               }

               for(int var17 = 0; var17 < var4.size(); ++var17) {
                  GeneralNameInterface var18 = var4.get(var17).getName();
                  if (!this.verify(var18)) {
                     return false;
                  }
               }

               return true;
            }
         }
      }
   }

   private static boolean hasNameType(GeneralNames var0, int var1) {
      Iterator var2 = var0.names().iterator();

      GeneralName var3;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         var3 = (GeneralName)var2.next();
      } while(var3.getType() != var1);

      return true;
   }

   public boolean verify(GeneralNameInterface var1) throws IOException {
      if (var1 == null) {
         throw new IOException("name is null");
      } else {
         if (this.excluded != null && this.excluded.size() > 0) {
            for(int var2 = 0; var2 < this.excluded.size(); ++var2) {
               GeneralSubtree var3 = this.excluded.get(var2);
               if (var3 != null) {
                  GeneralName var4 = var3.getName();
                  if (var4 != null) {
                     GeneralNameInterface var5 = var4.getName();
                     if (var5 != null) {
                        switch(var5.constrains(var1)) {
                        case -1:
                        case 2:
                        case 3:
                        default:
                           break;
                        case 0:
                        case 1:
                           return false;
                        }
                     }
                  }
               }
            }
         }

         if (this.permitted != null && this.permitted.size() > 0) {
            boolean var7 = false;

            for(int var8 = 0; var8 < this.permitted.size(); ++var8) {
               GeneralSubtree var9 = this.permitted.get(var8);
               if (var9 != null) {
                  GeneralName var10 = var9.getName();
                  if (var10 != null) {
                     GeneralNameInterface var6 = var10.getName();
                     if (var6 != null) {
                        switch(var6.constrains(var1)) {
                        case -1:
                        default:
                           break;
                        case 0:
                        case 1:
                           return true;
                        case 2:
                        case 3:
                           var7 = true;
                        }
                     }
                  }
               }
            }

            if (var7) {
               return false;
            }
         }

         return true;
      }
   }

   public Object clone() {
      try {
         NameConstraintsExtension var1 = (NameConstraintsExtension)super.clone();
         if (this.permitted != null) {
            var1.permitted = (GeneralSubtrees)this.permitted.clone();
         }

         if (this.excluded != null) {
            var1.excluded = (GeneralSubtrees)this.excluded.clone();
         }

         return var1;
      } catch (CloneNotSupportedException var2) {
         throw new RuntimeException("CloneNotSupportedException while cloning NameConstraintsException. This should never happen.");
      }
   }
}
