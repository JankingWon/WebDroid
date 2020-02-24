package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class ExtendedKeyUsageExtension extends Extension implements CertAttrSet<String> {
   public static final String IDENT = "x509.info.extensions.ExtendedKeyUsage";
   public static final String NAME = "ExtendedKeyUsage";
   public static final String USAGES = "usages";
   private static final Map<ObjectIdentifier, String> map = new HashMap();
   private static final int[] anyExtendedKeyUsageOidData = new int[]{2, 5, 29, 37, 0};
   private static final int[] serverAuthOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 1};
   private static final int[] clientAuthOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 2};
   private static final int[] codeSigningOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 3};
   private static final int[] emailProtectionOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 4};
   private static final int[] ipsecEndSystemOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 5};
   private static final int[] ipsecTunnelOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 6};
   private static final int[] ipsecUserOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 7};
   private static final int[] timeStampingOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 8};
   private static final int[] OCSPSigningOidData = new int[]{1, 3, 6, 1, 5, 5, 7, 3, 9};
   private Vector<ObjectIdentifier> keyUsages;

   private void encodeThis() throws IOException {
      if (this.keyUsages != null && !this.keyUsages.isEmpty()) {
         DerOutputStream var1 = new DerOutputStream();
         DerOutputStream var2 = new DerOutputStream();

         for(int var3 = 0; var3 < this.keyUsages.size(); ++var3) {
            var2.putOID((ObjectIdentifier)this.keyUsages.elementAt(var3));
         }

         var1.write((byte)48, (DerOutputStream)var2);
         this.extensionValue = var1.toByteArray();
      } else {
         this.extensionValue = null;
      }
   }

   public ExtendedKeyUsageExtension(Vector<ObjectIdentifier> var1) throws IOException {
      this(Boolean.FALSE, var1);
   }

   public ExtendedKeyUsageExtension(Boolean var1, Vector<ObjectIdentifier> var2) throws IOException {
      this.keyUsages = var2;
      this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
      this.critical = var1;
      this.encodeThis();
   }

   public ExtendedKeyUsageExtension(Boolean var1, Object var2) throws IOException {
      this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
      this.critical = var1;
      this.extensionValue = (byte[])((byte[])var2);
      DerValue var3 = new DerValue(this.extensionValue);
      if (var3.tag != 48) {
         throw new IOException("Invalid encoding for ExtendedKeyUsageExtension.");
      } else {
         this.keyUsages = new Vector();

         while(var3.data.available() != 0) {
            DerValue var4 = var3.data.getDerValue();
            ObjectIdentifier var5 = var4.getOID();
            this.keyUsages.addElement(var5);
         }

      }
   }

   public String toString() {
      if (this.keyUsages == null) {
         return "";
      } else {
         String var1 = "  ";
         boolean var2 = true;

         for(Iterator var3 = this.keyUsages.iterator(); var3.hasNext(); var2 = false) {
            ObjectIdentifier var4 = (ObjectIdentifier)var3.next();
            if (!var2) {
               var1 = var1 + "\n  ";
            }

            String var5 = (String)map.get(var4);
            if (var5 != null) {
               var1 = var1 + var5;
            } else {
               var1 = var1 + var4.toString();
            }
         }

         return super.toString() + "ExtendedKeyUsages [\n" + var1 + "\n]\n";
      }
   }

   public void encode(OutputStream var1) throws IOException {
      DerOutputStream var2 = new DerOutputStream();
      if (this.extensionValue == null) {
         this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
         this.critical = false;
         this.encodeThis();
      }

      super.encode(var2);
      var1.write(var2.toByteArray());
   }

   public void set(String var1, Object var2) throws IOException {
      if (var1.equalsIgnoreCase("usages")) {
         if (!(var2 instanceof Vector)) {
            throw new IOException("Attribute value should be of type Vector.");
         } else {
            this.keyUsages = (Vector)var2;
            this.encodeThis();
         }
      } else {
         throw new IOException("Attribute name [" + var1 + "] not recognized by CertAttrSet:ExtendedKeyUsageExtension.");
      }
   }

   public Vector<ObjectIdentifier> get(String var1) throws IOException {
      if (var1.equalsIgnoreCase("usages")) {
         return this.keyUsages;
      } else {
         throw new IOException("Attribute name [" + var1 + "] not recognized by CertAttrSet:ExtendedKeyUsageExtension.");
      }
   }

   public void delete(String var1) throws IOException {
      if (var1.equalsIgnoreCase("usages")) {
         this.keyUsages = null;
         this.encodeThis();
      } else {
         throw new IOException("Attribute name [" + var1 + "] not recognized by CertAttrSet:ExtendedKeyUsageExtension.");
      }
   }

   public Enumeration<String> getElements() {
      AttributeNameEnumeration var1 = new AttributeNameEnumeration();
      var1.addElement("usages");
      return var1.elements();
   }

   public String getName() {
      return "ExtendedKeyUsage";
   }

   public List<String> getExtendedKeyUsage() {
      ArrayList var1 = new ArrayList(this.keyUsages.size());
      Iterator var2 = this.keyUsages.iterator();

      while(var2.hasNext()) {
         ObjectIdentifier var3 = (ObjectIdentifier)var2.next();
         var1.add(var3.toString());
      }

      return var1;
   }

   static {
      map.put(ObjectIdentifier.newInternal(anyExtendedKeyUsageOidData), "anyExtendedKeyUsage");
      map.put(ObjectIdentifier.newInternal(serverAuthOidData), "serverAuth");
      map.put(ObjectIdentifier.newInternal(clientAuthOidData), "clientAuth");
      map.put(ObjectIdentifier.newInternal(codeSigningOidData), "codeSigning");
      map.put(ObjectIdentifier.newInternal(emailProtectionOidData), "emailProtection");
      map.put(ObjectIdentifier.newInternal(ipsecEndSystemOidData), "ipsecEndSystem");
      map.put(ObjectIdentifier.newInternal(ipsecTunnelOidData), "ipsecTunnel");
      map.put(ObjectIdentifier.newInternal(ipsecUserOidData), "ipsecUser");
      map.put(ObjectIdentifier.newInternal(timeStampingOidData), "timeStamping");
      map.put(ObjectIdentifier.newInternal(OCSPSigningOidData), "OCSPSigning");
   }
}
