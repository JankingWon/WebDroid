package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PolicyMappingsExtension extends Extension implements CertAttrSet<String> {
   public static final String IDENT = "x509.info.extensions.PolicyMappings";
   public static final String NAME = "PolicyMappings";
   public static final String MAP = "map";
   private List<CertificatePolicyMap> maps;

   private void encodeThis() throws IOException {
      if (this.maps != null && !this.maps.isEmpty()) {
         DerOutputStream var1 = new DerOutputStream();
         DerOutputStream var2 = new DerOutputStream();
         Iterator var3 = this.maps.iterator();

         while(var3.hasNext()) {
            CertificatePolicyMap var4 = (CertificatePolicyMap)var3.next();
            var4.encode(var2);
         }

         var1.write((byte)48, (DerOutputStream)var2);
         this.extensionValue = var1.toByteArray();
      } else {
         this.extensionValue = null;
      }
   }

   public PolicyMappingsExtension(List<CertificatePolicyMap> var1) throws IOException {
      this.maps = var1;
      this.extensionId = PKIXExtensions.PolicyMappings_Id;
      this.critical = false;
      this.encodeThis();
   }

   public PolicyMappingsExtension() {
      this.extensionId = PKIXExtensions.KeyUsage_Id;
      this.critical = false;
      this.maps = Collections.emptyList();
   }

   public PolicyMappingsExtension(Boolean var1, Object var2) throws IOException {
      this.extensionId = PKIXExtensions.PolicyMappings_Id;
      this.critical = var1;
      this.extensionValue = (byte[])((byte[])var2);
      DerValue var3 = new DerValue(this.extensionValue);
      if (var3.tag != 48) {
         throw new IOException("Invalid encoding for PolicyMappingsExtension.");
      } else {
         this.maps = new ArrayList();

         while(var3.data.available() != 0) {
            DerValue var4 = var3.data.getDerValue();
            CertificatePolicyMap var5 = new CertificatePolicyMap(var4);
            this.maps.add(var5);
         }

      }
   }

   public String toString() {
      if (this.maps == null) {
         return "";
      } else {
         String var1 = super.toString() + "PolicyMappings [\n" + this.maps.toString() + "]\n";
         return var1;
      }
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = PKIXExtensions.PolicyMappings_Id;
         this.critical = false;
         this.encodeThis();
      }

      super.encode(var2);
      var1.write(var2.toByteArray());
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("map")) {
         if (!(var2 instanceof List)) {
            throw new IOException("Attribute value should be of type List.");
         } else {
            this.maps = (List)var2;
            this.encodeThis();
         }
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension.");
      }
   }

   public List<CertificatePolicyMap> get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("map")) {
         return this.maps;
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("map")) {
         this.maps = null;
         this.encodeThis();
      } else {
         throw new IOException("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension.");
      }
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("map");
      return var1.elements();
   }

   public String getName() {
      return "PolicyMappings";
   }
}
