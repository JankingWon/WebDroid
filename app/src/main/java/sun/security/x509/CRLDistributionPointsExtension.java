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
import sun.security.util.ObjectIdentifier;

public class CRLDistributionPointsExtension extends Extension implements CertAttrSet<String> {
   public static final String IDENT = "x509.info.extensions.CRLDistributionPoints";
   public static final String NAME = "CRLDistributionPoints";
   public static final String POINTS = "points";
   private List<DistributionPoint> distributionPoints;
   private String extensionName;

   public CRLDistributionPointsExtension(List<DistributionPoint> var1) throws IOException {
      this(false, var1);
   }

   public CRLDistributionPointsExtension(boolean var1, List<DistributionPoint> var2) throws IOException {
      this(PKIXExtensions.CRLDistributionPoints_Id, var1, var2, "CRLDistributionPoints");
   }

   protected CRLDistributionPointsExtension(ObjectIdentifier var1, boolean var2, List<DistributionPoint> var3, String var4) throws IOException {
      this.extensionId = var1;
      this.critical = var2;
      this.distributionPoints = var3;
      this.encodeThis();
      this.extensionName = var4;
   }

   public CRLDistributionPointsExtension(Boolean var1, Object var2) throws IOException {
      this(PKIXExtensions.CRLDistributionPoints_Id, var1, var2, "CRLDistributionPoints");
   }

   protected CRLDistributionPointsExtension(ObjectIdentifier var1, Boolean var2, Object var3, String var4) throws IOException {
      this.extensionId = var1;
      this.critical = var2;
      if (!(var3 instanceof byte[])) {
         throw new IOException("Illegal argument type");
      } else {
         this.extensionValue = (byte[])((byte[])var3);
         DerValue var5 = new DerValue(this.extensionValue);
         if (var5.tag != 48) {
            throw new IOException("Invalid encoding for " + var4 + " extension.");
         } else {
            this.distributionPoints = new ArrayList();

            while(var5.data.available() != 0) {
               DerValue var6 = var5.data.getDerValue();
               DistributionPoint var7 = new DistributionPoint(var6);
               this.distributionPoints.add(var7);
            }

            this.extensionName = var4;
         }
      }
   }

   public String getName() {
      return this.extensionName;
   }

   public void encode(OutputStream var1) throws IOException {
      this.encode(var1, PKIXExtensions.CRLDistributionPoints_Id, false);
   }

   protected void encode(OutputStream var1, ObjectIdentifier var2, boolean var3) throws IOException {
      DerOutputStream var4 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = var2;
         this.critical = var3;
         this.encodeThis();
      }

      super.encode(var4);
      var1.write(var4.toByteArray());
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("points")) {
         if (!(var2 instanceof List)) {
            throw new IOException("Attribute value should be of type List.");
         } else {
            this.distributionPoints = (List)var2;
            this.encodeThis();
         }
      } else {
         throw new IOException("Attribute name [" + var1 + "] not recognized by CertAttrSet:" + this.extensionName + ".");
      }
   }

   public List<DistributionPoint> get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("points")) {
         return this.distributionPoints;
      } else {
         throw new IOException("Attribute name [" + var1 + "] not recognized by CertAttrSet:" + this.extensionName + ".");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("points")) {
         this.distributionPoints = Collections.emptyList();
         this.encodeThis();
      } else {
         throw new IOException("Attribute name [" + var1 + "] not recognized by CertAttrSet:" + this.extensionName + '.');
      }
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("points");
      return var1.elements();
   }

   private void encodeThis() throws IOException {
      if (this.distributionPoints.isEmpty()) {
         this.extensionValue = null;
      } else {
         DerOutputStream var1 = new DerOutputStream();
         Iterator var2 = this.distributionPoints.iterator();

         while(var2.hasNext()) {
            DistributionPoint var3 = (DistributionPoint)var2.next();
            var3.encode(var1);
         }

         DerOutputStream var4 = new DerOutputStream();
         var4.write((byte)48, (DerOutputStream)var1);
         this.extensionValue = var4.toByteArray();
      }

   }

   public String toString() {
      return super.toString() + this.extensionName + " [\n  " + this.distributionPoints + "]\n";
   }
}
